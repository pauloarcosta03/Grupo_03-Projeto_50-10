package com.grupo3.sasocial.data.repository

import com.grupo3.sasocial.data.remote.FirestoreDataSource
import com.grupo3.sasocial.domain.model.Alteracao
import com.grupo3.sasocial.domain.repository.AlteracaoRepository
import kotlinx.coroutines.flow.Flow

class AlteracaoRepositoryImpl(
    private val firestoreDataSource: FirestoreDataSource
) : AlteracaoRepository {
    
    override fun getAllAlteracoes(): Flow<List<Alteracao>> {
        return firestoreDataSource.getAllAlteracoes()
    }
    
    override suspend fun createAlteracao(alteracao: Alteracao): String {
        return firestoreDataSource.createAlteracao(alteracao)
    }
}
