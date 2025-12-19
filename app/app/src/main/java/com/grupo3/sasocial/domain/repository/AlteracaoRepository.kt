package com.grupo3.sasocial.domain.repository

import com.grupo3.sasocial.domain.model.Alteracao
import kotlinx.coroutines.flow.Flow

interface AlteracaoRepository {
    fun getAllAlteracoes(): Flow<List<Alteracao>>
    suspend fun createAlteracao(alteracao: Alteracao): String
}
