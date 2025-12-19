package com.grupo3.sasocial.presentation.entregas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grupo3.sasocial.di.AppModule
import com.grupo3.sasocial.domain.model.Entrega
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class HistoricoEntregasViewModel(application: Application) : AndroidViewModel(application) {
    private val entregaRepository = AppModule.provideEntregaRepository()
    
    private val _entregas = MutableStateFlow<List<Entrega>>(emptyList())
    val entregas: StateFlow<List<Entrega>> = _entregas.asStateFlow()
    
    init {
        loadEntregas()
    }
    
    private fun loadEntregas() {
        viewModelScope.launch {
            entregaRepository.getAllEntregas().collect { 
                _entregas.value = it.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
            }
        }
    }
    
    // Estatísticas
    val totalEntregas: Int
        get() = _entregas.value.size
    
    val totalEntregue: Int
        get() = _entregas.value.sumOf { it.quantity }
    
    val entregasHoje: Int
        get() {
            val hoje = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val hojeMillis = hoje.timeInMillis
            val amanhaMillis = hojeMillis + 24 * 60 * 60 * 1000
            
            return _entregas.value.count { entrega ->
                val entregaMillis = entrega.createdAt?.toDate()?.time ?: 0L
                entregaMillis >= hojeMillis && entregaMillis < amanhaMillis
            }
        }
    
    val quantidadeHoje: Int
        get() {
            val hoje = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val hojeMillis = hoje.timeInMillis
            val amanhaMillis = hojeMillis + 24 * 60 * 60 * 1000
            
            return _entregas.value
                .filter { entrega ->
                    val entregaMillis = entrega.createdAt?.toDate()?.time ?: 0L
                    entregaMillis >= hojeMillis && entregaMillis < amanhaMillis
                }
                .sumOf { it.quantity }
        }
}

