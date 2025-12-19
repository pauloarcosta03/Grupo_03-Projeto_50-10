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

class AddBemViewModel(application: Application) : AndroidViewModel(application) {
    private val bemRepository = AppModule.provideBemRepository()
    private val firestoreDataSource = AppModule.provideFirestoreDataSource()
    
    private val _goodTypes = MutableStateFlow<List<GoodType>>(emptyList())
    val goodTypes: StateFlow<List<GoodType>> = _goodTypes.asStateFlow()
    
    private val _inventoryStatuses = MutableStateFlow<List<InventoryStatus>>(emptyList())
    val inventoryStatuses: StateFlow<List<InventoryStatus>> = _inventoryStatuses.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadLookupTables()
    }
    
    private fun loadLookupTables() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("AddBemViewModel", "Starting to load good types...")
                val goodTypesList = firestoreDataSource.getAllGoodTypes()
                android.util.Log.d("AddBemViewModel", "Loaded ${goodTypesList.size} good types: ${goodTypesList.map { "${it.id}:${it.name}" }}")
                _goodTypes.value = goodTypesList
                
                // Se não carregou nada, usar fallback
                if (goodTypesList.isEmpty()) {
                    android.util.Log.w("AddBemViewModel", "No good types loaded from Firestore, using fallback")
                    _goodTypes.value = listOf(
                        GoodType(id = "fallback_alimentos", name = "Alimentos", description = ""),
                        GoodType(id = "fallback_higiene", name = "Higiene", description = ""),
                        GoodType(id = "fallback_vestuario", name = "Vestuário", description = ""),
                        GoodType(id = "fallback_limpeza", name = "Limpeza", description = ""),
                        GoodType(id = "fallback_outros", name = "Outros", description = "")
                    )
                }
                
                android.util.Log.d("AddBemViewModel", "Starting to load inventory statuses...")
                val statusesList = firestoreDataSource.getAllInventoryStatuses()
                android.util.Log.d("AddBemViewModel", "Loaded ${statusesList.size} inventory statuses: ${statusesList.map { "${it.id}:${it.name}" }}")
                _inventoryStatuses.value = statusesList
            } catch (e: Exception) {
                android.util.Log.e("AddBemViewModel", "Error loading lookup tables: ${e.message}", e)
                e.printStackTrace()
                // Se falhar, usar fallback
                _goodTypes.value = listOf(
                    GoodType(id = "fallback_alimentos", name = "Alimentos", description = ""),
                    GoodType(id = "fallback_higiene", name = "Higiene", description = ""),
                    GoodType(id = "fallback_vestuario", name = "Vestuário", description = ""),
                    GoodType(id = "fallback_limpeza", name = "Limpeza", description = ""),
                    GoodType(id = "fallback_outros", name = "Outros", description = "")
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createBem(bem: Bem, entryDate: String = "", validUntil: String = "") {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            val entryTimestamp = try {
                if (entryDate.isNotEmpty()) {
                    Timestamp(dateFormat.parse(entryDate)!!)
                } else {
                    Timestamp.now()
                }
            } catch (e: Exception) {
                Timestamp.now()
            }
            
            val validTimestamp = try {
                if (validUntil.isNotEmpty() && validUntil.trim().isNotEmpty()) {
                    android.util.Log.d("AddBemViewModel", "Parsing validUntil: '$validUntil'")
                    val parsedDate = dateFormat.parse(validUntil.trim())
                    if (parsedDate != null) {
                        val timestamp = Timestamp(parsedDate)
                        android.util.Log.d("AddBemViewModel", "Successfully parsed validUntil to Timestamp: ${timestamp.toDate()}")
                        timestamp
                    } else {
                        android.util.Log.w("AddBemViewModel", "Failed to parse validUntil date: $validUntil - parsedDate is null")
                        null
                    }
                } else {
                    android.util.Log.d("AddBemViewModel", "validUntil is empty, setting to null")
                    null
                }
            } catch (e: Exception) {
                android.util.Log.e("AddBemViewModel", "Error parsing validUntil date: $validUntil", e)
                e.printStackTrace()
                null
            }
            
            // Se goodTypeId for um nome (fallback), tentar encontrar o ID real no Firestore
            var finalGoodTypeId = bem.goodTypeId
            if (finalGoodTypeId.isNotEmpty() && !finalGoodTypeId.contains("/") && _goodTypes.value.isEmpty()) {
                // Se goodTypes ainda não carregou, tentar carregar agora
                try {
                    val goodTypes = firestoreDataSource.getAllGoodTypes()
                    _goodTypes.value = goodTypes
                    val foundType = goodTypes.find { 
                        it.name.equals(bem.goodTypeId, ignoreCase = true) 
                    }
                    if (foundType != null) {
                        finalGoodTypeId = foundType.id
                    } else {
                        // Se não encontrar, deixa vazio e usa só o category
                        finalGoodTypeId = ""
                    }
                } catch (e: Exception) {
                    // Se falhar, deixa vazio
                    finalGoodTypeId = ""
                }
            }
            
            // Se não tiver statusId, tentar encontrar "Disponível"
            var finalStatusId = bem.statusId
            if (finalStatusId.isEmpty()) {
                if (_inventoryStatuses.value.isEmpty()) {
                    // Se ainda não carregou, tentar carregar agora
                    try {
                        val statuses = firestoreDataSource.getAllInventoryStatuses()
                        _inventoryStatuses.value = statuses
                        finalStatusId = statuses.find { 
                            it.name.equals("Disponível", ignoreCase = true) 
                        }?.id ?: ""
                    } catch (e: Exception) {
                        // Se falhar, deixa vazio
                    }
                } else {
                    finalStatusId = _inventoryStatuses.value.find { 
                        it.name.equals("Disponível", ignoreCase = true) 
                    }?.id ?: ""
                }
            }
            
            val bemWithDates = bem.copy(
                createdAt = Timestamp.now(),
                entryDate = entryTimestamp,
                validUntil = validTimestamp,
                goodTypeId = finalGoodTypeId,
                statusId = finalStatusId
            )
            
            android.util.Log.d("AddBemViewModel", "Creating Bem with validUntil: ${bemWithDates.validUntil?.toDate() ?: "null"}")
            bemRepository.createBem(bemWithDates)
        }
    }
}
