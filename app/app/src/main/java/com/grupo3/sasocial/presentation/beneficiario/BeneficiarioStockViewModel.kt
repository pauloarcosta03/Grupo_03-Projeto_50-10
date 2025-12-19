package com.grupo3.sasocial.presentation.beneficiario

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grupo3.sasocial.di.AppModule
import com.grupo3.sasocial.domain.model.Bem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BeneficiarioStockViewModel(
    application: Application,
    private val categoriasAceites: List<String>
) : AndroidViewModel(application) {
    private val bemRepository = AppModule.provideBemRepository()
    
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
                
                android.util.Log.d("BeneficiarioStockViewModel", "Categorias aceites: $categoriasAceites")
                android.util.Log.d("BeneficiarioStockViewModel", "Total de produtos: ${allBens.size}, Produtos filtrados: ${filtered.size}")
                
                _bens.value = filtered
                _isLoading.value = false
            }
        }
    }
}

