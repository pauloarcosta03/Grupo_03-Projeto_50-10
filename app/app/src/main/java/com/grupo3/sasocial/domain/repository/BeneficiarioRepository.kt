package com.grupo3.sasocial.domain.repository

import com.grupo3.sasocial.domain.model.Beneficiario
import kotlinx.coroutines.flow.Flow

interface BeneficiarioRepository {
    fun getAllBeneficiarios(): Flow<List<Beneficiario>>
    suspend fun getBeneficiarioById(id: String): Beneficiario?
    suspend fun updateBeneficiario(beneficiario: Beneficiario)
    suspend fun aprovarBeneficiario(id: String)
    suspend fun aprovarBeneficiarioComConta(id: String, email: String): Result<String> // Retorna a palavra-passe temporária
    suspend fun criarContaParaBeneficiarioAprovado(id: String, email: String): Result<String> // Cria conta para beneficiário já aprovado
    suspend fun rejeitarBeneficiario(id: String)
}
