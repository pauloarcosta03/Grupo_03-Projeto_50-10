package com.grupo3.sasocial.domain.repository

import com.grupo3.sasocial.domain.model.Pedido
import kotlinx.coroutines.flow.Flow

interface PedidoRepository {
    fun getAllPedidos(): Flow<List<Pedido>>
    fun getPedidosByBeneficiarioEmail(email: String): Flow<List<Pedido>>
    suspend fun createPedido(pedido: Pedido): String
    suspend fun updatePedidoStatus(pedidoId: String, status: String, aprovadoPor: String? = null)
}

