package com.grupo3.sasocial.presentation.stock

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo3.sasocial.presentation.components.BottomNavBar
import com.grupo3.sasocial.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBemView(
    bemId: String,
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: EditBemViewModel = viewModel { 
        EditBemViewModel(context.applicationContext as Application, bemId) 
    }
    val bem by viewModel.bem.collectAsState()
    val goodTypes by viewModel.goodTypes.collectAsState()
    val inventoryStatuses by viewModel.inventoryStatuses.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var selectedGoodTypeId by remember { mutableStateOf("") }
    var selectedGoodTypeName by remember { mutableStateOf("") } // Guarda o nome para mostrar
    var selectedStatusId by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var entryDate by remember { mutableStateOf("") }
    var validUntil by remember { mutableStateOf("") }
    
    var categoryExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    
    val fallbackCategories = listOf("Alimentos", "Higiene", "Vestuário", "Limpeza", "Outros")
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    LaunchedEffect(bem) {
        bem?.let { b ->
            name = b.name
            selectedGoodTypeId = b.goodTypeId.ifEmpty { 
                // Se não tiver goodTypeId, tenta encontrar pelo nome da categoria
                goodTypes.find { it.name.equals(b.category, ignoreCase = true) }?.id ?: ""
            }
            // Guarda o nome da categoria para mostrar
            selectedGoodTypeName = b.category
            selectedStatusId = b.statusId
            quantity = b.quantity.toString()
            minStock = b.minStock.toString()
            supplier = b.supplier
            entryDate = b.entryDate?.toDate()?.let { dateFormat.format(it) } ?: ""
            validUntil = b.validUntil?.toDate()?.let { dateFormat.format(it) } ?: ""
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SASBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SASGreen)
                .padding(16.dp)
        ) {
            Text(
                text = "Editar Produto",
                color = SASWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do Produto *") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SASGreen,
                    unfocusedBorderColor = SASGray
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Categoria dropdown (do Firestore)
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                // Mostra o nome selecionado ou o nome do bem atual
                val displayValue = if (selectedGoodTypeName.isNotEmpty()) {
                    selectedGoodTypeName
                } else {
                    val selectedGoodType = goodTypes.find { it.id == selectedGoodTypeId }
                    selectedGoodType?.name ?: bem?.category ?: "Selecione..."
                }
                
                OutlinedTextField(
                    value = displayValue,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoria *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SASGreen,
                        unfocusedBorderColor = SASGray
                    )
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    if (goodTypes.isEmpty()) {
                        fallbackCategories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedGoodTypeId = option
                                    selectedGoodTypeName = option // Guarda o nome para mostrar
                                    categoryExpanded = false
                                }
                            )
                        }
                    } else {
                        goodTypes.forEach { goodType ->
                            DropdownMenuItem(
                                text = { Text(goodType.name) },
                                onClick = {
                                    selectedGoodTypeId = goodType.id
                                    selectedGoodTypeName = goodType.name // Guarda o nome para mostrar
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status dropdown (do Firestore)
            if (inventoryStatuses.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    val selectedStatus = inventoryStatuses.find { it.id == selectedStatusId }
                    OutlinedTextField(
                        value = selectedStatus?.name ?: "Selecione...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status do Inventário") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SASGreen,
                            unfocusedBorderColor = SASGray
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        inventoryStatuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name) },
                                onClick = {
                                    selectedStatusId = status.id
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantidade em Stock *") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SASGreen,
                        unfocusedBorderColor = SASGray
                    )
                )
                
                OutlinedTextField(
                    value = minStock,
                    onValueChange = { minStock = it },
                    label = { Text("Quantidade Mínima *") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SASGreen,
                        unfocusedBorderColor = SASGray
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = supplier,
                onValueChange = { supplier = it },
                label = { Text("Fornecedor") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SASGreen,
                    unfocusedBorderColor = SASGray
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = entryDate,
                onValueChange = { entryDate = it },
                label = { Text("Data de Entrada") },
                placeholder = { Text("dd/mm/aaaa (ex: 08/07/2025)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SASGreen,
                    unfocusedBorderColor = SASGray
                ),
                supportingText = {
                    if (entryDate.isNotEmpty() && entryDate.trim().isNotEmpty()) {
                        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        val isValid = try {
                            dateFormat.parse(entryDate.trim()) != null
                        } catch (e: Exception) {
                            false
                        }
                        if (!isValid) {
                            Text(
                                "Formato inválido. Use dd/mm/aaaa",
                                color = SASRed,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = validUntil,
                onValueChange = { validUntil = it },
                label = { Text("Data de Validade (opcional)") },
                placeholder = { Text("dd/mm/aaaa (ex: 31/12/2025) ou deixe vazio") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SASGreen,
                    unfocusedBorderColor = SASGray
                ),
                supportingText = {
                    if (validUntil.isNotEmpty() && validUntil.trim().isNotEmpty()) {
                        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        val isValid = try {
                            dateFormat.parse(validUntil.trim()) != null
                        } catch (e: Exception) {
                            false
                        }
                        if (!isValid) {
                            Text(
                                "Formato inválido. Use dd/mm/aaaa",
                                color = SASRed,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                "Deixe vazio para remover a data de validade",
                                color = SASGray,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Text(
                            "Deixe vazio se não houver data de validade",
                            color = SASGray,
                            fontSize = 12.sp
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        bem?.let { b ->
                            // Usa o nome guardado ou busca do Firestore
                            val categoryName = if (selectedGoodTypeName.isNotEmpty()) {
                                selectedGoodTypeName
                            } else {
                                val selectedGoodType = goodTypes.find { it.id == selectedGoodTypeId }
                                selectedGoodType?.name ?: selectedGoodTypeId.ifEmpty { b.category }
                            }
                            
                            viewModel.updateBem(
                                bem = b,
                                name = name,
                                category = categoryName,
                                goodTypeId = selectedGoodTypeId,
                                quantity = quantity.toIntOrNull() ?: b.quantity,
                                minStock = minStock.toIntOrNull() ?: b.minStock,
                                supplier = supplier,
                                statusId = selectedStatusId.ifEmpty { b.statusId },
                                entryDate = entryDate,
                                validUntil = validUntil
                            )
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SASGreen),
                    shape = RoundedCornerShape(8.dp),
                    enabled = name.isNotEmpty() && selectedGoodTypeId.isNotEmpty()
                ) {
                    Text("Atualizar Produto", fontWeight = FontWeight.Bold)
                }
                
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar")
                }
            }
        }
        
        BottomNavBar(
            currentRoute = "stock",
            onNavigate = onNavigate
        )
    }
}
