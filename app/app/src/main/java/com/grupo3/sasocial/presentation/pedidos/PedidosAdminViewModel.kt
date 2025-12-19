package com.grupo3.sasocial.presentation.pedidos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grupo3.sasocial.di.AppModule
import com.grupo3.sasocial.domain.model.Pedido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PedidosAdminViewModel(application: Application) : AndroidViewModel(application) {
    private val pedidoRepository = AppModule.providePedidoRepository()
    
    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadPedidos()
    }
    
    private fun loadPedidos() {
        viewModelScope.launch {
            _isLoading.value = true
            android.util.Log.d("PedidosAdminViewModel", "=== loadPedidos ===")
            android.util.Log.d("PedidosAdminViewModel", "Aguardando pedidos do Flow...")
            
            pedidoRepository.getAllPedidos().collect { pedidosList ->
                android.util.Log.d("PedidosAdminViewModel", "📦 Pedidos recebidos do Flow: ${pedidosList.size}")
                pedidosList.forEach { pedido ->
                    android.util.Log.d("PedidosAdminViewModel", "  - Pedido: ID=${pedido.id}, Email='${pedido.beneficiarioEmail}', Status=${pedido.status}, Items=${pedido.items.size}")
                }
                
                val sorted = pedidosList.sortedByDescending { it.createdAt?.seconds ?: 0L }
                _pedidos.value = sorted
                _isLoading.value = false
                
                android.util.Log.d("PedidosAdminViewModel", "StateFlow atualizado com ${sorted.size} pedidos ordenados")
            }
        }
    }
    
    fun getPedidosPendentes(): List<Pedido> {
        return _pedidos.value.filter { it.status == "PENDENTE" }
    }
    
    fun getPedidosAprovados(): List<Pedido> {
        return _pedidos.value.filter { it.status == "APROVADO" }
    }
    
    fun getPedidosRejeitados(): List<Pedido> {
        return _pedidos.value.filter { it.status == "REJEITADO" }
    }
    
    fun getPedidosEntregues(): List<Pedido> {
        return _pedidos.value.filter { it.status == "ENTREGUE" }
    }
}

