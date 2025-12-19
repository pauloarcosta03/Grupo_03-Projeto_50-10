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

class BeneficiarioDetailViewModel(
    application: Application,
    private val beneficiarioId: String
) : AndroidViewModel(application) {
    private val beneficiarioRepository = AppModule.provideBeneficiarioRepository()
    
    private val _beneficiario = MutableStateFlow<Beneficiario?>(null)
    val beneficiario: StateFlow<Beneficiario?> = _beneficiario.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadBeneficiario()
    }
    
    private fun loadBeneficiario() {
        viewModelScope.launch {
            _isLoading.value = true
            _beneficiario.value = beneficiarioRepository.getBeneficiarioById(beneficiarioId)
            _isLoading.value = false
        }
    }
    
    private val _approvalResult = MutableStateFlow<Result<String>?>(null)
    val approvalResult: StateFlow<Result<String>?> = _approvalResult.asStateFlow()
    
    fun aprovarBeneficiario() {
        viewModelScope.launch {
            val beneficiario = _beneficiario.value
            if (beneficiario != null && beneficiario.email.isNotEmpty()) {
                val result = beneficiarioRepository.aprovarBeneficiarioComConta(beneficiarioId, beneficiario.email)
                _approvalResult.value = result
                if (result.isSuccess) {
                    loadBeneficiario() // Recarregar para atualizar status
                }
            } else {
                // Fallback: aprovar sem criar conta se não houver email
                beneficiarioRepository.aprovarBeneficiario(beneficiarioId)
                _approvalResult.value = Result.success("")
                loadBeneficiario()
            }
        }
    }
    
    fun criarContaParaBeneficiarioAprovado() {
        viewModelScope.launch {
            val beneficiario = _beneficiario.value
            if (beneficiario != null && beneficiario.email.isNotEmpty()) {
                val result = beneficiarioRepository.criarContaParaBeneficiarioAprovado(beneficiarioId, beneficiario.email)
                _approvalResult.value = result
            } else {
                _approvalResult.value = Result.failure(IllegalArgumentException("Email não disponível"))
            }
        }
    }
    
    fun rejeitarBeneficiario() {
        viewModelScope.launch {
            beneficiarioRepository.rejeitarBeneficiario(beneficiarioId)
        }
    }
}
