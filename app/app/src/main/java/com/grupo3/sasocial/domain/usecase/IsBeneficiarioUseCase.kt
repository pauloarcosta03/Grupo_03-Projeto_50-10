package com.grupo3.sasocial.domain.usecase

import com.grupo3.sasocial.data.remote.AuthDataSource
import com.grupo3.sasocial.data.remote.FirestoreDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class IsBeneficiarioUseCase(
    private val authDataSource: AuthDataSource,
    private val firestoreDataSource: FirestoreDataSource
) {
    suspend fun invoke(): Boolean {
        val userId = authDataSource.getCurrentUserId()
        val userEmail = authDataSource.getCurrentUserEmail().first()
        
        android.util.Log.d("IsBeneficiario", "Verificando tipo de utilizador. UID: $userId, Email: $userEmail")
        
        // UID específico é sempre administrador
        if (userId == "nfvJnZzqgkRbEpzGURPdzcqrWz22") {
            android.util.Log.d("IsBeneficiario", "UID corresponde ao administrador, não é beneficiário")
            return false
        }
        
        if (userEmail.isBlank()) {
            android.util.Log.d("IsBeneficiario", "Email vazio, não é beneficiário")
            return false
        }
        
        // admin@ipca.pt é apenas para website, não é beneficiário na app
        if (userEmail.equals("admin@ipca.pt", ignoreCase = true)) {
            android.util.Log.d("IsBeneficiario", "Email é admin@ipca.pt (website), não é beneficiário")
            return false
        }
        
        // Se o email termina com @alunos.ipca.pt, é beneficiário
        if (userEmail.endsWith("@alunos.ipca.pt", ignoreCase = true)) {
            android.util.Log.d("IsBeneficiario", "Email termina com @alunos.ipca.pt, é beneficiário")
            return true
        }
        
        // Se o email termina com @ipca.pt (mas não é admin@ipca.pt), é beneficiário
        if (userEmail.endsWith("@ipca.pt", ignoreCase = true)) {
            android.util.Log.d("IsBeneficiario", "Email termina com @ipca.pt, é beneficiário")
            return true
        }
        
        // Se o email termina com @gmail.com, é beneficiário
        if (userEmail.endsWith("@gmail.com", ignoreCase = true)) {
            android.util.Log.d("IsBeneficiario", "Email termina com @gmail.com, é beneficiário")
            return true
        }
        
        // Verificar se existe candidatura aprovada com este email (fallback)
        val beneficiarios = firestoreDataSource.getAllBeneficiarios()
        val allBeneficiarios = beneficiarios.first()
        android.util.Log.d("IsBeneficiario", "Total de candidaturas: ${allBeneficiarios.size}")
        
        val isBenef = allBeneficiarios.any { beneficiario ->
            val emailMatch = beneficiario.email.equals(userEmail, ignoreCase = true)
            val statusMatch = beneficiario.status.equals("aceite", ignoreCase = true) || 
                             beneficiario.status.equals("aprovado", ignoreCase = true)
            
            android.util.Log.d("IsBeneficiario", 
                "Candidatura: email=${beneficiario.email}, status=${beneficiario.status}, " +
                "emailMatch=$emailMatch, statusMatch=$statusMatch")
            
            emailMatch && statusMatch
        }
        
        android.util.Log.d("IsBeneficiario", "Resultado: É beneficiário = $isBenef")
        return isBenef
    }
    
    suspend fun getBeneficiarioAprovado(): com.grupo3.sasocial.domain.model.Beneficiario? {
        val userId = authDataSource.getCurrentUserId()
        val userEmail = authDataSource.getCurrentUserEmail().first()
        
        android.util.Log.d("IsBeneficiario", "=== getBeneficiarioAprovado ===")
        android.util.Log.d("IsBeneficiario", "User ID: $userId")
        android.util.Log.d("IsBeneficiario", "User Email: $userEmail")
        
        // UID específico é administrador, não retorna beneficiário
        if (userId == "nfvJnZzqgkRbEpzGURPdzcqrWz22") {
            android.util.Log.d("IsBeneficiario", "É administrador, retornando null")
            return null
        }
        
        if (userEmail.isBlank()) {
            android.util.Log.d("IsBeneficiario", "Email vazio, retornando null")
            return null
        }
        
        // admin@ipca.pt é apenas para website, não é beneficiário na app
        if (userEmail.equals("admin@ipca.pt", ignoreCase = true)) {
            android.util.Log.d("IsBeneficiario", "É admin@ipca.pt, retornando null")
            return null
        }
        
        // PRIMEIRO: Procura QUALQUER candidatura com este email (independentemente do status)
        // Isto garante que sempre usa o nome da candidatura se existir
        // Usa função síncrona para obter dados imediatamente
        val todasCandidaturas = firestoreDataSource.getAllBeneficiariosSync()
        android.util.Log.d("IsBeneficiario", "Total de candidaturas carregadas: ${todasCandidaturas.size}")
        todasCandidaturas.forEach { candidatura ->
            android.util.Log.d("IsBeneficiario", "Candidatura: Email=${candidatura.email}, Nome=${candidatura.nome}, Status=${candidatura.status}")
        }
        
        val candidaturaComEmail = todasCandidaturas.firstOrNull { 
            val matches = it.email.equals(userEmail, ignoreCase = true)
            if (matches) {
                android.util.Log.d("IsBeneficiario", "Candidatura encontrada: ID=${it.id}, Nome=${it.nome}, Status=${it.status}, Email=${it.email}")
            }
            matches
        }
        
        // Se encontrar candidatura aprovada, retorna ela
        if (candidaturaComEmail != null && (candidaturaComEmail.status == "aceite" || candidaturaComEmail.status == "aprovado")) {
            android.util.Log.d("IsBeneficiario", "✅ Candidatura APROVADA encontrada: Nome=${candidaturaComEmail.nome}, Status=${candidaturaComEmail.status}")
            return candidaturaComEmail
        }
        
        // Se encontrar candidatura (mesmo não aprovada), usa os dados dela mas cria como aprovada
        if (candidaturaComEmail != null) {
            android.util.Log.d("IsBeneficiario", "⚠️ Candidatura encontrada mas não aprovada: Nome=${candidaturaComEmail.nome}, Status=${candidaturaComEmail.status}")
            android.util.Log.d("IsBeneficiario", "Usando dados da candidatura mas criando como aprovada")
            // IMPORTANTE: Usa as categorias da candidatura, não todas as categorias
            return candidaturaComEmail.copy(
                status = "aceite"
            )
        }
        
        // Se o email termina com @alunos.ipca.pt, @ipca.pt ou @gmail.com, cria um beneficiário virtual
        // MAS só se não houver candidatura (já verificado acima)
        val isBeneficiarioEmail = userEmail.endsWith("@alunos.ipca.pt", ignoreCase = true) || 
                                 userEmail.endsWith("@ipca.pt", ignoreCase = true) ||
                                 userEmail.endsWith("@gmail.com", ignoreCase = true)
        
        if (isBeneficiarioEmail) {
            android.util.Log.d("IsBeneficiario", "Email é de beneficiário, criando beneficiário virtual (SEM candidatura)")
            // Gera nome a partir do email (já não há candidatura para usar)
            val nomeGerado = userEmail.substringBefore("@")
            android.util.Log.d("IsBeneficiario", "Nome gerado: $nomeGerado")
            
            // Beneficiário virtual tem TODAS as categorias (porque não há candidatura para restringir)
            return com.grupo3.sasocial.domain.model.Beneficiario(
                id = userId ?: "",
                email = userEmail,
                nome = nomeGerado,
                status = "aceite",
                produtosAlimentares = true,
                produtosHigienePessoal = true,
                produtosLimpeza = true,
                outros = true
            )
        }
        
        android.util.Log.d("IsBeneficiario", "Não é beneficiário, retornando null")
        return null
    }
    
    fun getCategoriasAceites(beneficiario: com.grupo3.sasocial.domain.model.Beneficiario): List<String> {
        val categorias = mutableListOf<String>()
        
        android.util.Log.d("IsBeneficiario", "=== getCategoriasAceites ===")
        android.util.Log.d("IsBeneficiario", "Beneficiário: ${beneficiario.nome}, Email: ${beneficiario.email}, ID: ${beneficiario.id}")
        android.util.Log.d("IsBeneficiario", "produtosAlimentares: ${beneficiario.produtosAlimentares}")
        android.util.Log.d("IsBeneficiario", "produtosHigienePessoal: ${beneficiario.produtosHigienePessoal}")
        android.util.Log.d("IsBeneficiario", "produtosLimpeza: ${beneficiario.produtosLimpeza}")
        android.util.Log.d("IsBeneficiario", "outros: ${beneficiario.outros}")
        
        // IMPORTANTE: SEMPRE usa apenas as categorias marcadas como true na candidatura
        // Não retorna todas as categorias mesmo que seja beneficiário virtual
        // Se o beneficiário tem candidatura, usa APENAS as categorias da candidatura
        
        val temTodas = beneficiario.produtosAlimentares && 
                      beneficiario.produtosHigienePessoal && 
                      beneficiario.produtosLimpeza && 
                      beneficiario.outros
        
        // Verificar se é beneficiário virtual (ID é UID curto ou vazio, não é ID de Firestore)
        // IDs de Firestore são longos (20+ caracteres), UIDs são também longos mas diferentes
        val isVirtual = beneficiario.id.isEmpty() || 
                       beneficiario.id.length < 20 || 
                       beneficiario.id == beneficiario.email.substringBefore("@")
        
        val isBeneficiarioEmail = beneficiario.email.endsWith("@alunos.ipca.pt", ignoreCase = true) ||
                                 beneficiario.email.endsWith("@ipca.pt", ignoreCase = true) ||
                                 beneficiario.email.endsWith("@gmail.com", ignoreCase = true)
        
        // Se é beneficiário virtual (sem candidatura) E tem todas as categorias, retorna todas
        // Mas se tem candidatura (ID longo = ID de Firestore), usa apenas as categorias da candidatura
        if (isVirtual && isBeneficiarioEmail && temTodas) {
            // Beneficiário virtual SEM candidatura - retorna todas as categorias
            android.util.Log.d("IsBeneficiario", "⚠️ Beneficiário virtual SEM candidatura - retornando TODAS as categorias")
            return listOf(
                "Alimentos", "Alimento",
                "Higiene Pessoal", "Higiene", "Higiene e Cuidados Pessoais",
                "Limpeza", "Produtos de Limpeza",
                "Outros", "Outro"
            ).distinct()
        }
        
        // Se tem candidatura OU não tem todas as categorias, usa APENAS as categorias marcadas
        android.util.Log.d("IsBeneficiario", "✅ Usando categorias da candidatura (apenas as marcadas)")
        
        // Retorna APENAS as categorias marcadas como true
        if (beneficiario.produtosAlimentares) {
            categorias.add("Alimentos")
            categorias.add("Alimento")
        }
        if (beneficiario.produtosHigienePessoal) {
            categorias.add("Higiene Pessoal")
            categorias.add("Higiene")
            categorias.add("Higiene e Cuidados Pessoais")
        }
        if (beneficiario.produtosLimpeza) {
            categorias.add("Limpeza")
            categorias.add("Produtos de Limpeza")
        }
        if (beneficiario.outros) {
            categorias.add("Outros")
            categorias.add("Outro")
        }
        
        val categoriasFinais = categorias.distinct()
        android.util.Log.d("IsBeneficiario", "Categorias aceites retornadas: $categoriasFinais")
        return categoriasFinais
    }
}

