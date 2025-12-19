package com.grupo3.sasocial.domain.repository

import com.grupo3.sasocial.domain.model.Bem
import kotlinx.coroutines.flow.Flow

interface BemRepository {
    fun getAllBens(): Flow<List<Bem>>
    suspend fun getBemById(id: String): Bem?
    suspend fun createBem(bem: Bem): String
    suspend fun updateBem(bem: Bem)
    suspend fun deleteBem(id: String)
}
