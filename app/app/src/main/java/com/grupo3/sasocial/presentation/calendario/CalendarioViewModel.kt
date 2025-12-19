package com.grupo3.sasocial.presentation.calendario

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grupo3.sasocial.di.AppModule
import com.grupo3.sasocial.domain.model.Entrega
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalendarioViewModel(application: Application) : AndroidViewModel(application) {
    private val entregaRepository = AppModule.provideEntregaRepository()
    
    private val _entregas = MutableStateFlow<List<Entrega>>(emptyList())
    val entregas: StateFlow<List<Entrega>> = _entregas.asStateFlow()
    
    init {
        loadEntregas()
    }
    
    private fun loadEntregas() {
        viewModelScope.launch {
            entregaRepository.getAllEntregas().collect { _entregas.value = it }
        }
    }
}
