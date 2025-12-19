package com.grupo3.sasocial.presentation.stock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.grupo3.sasocial.data.remote.FirestoreDataSource
import com.grupo3.sasocial.di.AppModule
import com.grupo3.sasocial.domain.model.Bem
import com.grupo3.sasocial.domain.model.GoodType
import com.grupo3.sasocial.domain.model.InventoryStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class EditBemViewModel(
    application: Application,
    private val bemId: String
) : AndroidViewModel(application) {
    private val bemRepository = AppModule.provideBemRepository()
    private val firestoreDataSource = AppModule.provideFirestoreDataSource()
    
    private val _bem = MutableStateFlow<Bem?>(null)
    val bem: StateFlow<Bem?> = _bem.asStateFlow()
    
    private val _goodTypes = MutableStateFlow<List<GoodType>>(emptyList())
    val goodTypes: StateFlow<List<GoodType>> = _goodTypes.asStateFlow()
    
    private val _inventoryStatuses = MutableStateFlow<List<InventoryStatus>>(emptyList())
    val inventoryStatuses: StateFlow<List<InventoryStatus>> = _inventoryStatuses.asStateFlow()
    
    init {
        loadBem()
        loadLookupTables()
    }
    
    private fun loadLookupTables() {
        viewModelScope.launch {
            try {
                android.util.Log.d("EditBemViewModel", "Starting to load good types...")
                val goodTypesList = firestoreDataSource.getAllGoodTypes()
                android.util.Log.d("EditBemViewModel", "Loaded ${goodTypesList.size} good types: ${goodTypesList.map { "${it.id}:${it.name}" }}")
                
                // Se não carregou nada, usar fallback
                if (goodTypesList.isEmpty()) {
                    android.util.Log.w("EditBemViewModel", "No good types loaded from Firestore, using fallback")
                    _goodTypes.value = listOf(
                        GoodType(id = "fallback_alimentos", name = "Alimentos", description = ""),
                        GoodType(id = "fallback_higiene", name = "Higiene", description = ""),
                        GoodType(id = "fallback_vestuario", name = "Vestuário", description = ""),
                        GoodType(id = "fallback_limpeza", name = "Limpeza", description = ""),
                        GoodType(id = "fallback_outros", name = "Outros", description = "")
                    )
                } else {
                    _goodTypes.value = goodTypesList
                }
                
                android.util.Log.d("EditBemViewModel", "Starting to load inventory statuses...")
                val statusesList = firestoreDataSource.getAllInventoryStatuses()
                android.util.Log.d("EditBemViewModel", "Loaded ${statusesList.size} inventory statuses: ${statusesList.map { "${it.id}:${it.name}" }}")
                _inventoryStatuses.value = statusesList
            } catch (e: Exception) {
                android.util.Log.e("EditBemViewModel", "Error loading lookup tables: ${e.message}", e)
                e.printStackTrace()
                // Se falhar, usar fallback
                _goodTypes.value = listOf(
                    GoodType(id = "fallback_alimentos", name = "Alimentos", description = ""),
                    GoodType(id = "fallback_higiene", name = "Higiene", description = ""),
                    GoodType(id = "fallback_vestuario", name = "Vestuário", description = ""),
                    GoodType(id = "fallback_limpeza", name = "Limpeza", description = ""),
                    GoodType(id = "fallback_outros", name = "Outros", description = "")
                )
            }
        }
    }
    
    private fun loadBem() {
        viewModelScope.launch {
            _bem.value = bemRepository.getBemById(bemId)
        }
    }
    
    fun updateBem(
        bem: Bem,
        name: String,
        category: String,
        goodTypeId: String,
        quantity: Int,
        minStock: Int,
        supplier: String,
        statusId: String,
        entryDate: String,
        validUntil: String
    ) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            val entryTimestamp = try {
                if (entryDate.isNotEmpty()) {
                    Timestamp(dateFormat.parse(entryDate)!!)
                } else {
                    bem.entryDate
                }
            } catch (e: Exception) {
                bem.entryDate
            }
            
            val validTimestamp = try {
                if (validUntil.isNotEmpty() && validUntil.trim().isNotEmpty()) {
                    val parsedDate = dateFormat.parse(validUntil.trim())
                    if (parsedDate != null) {
                        Timestamp(parsedDate)
                    } else {
                        android.util.Log.w("EditBemViewModel", "Failed to parse validUntil date: $validUntil, keeping existing value")
                        bem.validUntil
                    }
                } else {
                    // Se estiver vazio, guarda null (remove a data de validade)
                    null
                }
            } catch (e: Exception) {
                android.util.Log.e("EditBemViewModel", "Error parsing validUntil date: $validUntil", e)
                bem.validUntil
            }
            
            val updatedBem = bem.copy(
                name = name,
                category = category, // Mantém para compatibilidade
                goodTypeId = goodTypeId,
                quantity = quantity,
                minStock = minStock,
                supplier = supplier,
                statusId = statusId,
                entryDate = entryTimestamp,
                validUntil = validTimestamp
            )
            
            bemRepository.updateBem(updatedBem)
        }
    }
}
