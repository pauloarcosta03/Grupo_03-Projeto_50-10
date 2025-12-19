package com.grupo3.sasocial.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grupo3.sasocial.di.AppModule
import com.grupo3.sasocial.domain.model.Bem
import com.grupo3.sasocial.domain.model.Beneficiario
import com.grupo3.sasocial.domain.model.Entrega
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val bemRepository = AppModule.provideBemRepository()
    private val entregaRepository = AppModule.provideEntregaRepository()
    private val beneficiarioRepository = AppModule.provideBeneficiarioRepository()
    
    private val _bens = MutableStateFlow<List<Bem>>(emptyList())
    val bens: StateFlow<List<Bem>> = _bens.asStateFlow()
    
    private val _entregas = MutableStateFlow<List<Entrega>>(emptyList())
    val entregas: StateFlow<List<Entrega>> = _entregas.asStateFlow()
    
    private val _beneficiariosPendentes = MutableStateFlow<List<Beneficiario>>(emptyList())
    val beneficiariosPendentes: StateFlow<List<Beneficiario>> = _beneficiariosPendentes.asStateFlow()
    
    private val _beneficiariosAceites = MutableStateFlow<List<Beneficiario>>(emptyList())
    val beneficiariosAceites: StateFlow<List<Beneficiario>> = _beneficiariosAceites.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            bemRepository.getAllBens().collect { _bens.value = it }
        }
        viewModelScope.launch {
            entregaRepository.getAllEntregas().collect { _entregas.value = it }
        }
        viewModelScope.launch {
            beneficiarioRepository.getAllBeneficiarios().collect { list ->
                _beneficiariosPendentes.value = list.filter { it.status == "pendente" }
                _beneficiariosAceites.value = list.filter { 
                    it.status.equals("aceite", ignoreCase = true) || 
                    it.status.equals("aprovado", ignoreCase = true)
                }
            }
        }
    }
    
    fun getCategoriasBeneficiario(beneficiario: Beneficiario): List<String> {
        val categorias = mutableListOf<String>()
        if (beneficiario.produtosAlimentares) categorias.add("Alimentos")
        if (beneficiario.produtosHigienePessoal) categorias.add("Higiene")
        if (beneficiario.produtosLimpeza) categorias.add("Limpeza")
        if (beneficiario.outros) categorias.add("Outros")
        return categorias
    }
    
    fun deleteBem(id: String) {
        viewModelScope.launch {
            bemRepository.deleteBem(id)
        }
    }
}
