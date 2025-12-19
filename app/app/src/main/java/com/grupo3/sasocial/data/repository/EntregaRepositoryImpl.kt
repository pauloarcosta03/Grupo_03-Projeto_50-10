package com.grupo3.sasocial.data.repository

import com.grupo3.sasocial.data.remote.FirestoreDataSource
import com.grupo3.sasocial.domain.model.Entrega
import com.grupo3.sasocial.domain.repository.EntregaRepository
import kotlinx.coroutines.flow.Flow

class EntregaRepositoryImpl(
    private val firestoreDataSource: FirestoreDataSource
) : EntregaRepository {
    
    override fun getAllEntregas(): Flow<List<Entrega>> {
        return firestoreDataSource.getAllEntregas()
    }
    
    override suspend fun createEntrega(entrega: Entrega): String {
        return firestoreDataSource.createEntrega(entrega)
    }
}
