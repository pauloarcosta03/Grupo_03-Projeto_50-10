package com.grupo3.sasocial.presentation.beneficiario

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grupo3.sasocial.di.AppModule
import com.grupo3.sasocial.domain.model.Pedido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BeneficiarioPedidosViewModel(
    application: Application,
    private val beneficiarioEmail: String
) : AndroidViewModel(application) {
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
            android.util.Log.d("BeneficiarioPedidosViewModel", "=== loadPedidos ===")
            android.util.Log.d("BeneficiarioPedidosViewModel", "Buscando pedidos para email: '$beneficiarioEmail' (trimmed: '${beneficiarioEmail.trim().lowercase()}', length=${beneficiarioEmail.length})")
            
            pedidoRepository.getPedidosByBeneficiarioEmail(beneficiarioEmail).collect { pedidosList ->
                android.util.Log.d("BeneficiarioPedidosViewModel", "📦 Pedidos recebidos do Flow: ${pedidosList.size}")
                pedidosList.forEach { pedido ->
                    android.util.Log.d("BeneficiarioPedidosViewModel", "  - Pedido: ID=${pedido.id}, Email='${pedido.beneficiarioEmail}', Status=${pedido.status}, Items=${pedido.items.size}")
                }
                
                // Ordenar por data de criação (mais recentes primeiro)
                val sorted = pedidosList.sortedByDescending { it.createdAt?.seconds ?: 0L }
                _pedidos.value = sorted
                _isLoading.value = false
                
                android.util.Log.d("BeneficiarioPedidosViewModel", "StateFlow atualizado com ${sorted.size} pedidos ordenados")
            }
        }
    }
}

