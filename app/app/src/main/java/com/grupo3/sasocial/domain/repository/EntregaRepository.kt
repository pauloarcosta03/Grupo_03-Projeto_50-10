package com.grupo3.sasocial.domain.repository

import com.grupo3.sasocial.domain.model.Entrega
import kotlinx.coroutines.flow.Flow

interface EntregaRepository {
    fun getAllEntregas(): Flow<List<Entrega>>
    suspend fun createEntrega(entrega: Entrega): String
}
