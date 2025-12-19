package com.grupo3.sasocial.data.repository

import com.grupo3.sasocial.data.remote.AuthDataSource
import com.grupo3.sasocial.data.remote.FirestoreDataSource
import com.grupo3.sasocial.domain.model.Beneficiario
import com.grupo3.sasocial.domain.repository.BeneficiarioRepository
import kotlinx.coroutines.flow.Flow

class BeneficiarioRepositoryImpl(
    private val firestoreDataSource: FirestoreDataSource,
    private val authDataSource: AuthDataSource
) : BeneficiarioRepository {
    
    override fun getAllBeneficiarios(): Flow<List<Beneficiario>> {
        return firestoreDataSource.getAllBeneficiarios()
    }
    
    override suspend fun getBeneficiarioById(id: String): Beneficiario? {
        return firestoreDataSource.getBeneficiarioById(id)
    }
    
    override suspend fun updateBeneficiario(beneficiario: Beneficiario) {
        // Not used directly, use status update instead
    }
    
    override suspend fun aprovarBeneficiario(id: String) {
        firestoreDataSource.updateBeneficiarioStatus(id, "aceite")
    }
    
    override suspend fun aprovarBeneficiarioComConta(id: String, email: String): Result<String> {
        return try {
            // 0. Validar email (aceita emails do IPCA: @alunos.ipca.pt, @ipca.pt, etc.)
            val trimmedEmail = email.trim()
            if (!isValidEmail(trimmedEmail)) {
                android.util.Log.e("BeneficiarioRepository", "Email inválido: $trimmedEmail")
                return Result.failure(IllegalArgumentException("Email inválido: $trimmedEmail"))
            }
            
            // 1. Atualizar status para "aceite"
            firestoreDataSource.updateBeneficiarioStatus(id, "aceite")
            
            // 2. Gerar palavra-passe temporária
            val temporaryPassword = authDataSource.generateTemporaryPassword()
            
            // 3. Criar conta no Firebase Auth
            val createResult = authDataSource.createUserWithEmailAndPassword(trimmedEmail, temporaryPassword)
            if (createResult.isFailure) {
                // Se a conta já existe, apenas atualizar status
                val error = createResult.exceptionOrNull()
                if (error?.message?.contains("already exists") == true) {
                    android.util.Log.w("BeneficiarioRepository", "Conta já existe para $email, apenas atualizando status")
                    return Result.success(temporaryPassword)
                } else {
                    return Result.failure(error ?: Exception("Erro ao criar conta"))
                }
            }
            
            // 4. Enviar email de redefinição de palavra-passe
            // O Firebase Auth enviará um email com link para redefinir
            authDataSource.sendPasswordResetEmail(trimmedEmail)
            
            // Nota: A palavra-passe temporária é retornada apenas para feedback ao admin
            // Em produção, considere usar Cloud Functions para enviar email personalizado
            Result.success(temporaryPassword)
        } catch (e: Exception) {
            android.util.Log.e("BeneficiarioRepository", "Erro ao aprovar beneficiário com conta", e)
            Result.failure(e)
        }
    }
    
    override suspend fun criarContaParaBeneficiarioAprovado(id: String, email: String): Result<String> {
        return try {
            // Validar email
            val trimmedEmail = email.trim()
            if (!isValidEmail(trimmedEmail)) {
                android.util.Log.e("BeneficiarioRepository", "Email inválido: $trimmedEmail")
                return Result.failure(IllegalArgumentException("Email inválido: $trimmedEmail"))
            }
            
            // Verificar se o beneficiário está aprovado
            val beneficiario = firestoreDataSource.getBeneficiarioById(id)
            if (beneficiario == null) {
                return Result.failure(IllegalArgumentException("Beneficiário não encontrado"))
            }
            
            if (beneficiario.status != "aceite" && beneficiario.status != "aprovado") {
                return Result.failure(IllegalArgumentException("Só é possível criar conta para beneficiários aprovados. Status atual: ${beneficiario.status}"))
            }
            
            android.util.Log.d("BeneficiarioRepository", "Criando conta para beneficiário já aprovado: $id, Email: $trimmedEmail")
            
            // Gerar palavra-passe temporária
            val temporaryPassword = authDataSource.generateTemporaryPassword()
            
            // Criar conta no Firebase Auth
            val createResult = authDataSource.createUserWithEmailAndPassword(trimmedEmail, temporaryPassword)
            if (createResult.isFailure) {
                // Se a conta já existe, apenas retornar sucesso
                val error = createResult.exceptionOrNull()
                if (error?.message?.contains("already exists") == true) {
                    android.util.Log.w("BeneficiarioRepository", "Conta já existe para $trimmedEmail")
                    return Result.failure(IllegalArgumentException("Uma conta já existe para este email"))
                } else {
                    return Result.failure(error ?: Exception("Erro ao criar conta"))
                }
            }
            
            // Enviar email de redefinição de palavra-passe
            authDataSource.sendPasswordResetEmail(trimmedEmail)
            
            android.util.Log.d("BeneficiarioRepository", "Conta criada com sucesso para beneficiário aprovado: $trimmedEmail")
            Result.success(temporaryPassword)
        } catch (e: Exception) {
            android.util.Log.e("BeneficiarioRepository", "Erro ao criar conta para beneficiário aprovado", e)
            Result.failure(e)
        }
    }
    
    override suspend fun rejeitarBeneficiario(id: String) {
        firestoreDataSource.updateBeneficiarioStatus(id, "recusado")
    }
    
    /**
     * Valida se o email é válido.
     * Aceita emails padrão e emails do IPCA (@alunos.ipca.pt, @ipca.pt, etc.)
     */
    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        
        // Regex básico para validar formato de email
        // Aceita: letras, números, pontos, hífens, underscores antes do @
        // Aceita: domínios como @alunos.ipca.pt, @ipca.pt, @gmail.com, etc.
        val emailRegex = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$".toRegex()
        
        // Verifica formato básico
        if (!emailRegex.matches(email)) {
            return false
        }
        
        // Verificações adicionais
        // Não pode começar ou terminar com ponto, hífen ou underscore
        if (email.startsWith(".") || email.startsWith("-") || email.startsWith("_") ||
            email.endsWith(".") || email.endsWith("-") || email.endsWith("_")) {
            return false
        }
        
        // Deve ter pelo menos um caractere antes do @
        val atIndex = email.indexOf('@')
        if (atIndex <= 0 || atIndex >= email.length - 1) {
            return false
        }
        
        // Deve ter pelo menos um ponto após o @
        val domain = email.substring(atIndex + 1)
        if (!domain.contains('.')) {
            return false
        }
        
        return true
    }
}
