package com.grupo3.sasocial.presentation.beneficiario

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grupo3.sasocial.di.AppModule
import com.grupo3.sasocial.domain.model.Bem
import com.grupo3.sasocial.domain.model.Pedido
import com.grupo3.sasocial.domain.model.PedidoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp

class CriarPedidoViewModel(
    application: Application,
    private val categoriasAceites: List<String>
) : AndroidViewModel(application) {
    private val bemRepository = AppModule.provideBemRepository()
    private val pedidoRepository = AppModule.providePedidoRepository()
    
    private val _bens = MutableStateFlow<List<Bem>>(emptyList())
    val bens: StateFlow<List<Bem>> = _bens.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadBens()
    }
    
    private fun loadBens() {
        viewModelScope.launch {
            _isLoading.value = true
            bemRepository.getAllBens().collect { allBens ->
                // Filtrar apenas produtos das categorias aceites e com stock disponível
                // Usar comparação case-insensitive e verificar variações de nomes
                val filtered = allBens.filter { bem ->
                    val bemCategory = bem.category.trim()
                    val matchesCategory = categoriasAceites.any { categoriaAceite ->
                        val categoriaAceiteTrim = categoriaAceite.trim()
                        // Comparação exata (case-insensitive)
                        bemCategory.equals(categoriaAceiteTrim, ignoreCase = true) ||
                        // Verificar se contém (para casos como "Higiene" vs "Higiene Pessoal")
                        bemCategory.contains(categoriaAceiteTrim, ignoreCase = true) ||
                        categoriaAceiteTrim.contains(bemCategory, ignoreCase = true) ||
                        // Mapeamentos específicos
                        (bemCategory.equals("Alimento", ignoreCase = true) && categoriaAceiteTrim.equals("Alimentos", ignoreCase = true)) ||
                        (bemCategory.equals("Alimentos", ignoreCase = true) && categoriaAceiteTrim.equals("Alimento", ignoreCase = true)) ||
                        (bemCategory.equals("Higiene", ignoreCase = true) && categoriaAceiteTrim.contains("Higiene", ignoreCase = true)) ||
                        (bemCategory.contains("Higiene", ignoreCase = true) && categoriaAceiteTrim.equals("Higiene", ignoreCase = true)) ||
                        (bemCategory.equals("Produtos de Limpeza", ignoreCase = true) && categoriaAceiteTrim.equals("Limpeza", ignoreCase = true)) ||
                        (bemCategory.equals("Limpeza", ignoreCase = true) && categoriaAceiteTrim.equals("Produtos de Limpeza", ignoreCase = true)) ||
                        (bemCategory.equals("Outro", ignoreCase = true) && categoriaAceiteTrim.equals("Outros", ignoreCase = true)) ||
                        (bemCategory.equals("Outros", ignoreCase = true) && categoriaAceiteTrim.equals("Outro", ignoreCase = true))
                    }
                    matchesCategory && bem.quantity > 0
                }
                
                android.util.Log.d("CriarPedidoViewModel", "=== FILTRO DE PRODUTOS ===")
                android.util.Log.d("CriarPedidoViewModel", "Categorias aceites: $categoriasAceites")
                android.util.Log.d("CriarPedidoViewModel", "Total de produtos: ${allBens.size}")
                
                // Log de produtos não filtrados
                val produtosFiltradosFora = allBens.filter { bem ->
                    val bemCategory = bem.category.trim()
                    val matchesCategory = categoriasAceites.any { categoriaAceite ->
                        val categoriaAceiteTrim = categoriaAceite.trim()
                        bemCategory.equals(categoriaAceiteTrim, ignoreCase = true) ||
                        bemCategory.contains(categoriaAceiteTrim, ignoreCase = true) ||
                        categoriaAceiteTrim.contains(bemCategory, ignoreCase = true) ||
                        (bemCategory.equals("Alimento", ignoreCase = true) && categoriaAceiteTrim.equals("Alimentos", ignoreCase = true)) ||
                        (bemCategory.equals("Alimentos", ignoreCase = true) && categoriaAceiteTrim.equals("Alimento", ignoreCase = true)) ||
                        (bemCategory.equals("Higiene", ignoreCase = true) && categoriaAceiteTrim.contains("Higiene", ignoreCase = true)) ||
                        (bemCategory.contains("Higiene", ignoreCase = true) && categoriaAceiteTrim.equals("Higiene", ignoreCase = true)) ||
                        (bemCategory.equals("Produtos de Limpeza", ignoreCase = true) && categoriaAceiteTrim.equals("Limpeza", ignoreCase = true)) ||
                        (bemCategory.equals("Limpeza", ignoreCase = true) && categoriaAceiteTrim.equals("Produtos de Limpeza", ignoreCase = true)) ||
                        (bemCategory.equals("Outro", ignoreCase = true) && categoriaAceiteTrim.equals("Outros", ignoreCase = true)) ||
                        (bemCategory.equals("Outros", ignoreCase = true) && categoriaAceiteTrim.equals("Outro", ignoreCase = true))
                    }
                    !matchesCategory && bem.quantity > 0
                }
                
                produtosFiltradosFora.forEach { bem ->
                    android.util.Log.d("CriarPedidoViewModel", "❌ Produto FILTRADO FORA: ${bem.name}, Categoria: ${bem.category}")
                }
                
                android.util.Log.d("CriarPedidoViewModel", "Produtos filtrados (mostrados): ${filtered.size}")
                filtered.forEach { bem ->
                    android.util.Log.d("CriarPedidoViewModel", "✅ Produto ACEITE: ${bem.name}, Categoria: ${bem.category}")
                }
                android.util.Log.d("CriarPedidoViewModel", "=== FIM DO FILTRO ===")
                
                _bens.value = filtered
                _isLoading.value = false
            }
        }
    }
    
    fun createPedido(
        beneficiarioId: String,
        beneficiarioEmail: String,
        beneficiarioNome: String,
        selectedItems: Map<String, Int>,
        bens: List<Bem>,
        observacoes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val items = selectedItems.mapNotNull { (bemId, quantity) ->
                    val bem = bens.find { it.id == bemId }
                    bem?.let {
                        PedidoItem(
                            productId = it.id,
                            productName = it.name,
                            productCategory = it.category,
                            quantity = quantity,
                            unit = "unidade"
                        )
                    }
                }
                
                if (items.isEmpty()) {
                    onError("Selecione pelo menos um item")
                    return@launch
                }
                
                val pedido = Pedido(
                    beneficiarioId = beneficiarioId,
                    beneficiarioEmail = beneficiarioEmail,
                    beneficiarioNome = beneficiarioNome,
                    items = items,
                    status = "PENDENTE",
                    observacoes = observacoes,
                    createdAt = Timestamp.now()
                )
                
                android.util.Log.d("CriarPedidoViewModel", "Criando pedido: Email=$beneficiarioEmail, Items=${items.size}")
                val pedidoId = pedidoRepository.createPedido(pedido)
                android.util.Log.d("CriarPedidoViewModel", "Pedido criado com sucesso! ID: $pedidoId")
                
                // Aguardar um pouco para garantir que o Firestore processou
                kotlinx.coroutines.delay(1000)
                
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("CriarPedidoViewModel", "Erro ao criar pedido", e)
                onError(e.message ?: "Erro ao criar pedido")
            }
        }
    }
}

