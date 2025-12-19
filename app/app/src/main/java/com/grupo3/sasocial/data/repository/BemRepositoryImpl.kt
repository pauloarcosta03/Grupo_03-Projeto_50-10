package com.grupo3.sasocial.data.repository

import com.grupo3.sasocial.data.remote.FirestoreDataSource
import com.grupo3.sasocial.domain.model.Bem
import com.grupo3.sasocial.domain.repository.BemRepository
import kotlinx.coroutines.flow.Flow

class BemRepositoryImpl(
    private val firestoreDataSource: FirestoreDataSource
) : BemRepository {
    
    override fun getAllBens(): Flow<List<Bem>> {
        return firestoreDataSource.getAllBens()
    }
    
    override suspend fun getBemById(id: String): Bem? {
        return firestoreDataSource.getBemById(id)
    }
    
    override suspend fun createBem(bem: Bem): String {
        return firestoreDataSource.createBem(bem)
    }
    
    override suspend fun updateBem(bem: Bem) {
        firestoreDataSource.updateBem(bem)
    }
    
    override suspend fun deleteBem(id: String) {
        firestoreDataSource.deleteBem(id)
    }
}
