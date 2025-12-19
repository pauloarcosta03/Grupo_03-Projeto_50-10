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
import com.grupo3.sasocial.domain.model.Bem
import com.grupo3.sasocial.presentation.components.BottomNavBar
import com.grupo3.sasocial.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBemView(
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AddBemViewModel = viewModel { AddBemViewModel(context.applicationContext as Application) }
    
    val goodTypes by viewModel.goodTypes.collectAsState()
    val inventoryStatuses by viewModel.inventoryStatuses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
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
    
    // Debug: Log quando goodTypes carregam
    LaunchedEffect(goodTypes) {
        android.util.Log.d("AddBemView", "goodTypes updated: ${goodTypes.size} items")
        goodTypes.forEach { 
            android.util.Log.d("AddBemView", "  - ${it.id}: ${it.name}")
        }
    }
    
    // Definir status padrão como "Disponível" se disponível
    LaunchedEffect(inventoryStatuses) {
        if (selectedStatusId.isEmpty() && inventoryStatuses.isNotEmpty()) {
            val disponivel = inventoryStatuses.find { 
                it.name.equals("Disponível", ignoreCase = true) 
            }
            if (disponivel != null) {
                selectedStatusId = disponivel.id
            }
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
                text = "Adicionar Bem",
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
                placeholder = { Text("Ex: Arroz") },
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
                // Mostra o nome selecionado ou "Selecione..."
                val displayValue = if (selectedGoodTypeName.isNotEmpty()) {
                    selectedGoodTypeName
                } else {
                    val selectedGoodType = goodTypes.find { it.id == selectedGoodTypeId }
                    selectedGoodType?.name ?: "Selecione..."
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
                    if (isLoading && goodTypes.isEmpty()) {
                        // Se ainda está a carregar, mostra mensagem
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "A carregar categorias...", 
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SASGray
                                ) 
                            },
                            onClick = { categoryExpanded = false },
                            enabled = false
                        )
                    } else if (goodTypes.isEmpty()) {
                        // Se não carregou nada, mostra mensagem de erro
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "Erro ao carregar categorias", 
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SASRed
                                ) 
                            },
                            onClick = { categoryExpanded = false },
                            enabled = false
                        )
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
                    placeholder = { Text("0") },
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
                    placeholder = { Text("0") },
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
                placeholder = { Text("Nome ou identificação do fornecedor") },
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
                placeholder = { Text("dd/mm/aaaa (ex: 31/12/2025)") },
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
                        }
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
                        // Usa o nome guardado ou busca do Firestore
                        val categoryName = if (selectedGoodTypeName.isNotEmpty()) {
                            selectedGoodTypeName
                        } else {
                            val selectedGoodType = goodTypes.find { it.id == selectedGoodTypeId }
                            selectedGoodType?.name ?: selectedGoodTypeId // Fallback se não encontrar
                        }
                        
                        viewModel.createBem(
                            Bem(
                                name = name,
                                category = categoryName, // Mantém para compatibilidade
                                goodTypeId = selectedGoodTypeId,
                                quantity = quantity.toIntOrNull() ?: 0,
                                minStock = minStock.toIntOrNull() ?: 0,
                                supplier = supplier,
                                statusId = selectedStatusId
                            ),
                            entryDate = entryDate,
                            validUntil = validUntil
                        )
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SASGreen),
                    shape = RoundedCornerShape(8.dp),
                    enabled = name.isNotEmpty() && 
                             selectedGoodTypeId.isNotEmpty() && 
                             quantity.isNotEmpty() &&
                             goodTypes.isNotEmpty() // Só permite guardar se os dados carregaram
                ) {
                    Text("Adicionar Produto", fontWeight = FontWeight.Bold)
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
