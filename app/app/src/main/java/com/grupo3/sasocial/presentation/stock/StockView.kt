package com.grupo3.sasocial.presentation.stock

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.grupo3.sasocial.domain.model.Bem
import com.grupo3.sasocial.presentation.components.BottomNavBar
import com.grupo3.sasocial.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockView(
    onNavigate: (String) -> Unit,
    onAddBem: () -> Unit,
    onEditBem: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: StockViewModel = viewModel { StockViewModel(context.applicationContext as Application) }
    val bens by viewModel.bens.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<Bem?>(null) }
    var showDarBaixaDialog by remember { mutableStateOf<Bem?>(null) }
    var darBaixaQuantity by remember { mutableStateOf("1") }
    var darBaixaNotes by remember { mutableStateOf("") }
    var selectedBeneficiario by remember { mutableStateOf<com.grupo3.sasocial.domain.model.Beneficiario?>(null) }
    var beneficiarioExpanded by remember { mutableStateOf(false) }
    
    val filteredBens = bens.filter { 
        searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) 
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SASBackground)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SASGreen)
                .padding(16.dp)
        ) {
            Text(
                text = "Stock",
                color = SASWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Pesquisar") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SASGreen,
                    unfocusedBorderColor = SASGray
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }
        
        // Add button
        Button(
            onClick = onAddBem,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SASGreen),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Adicionar Bem")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredBens) { bem ->
                BemCard(
                    bem = bem,
                    onDarBaixa = { showDarBaixaDialog = bem },
                    onEdit = { onEditBem(bem.id) },
                    onDelete = { showDeleteDialog = bem }
                )
            }
            
            if (filteredBens.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum bem encontrado",
                            color = SASGray
                        )
                    }
                }
            }
        }
        
        BottomNavBar(
            currentRoute = "stock",
            onNavigate = onNavigate
        )
    }
    
    // Delete Dialog
    showDeleteDialog?.let { bem ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Excluir Produto") },
            text = { Text("Tem certeza que deseja excluir \"${bem.name}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBem(bem.id)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SASRed)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Dar Baixa Dialog
    showDarBaixaDialog?.let { bem ->
        val beneficiariosParaCategoria = viewModel.getBeneficiariosByCategory(bem.category)
        val stockAposEntrega = bem.quantity - (darBaixaQuantity.toIntOrNull() ?: 0)
        
        AlertDialog(
            onDismissRequest = { showDarBaixaDialog = null },
            title = { Text("Dar Baixa no Stock - ${bem.name}") },
            text = {
                Column {
                    // Stock info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Stock Atual", fontSize = 12.sp, color = SASGray)
                            Text("${bem.quantity}", fontWeight = FontWeight.Bold, color = SASGreenDark)
                        }
                        Column {
                            Text("Quantidade a Entregar *", fontSize = 12.sp, color = SASGray)
                            OutlinedTextField(
                                value = darBaixaQuantity,
                                onValueChange = { darBaixaQuantity = it },
                                singleLine = true,
                                modifier = Modifier.width(100.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SASGreen,
                                    unfocusedBorderColor = SASGray
                                )
                            )
                        }
                    }
                    
                    Text(
                        text = "Stock após entrega: $stockAposEntrega",
                        fontSize = 12.sp,
                        color = SASGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Beneficiário dropdown
                    Text("Beneficiário *", fontSize = 12.sp, color = SASGray)
                    
                    if (beneficiariosParaCategoria.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
                        ) {
                            Text(
                                text = "Não há beneficiários aceites para a categoria \"${bem.category}\". Certifique-se de que existem candidaturas aceites nesta categoria.",
                                color = Color(0xFF856404),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = beneficiarioExpanded,
                            onExpandedChange = { beneficiarioExpanded = !beneficiarioExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedBeneficiario?.let { "${it.nome} (${it.email})" } ?: "Selecione um beneficiário...",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = beneficiarioExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SASGreen,
                                    unfocusedBorderColor = SASGray
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = beneficiarioExpanded,
                                onDismissRequest = { beneficiarioExpanded = false }
                            ) {
                                beneficiariosParaCategoria.forEach { beneficiario ->
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(beneficiario.nome, fontWeight = FontWeight.Medium)
                                                Text(beneficiario.email, fontSize = 12.sp, color = SASGray)
                                            }
                                        },
                                        onClick = {
                                            selectedBeneficiario = beneficiario
                                            beneficiarioExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = "Apenas beneficiários aceites na categoria \"${bem.category}\" são mostrados.",
                            fontSize = 10.sp,
                            color = SASGray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Observações
                    Text("Observações (opcional)", fontSize = 12.sp, color = SASGray)
                    OutlinedTextField(
                        value = darBaixaNotes,
                        onValueChange = { darBaixaNotes = it },
                        placeholder = { Text("Adicione observações sobre esta entrega...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SASGreen,
                            unfocusedBorderColor = SASGray
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = darBaixaQuantity.toIntOrNull() ?: 0
                        if (qty > 0 && qty <= bem.quantity && selectedBeneficiario != null) {
                            viewModel.darBaixa(
                                bem = bem,
                                quantidade = qty,
                                beneficiaryName = selectedBeneficiario!!.nome,
                                beneficiaryEmail = selectedBeneficiario!!.email,
                                beneficiaryId = selectedBeneficiario!!.id,
                                notes = darBaixaNotes
                            )
                            showDarBaixaDialog = null
                            darBaixaQuantity = "1"
                            darBaixaNotes = ""
                            selectedBeneficiario = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SASGreen),
                    enabled = selectedBeneficiario != null && (darBaixaQuantity.toIntOrNull() ?: 0) > 0
                ) {
                    Text("Confirmar Baixa")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { 
                    showDarBaixaDialog = null
                    darBaixaQuantity = "1"
                    darBaixaNotes = ""
                    selectedBeneficiario = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun BemCard(
    bem: Bem,
    onDarBaixa: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val estado = if (bem.quantity <= bem.minStock) "Baixo" else "OK"
    val estadoColor = if (bem.quantity <= bem.minStock) SASRed else SASGreenApproved
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SASWhite),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Nome + Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bem.name,
                    color = SASGreenDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Surface(
                    color = estadoColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = estado,
                        color = estadoColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Info rows
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Categoria", fontSize = 10.sp, color = SASGray)
                    Text(bem.category, fontSize = 13.sp, color = SASGreenDark)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fornecedor", fontSize = 10.sp, color = SASGray)
                    Text(bem.supplier.ifEmpty { "-" }, fontSize = 13.sp, color = SASGreenDark)
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Quantidade", fontSize = 10.sp, color = SASGray)
                    Text("${bem.quantity}", fontSize = 13.sp, color = SASGreenDark, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Stock Mínimo", fontSize = 10.sp, color = SASGray)
                    Text("${bem.minStock}", fontSize = 13.sp, color = SASGray)
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Datas
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Data de Entrada", fontSize = 10.sp, color = SASGray)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val entryDateText = bem.entryDate?.toDate()?.let { dateFormat.format(it) } ?: "-"
                    Text(entryDateText, fontSize = 13.sp, color = SASGray)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Data de Validade", fontSize = 10.sp, color = SASGray)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val validUntilText = bem.validUntil?.toDate()?.let { dateFormat.format(it) } ?: "-"
                    Text(
                        validUntilText, 
                        fontSize = 13.sp, 
                        color = if (bem.validUntil != null) {
                            // Verifica se está expirada
                            val now = System.currentTimeMillis()
                            val validUntilMillis = bem.validUntil.toDate().time
                            if (validUntilMillis < now) SASRed else SASGreenDark
                        } else {
                            SASGray
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDarBaixa,
                    colors = ButtonDefaults.buttonColors(containerColor = SASGreen),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("Dar Baixa", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("Editar", fontSize = 12.sp, color = SASGreenDark)
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SASRed),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("Excluir", fontSize = 12.sp)
                }
            }
        }
    }
}
