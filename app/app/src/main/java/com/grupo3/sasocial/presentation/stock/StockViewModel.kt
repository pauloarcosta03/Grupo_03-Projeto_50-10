package com.grupo3.sasocial.presentation.stock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.grupo3.sasocial.di.AppModule
import com.grupo3.sasocial.domain.model.Bem
import com.grupo3.sasocial.domain.model.Beneficiario
import com.grupo3.sasocial.domain.model.Entrega
import com.grupo3.sasocial.data.remote.FirestoreDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StockViewModel(application: Application) : AndroidViewModel(application) {
    private val bemRepository = AppModule.provideBemRepository()
    private val entregaRepository = AppModule.provideEntregaRepository()
    private val beneficiarioRepository = AppModule.provideBeneficiarioRepository()
    private val firestoreDataSource = AppModule.provideFirestoreDataSource()
    
    private val _bens = MutableStateFlow<List<Bem>>(emptyList())
    val bens: StateFlow<List<Bem>> = _bens.asStateFlow()
    
    private val _beneficiariosAceites = MutableStateFlow<List<Beneficiario>>(emptyList())
    val beneficiariosAceites: StateFlow<List<Beneficiario>> = _beneficiariosAceites.asStateFlow()
    
    init {
        loadBens()
        loadBeneficiariosAceites()
    }
    
    private fun loadBens() {
        viewModelScope.launch {
            bemRepository.getAllBens().collect { _bens.value = it }
        }
    }
    
    private fun loadBeneficiariosAceites() {
        viewModelScope.launch {
            beneficiarioRepository.getAllBeneficiarios().collect { beneficiarios ->
                _beneficiariosAceites.value = beneficiarios.filter { 
                    it.status == "aceite" || it.status == "aprovado" 
                }
            }
        }
    }
    
    fun getBeneficiariosByCategory(category: String): List<Beneficiario> {
        return _beneficiariosAceites.value.filter { beneficiario ->
            when (category.lowercase()) {
                "alimentos" -> beneficiario.produtosAlimentares
                "higiene" -> beneficiario.produtosHigienePessoal
                "vestuário", "vestuario" -> true // Vestuário não tem campo específico, mostrar todos
                "limpeza" -> beneficiario.produtosLimpeza
                "outros" -> beneficiario.outros
                else -> true
            }
        }
    }
    
    fun deleteBem(id: String) {
        viewModelScope.launch {
            bemRepository.deleteBem(id)
        }
    }
    
    fun darBaixa(
        bem: Bem, 
        quantidade: Int,
        beneficiaryName: String = "",
        beneficiaryEmail: String = "",
        beneficiaryId: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val stockBefore = bem.quantity
            val stockAfter = bem.quantity - quantidade
            
            if (stockAfter >= 0) {
                // Buscar ID do status "PENDENTE"
                val deliveryStatuses = firestoreDataSource.getAllDeliveryStatuses()
                val pendenteStatusId = deliveryStatuses.find { it.name.equals("Pendente", ignoreCase = true) }?.id ?: ""
                
                // Atualizar stock
                bemRepository.updateBem(bem.copy(quantity = stockAfter))
                
                // Criar registo de entrega/baixa
                val entrega = Entrega(
                    beneficiaryEmail = beneficiaryEmail,
                    beneficiaryId = beneficiaryId,
                    beneficiaryName = beneficiaryName,
                    deliveryStatusId = pendenteStatusId,
                    createdAt = Timestamp.now(),
                    deliveryDate = Timestamp.now(),
                    notes = notes,
                    productCategory = bem.category,
                    productId = bem.id,
                    productName = bem.name,
                    quantity = quantidade,
                    stockAfter = stockAfter,
                    stockBefore = stockBefore
                )
                entregaRepository.createEntrega(entrega)
            }
        }
    }
}
