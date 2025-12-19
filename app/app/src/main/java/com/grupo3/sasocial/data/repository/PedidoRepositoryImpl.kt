package com.grupo3.sasocial.data.repository

import com.grupo3.sasocial.data.remote.FirestoreDataSource
import com.grupo3.sasocial.domain.model.Pedido
import com.grupo3.sasocial.domain.repository.PedidoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PedidoRepositoryImpl(
    private val firestoreDataSource: FirestoreDataSource
) : PedidoRepository {
    
    override fun getAllPedidos(): Flow<List<Pedido>> {
        return firestoreDataSource.getAllPedidos()
    }
    
    override fun getPedidosByBeneficiarioEmail(email: String): Flow<List<Pedido>> {
        return firestoreDataSource.getAllPedidos().map { pedidos ->
            android.util.Log.d("PedidoRepository", "=== FILTRO DE PEDIDOS ===")
            val emailTrimmed = email.trim().lowercase()
            android.util.Log.d("PedidoRepository", "Email procurado: '$emailTrimmed' (original: '$email', length=${email.length})")
            android.util.Log.d("PedidoRepository", "Total de pedidos antes do filtro: ${pedidos.size}")
            
            if (pedidos.isEmpty()) {
                android.util.Log.w("PedidoRepository", "⚠️ Nenhum pedido carregado do Firestore!")
            }
            
            pedidos.forEach { pedido ->
                val pedidoEmailTrimmed = pedido.beneficiarioEmail.trim().lowercase()
                val matches = pedidoEmailTrimmed == emailTrimmed
                android.util.Log.d("PedidoRepository", "Pedido: ID=${pedido.id}, Email='${pedido.beneficiarioEmail}' (trimmed/lowercase='$pedidoEmailTrimmed'), Status=${pedido.status}, Match=$matches")
            }
            
            val filtered = pedidos.filter { pedido ->
                val pedidoEmailTrimmed = pedido.beneficiarioEmail.trim().lowercase()
                val matches = pedidoEmailTrimmed == emailTrimmed
                
                if (!matches) {
                    android.util.Log.d("PedidoRepository", "❌ Email NÃO corresponde: '$pedidoEmailTrimmed' != '$emailTrimmed'")
                } else {
                    android.util.Log.d("PedidoRepository", "✅ Email corresponde: '$pedidoEmailTrimmed' == '$emailTrimmed'")
                }
                matches
            }
            
            android.util.Log.d("PedidoRepository", "Pedidos após filtro: ${filtered.size}")
            if (filtered.isEmpty() && pedidos.isNotEmpty()) {
                android.util.Log.w("PedidoRepository", "⚠️ Nenhum pedido corresponde ao email '$emailTrimmed'!")
                android.util.Log.w("PedidoRepository", "Emails disponíveis: ${pedidos.map { it.beneficiarioEmail.trim().lowercase() }}")
            }
            android.util.Log.d("PedidoRepository", "=== FIM DO FILTRO ===")
            filtered
        }
    }
    
    override suspend fun createPedido(pedido: Pedido): String {
        return firestoreDataSource.createPedido(pedido)
    }
    
    override suspend fun updatePedidoStatus(pedidoId: String, status: String, aprovadoPor: String?): Unit {
        firestoreDataSource.updatePedidoStatus(pedidoId, status, aprovadoPor)
    }
}

