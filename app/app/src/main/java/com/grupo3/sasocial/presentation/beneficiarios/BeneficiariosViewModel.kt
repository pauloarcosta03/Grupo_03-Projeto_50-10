package com.grupo3.sasocial.presentation.beneficiarios

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grupo3.sasocial.di.AppModule
import com.grupo3.sasocial.domain.model.Beneficiario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BeneficiariosViewModel(application: Application) : AndroidViewModel(application) {
    private val beneficiarioRepository = AppModule.provideBeneficiarioRepository()
    
    private val _beneficiarios = MutableStateFlow<List<Beneficiario>>(emptyList())
    val beneficiarios: StateFlow<List<Beneficiario>> = _beneficiarios.asStateFlow()
    
    init {
        loadBeneficiarios()
    }
    
    private fun loadBeneficiarios() {
        viewModelScope.launch {
            beneficiarioRepository.getAllBeneficiarios().collect { _beneficiarios.value = it }
        }
    }
}
