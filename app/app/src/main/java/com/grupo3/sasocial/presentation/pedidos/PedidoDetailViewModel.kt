package com.grupo3.sasocial.presentation.pedidos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grupo3.sasocial.di.AppModule
import com.grupo3.sasocial.domain.model.Bem
import com.grupo3.sasocial.domain.model.Entrega
import com.grupo3.sasocial.domain.model.Pedido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import com.grupo3.sasocial.data.remote.AuthDataSource

class PedidoDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val pedidoRepository = AppModule.providePedidoRepository()
    private val bemRepository = AppModule.provideBemRepository()
    private val entregaRepository = AppModule.provideEntregaRepository()
    private val authDataSource = AppModule.provideAuthDataSource()
    
    private val _pedido = MutableStateFlow<Pedido?>(null)
    val pedido: StateFlow<Pedido?> = _pedido.asStateFlow()
    
    private val _bens = MutableStateFlow<List<Bem>>(emptyList())
    val bens: StateFlow<List<Bem>> = _bens.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _approvalResult = MutableStateFlow<Result<Unit>?>(null)
    val approvalResult: StateFlow<Result<Unit>?> = _approvalResult.asStateFlow()
    
    fun loadPedido(pedidoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            pedidoRepository.getAllPedidos().collect { pedidos ->
                _pedido.value = pedidos.find { it.id == pedidoId }
                _isLoading.value = false
            }
        }
    }
    
    fun loadBens() {
        viewModelScope.launch {
            bemRepository.getAllBens().collect { bensList ->
                _bens.value = bensList
            }
        }
    }
    
    fun aprovarPedido(pedidoId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val pedido = _pedido.value
                if (pedido == null) {
                    onError("Pedido não encontrado")
                    return@launch
                }
                
                // Verificar se o pedido já está aprovado
                if (pedido.status == "APROVADO" || pedido.status == "ENTREGUE") {
                    android.util.Log.w("PedidoDetailViewModel", "Pedido ${pedidoId} já está ${pedido.status}. Não é possível aprovar novamente.")
                    onError("Este pedido já foi ${pedido.status.lowercase()}. Não é possível aprovar novamente.")
                    return@launch
                }
                
                // Verificar se o pedido está pendente
                if (pedido.status != "PENDENTE") {
                    android.util.Log.w("PedidoDetailViewModel", "Pedido ${pedidoId} tem status ${pedido.status}. Só é possível aprovar pedidos pendentes.")
                    onError("Só é possível aprovar pedidos pendentes. Status atual: ${pedido.status}")
                    return@launch
                }
                
                android.util.Log.d("PedidoDetailViewModel", "Aprovando pedido ${pedidoId} com status atual: ${pedido.status}")
                
                // Verificar se há stock suficiente para todos os itens
                val stockInsuficiente = pedido.items.any { item ->
                    val bem = _bens.value.find { it.id == item.productId }
                    bem == null || bem.quantity < item.quantity
                }
                
                if (stockInsuficiente) {
                    onError("Stock insuficiente para um ou mais itens do pedido")
                    return@launch
                }
                
                // Atualizar status do pedido PRIMEIRO
                val currentUserId = authDataSource.getCurrentUserId() ?: "admin"
                android.util.Log.d("PedidoDetailViewModel", "Atualizando status do pedido ${pedidoId} para APROVADO")
                pedidoRepository.updatePedidoStatus(pedidoId, "APROVADO", currentUserId)
                
                // Aguardar um pouco para garantir que o status foi atualizado
                kotlinx.coroutines.delay(500)
                
                // Dar baixa do stock e criar entrega
                android.util.Log.d("PedidoDetailViewModel", "Dando baixa do stock para ${pedido.items.size} itens")
                pedido.items.forEach { item ->
                    val bem = _bens.value.find { it.id == item.productId }
                    bem?.let { produto ->
                        val stockAntes = produto.quantity
                        val stockDepois = stockAntes - item.quantity
                        
                        android.util.Log.d("PedidoDetailViewModel", "Item: ${produto.name}, Stock antes: $stockAntes, Stock depois: $stockDepois")
                        
                        // Atualizar stock - criar novo Bem com quantidade atualizada
                        val bemAtualizado = produto.copy(quantity = stockDepois)
                        bemRepository.updateBem(bemAtualizado)
                        
                        // Criar entrega
                        val entrega = Entrega(
                            beneficiaryId = pedido.beneficiarioId,
                            beneficiaryEmail = pedido.beneficiarioEmail,
                            beneficiaryName = pedido.beneficiarioNome,
                            productId = produto.id,
                            productName = produto.name,
                            productCategory = produto.category,
                            quantity = item.quantity,
                            stockBefore = stockAntes,
                            stockAfter = stockDepois,
                            deliveryDate = Timestamp.now(),
                            notes = "Entrega aprovada do pedido ${pedido.id}",
                            deliveryStatusId = "ENTREGUE",
                            createdAt = Timestamp.now()
                        )
                        
                        entregaRepository.createEntrega(entrega)
                        android.util.Log.d("PedidoDetailViewModel", "Entrega criada para item: ${produto.name}")
                    }
                }
                
                android.util.Log.d("PedidoDetailViewModel", "Pedido ${pedidoId} aprovado com sucesso!")
                
                // Atualizar o estado local do pedido para refletir a mudança imediatamente
                _pedido.value = pedido.copy(status = "APROVADO")
                
                _approvalResult.value = Result.success(Unit)
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("PedidoDetailViewModel", "Erro ao aprovar pedido", e)
                _approvalResult.value = Result.failure(e)
                onError(e.message ?: "Erro ao aprovar pedido")
            }
        }
    }
    
    fun rejeitarPedido(pedidoId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentUserId = authDataSource.getCurrentUserId() ?: "admin"
                pedidoRepository.updatePedidoStatus(pedidoId, "REJEITADO", currentUserId)
                _approvalResult.value = Result.success(Unit)
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("PedidoDetailViewModel", "Erro ao rejeitar pedido", e)
                _approvalResult.value = Result.failure(e)
                onError(e.message ?: "Erro ao rejeitar pedido")
            }
        }
    }
    
    fun marcarComoEntregue(pedidoId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentUserId = authDataSource.getCurrentUserId() ?: "admin"
                pedidoRepository.updatePedidoStatus(pedidoId, "ENTREGUE", currentUserId)
                _approvalResult.value = Result.success(Unit)
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("PedidoDetailViewModel", "Erro ao marcar como entregue", e)
                _approvalResult.value = Result.failure(e)
                onError(e.message ?: "Erro ao marcar como entregue")
            }
        }
    }
}


