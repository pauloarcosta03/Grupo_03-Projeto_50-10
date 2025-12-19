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

class BeneficiarioDashboardViewModel(
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
            android.util.Log.d("BeneficiarioDashboard", "=== loadPedidos ===")
            android.util.Log.d("BeneficiarioDashboard", "Buscando pedidos para email: '$beneficiarioEmail' (trimmed: '${beneficiarioEmail.trim().lowercase()}', length=${beneficiarioEmail.length})")
            
            pedidoRepository.getPedidosByBeneficiarioEmail(beneficiarioEmail).collect { pedidosList ->
                android.util.Log.d("BeneficiarioDashboard", "📦 Pedidos recebidos do Flow: ${pedidosList.size}")
                pedidosList.forEach { pedido ->
                    android.util.Log.d("BeneficiarioDashboard", "  - Pedido: ID=${pedido.id}, Email='${pedido.beneficiarioEmail}', Status=${pedido.status}, Items=${pedido.items.size}")
                }
                
                _pedidos.value = pedidosList
                _isLoading.value = false
                
                android.util.Log.d("BeneficiarioDashboard", "StateFlow atualizado com ${pedidosList.size} pedidos")
            }
        }
    }
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    fun refreshPedidos() {
        viewModelScope.launch {
            _isRefreshing.value = true
            android.util.Log.d("BeneficiarioDashboard", "Refresh manual solicitado")
            // O Flow já está a escutar mudanças, mas mostramos feedback visual
            // Aguardar um pouco para mostrar o feedback
            kotlinx.coroutines.delay(500)
            _isRefreshing.value = false
            // O addSnapshotListener vai atualizar automaticamente quando houver mudanças
        }
    }
}

