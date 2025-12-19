package com.grupo3.sasocial.presentation.beneficiario

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.grupo3.sasocial.domain.model.Bem
import com.grupo3.sasocial.domain.model.PedidoItem
import com.grupo3.sasocial.ui.theme.*
import java.util.*
import android.widget.Toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarPedidoView(
    beneficiarioId: String,
    beneficiarioEmail: String,
    beneficiarioNome: String,
    categoriasAceites: List<String>,
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: CriarPedidoViewModel = viewModel { 
        CriarPedidoViewModel(context.applicationContext as Application, categoriasAceites) 
    }
    
    // Log para debug
    LaunchedEffect(beneficiarioEmail) {
        android.util.Log.d("CriarPedidoView", "=== CRIAR PEDIDO VIEW ===")
        android.util.Log.d("CriarPedidoView", "Email do beneficiário recebido: '$beneficiarioEmail' (trimmed: '${beneficiarioEmail.trim().lowercase()}', length=${beneficiarioEmail.length})")
        android.util.Log.d("CriarPedidoView", "ID do beneficiário: '$beneficiarioId'")
        android.util.Log.d("CriarPedidoView", "Nome do beneficiário: '$beneficiarioNome'")
        
        if (beneficiarioEmail.isBlank()) {
            android.util.Log.e("CriarPedidoView", "❌ ERRO: Email está vazio!")
        }
    }
    
    val bens by viewModel.bens.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var selectedItems by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var observacoes by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SASBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SASGreen)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = SASWhite)
            }
            Text(
                text = "Criar Pedido",
                color = SASWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SASGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Selecione os produtos que deseja solicitar:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = SASGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                item {
                    if (bens.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SASWhite),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Sem produtos disponíveis",
                                    fontSize = 16.sp,
                                    color = SASGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Não há produtos em stock nas suas categorias aceites",
                                    fontSize = 12.sp,
                                    color = SASGray
                                )
                            }
                        }
                    }
                }
                
                items(bens) { bem ->
                    ProductSelectionCard(
                        bem = bem,
                        quantity = selectedItems[bem.id] ?: 0,
                        onQuantityChange = { newQuantity ->
                            selectedItems = if (newQuantity > 0) {
                                selectedItems + (bem.id to newQuantity)
                            } else {
                                selectedItems - bem.id
                            }
                        }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = observacoes,
                        onValueChange = { observacoes = it },
                        label = { Text("Observações (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SASGreen,
                            unfocusedBorderColor = SASGray
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SASGreen),
                        shape = RoundedCornerShape(8.dp),
                        enabled = selectedItems.isNotEmpty() && !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = SASWhite
                            )
                        } else {
                            Text(
                                text = "Enviar Pedido (${selectedItems.values.sum()} itens)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Dialog de confirmação
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar Pedido", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Deseja enviar este pedido?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total de itens: ${selectedItems.values.sum()}",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        isSubmitting = true
                        android.util.Log.d("CriarPedidoView", "=== SUBMETENDO PEDIDO ===")
                        android.util.Log.d("CriarPedidoView", "Email a enviar: '$beneficiarioEmail'")
                        android.util.Log.d("CriarPedidoView", "ID a enviar: '$beneficiarioId'")
                        android.util.Log.d("CriarPedidoView", "Nome a enviar: '$beneficiarioNome'")
                        android.util.Log.d("CriarPedidoView", "Items selecionados: ${selectedItems.size}")
                        
                        viewModel.createPedido(
                            beneficiarioId = beneficiarioId,
                            beneficiarioEmail = beneficiarioEmail,
                            beneficiarioNome = beneficiarioNome,
                            selectedItems = selectedItems,
                            bens = bens,
                            observacoes = observacoes,
                            onSuccess = {
                                isSubmitting = false
                                android.util.Log.d("CriarPedidoView", "✅ Pedido criado com sucesso, navegando para dashboard")
                                Toast.makeText(
                                    context,
                                    "Pedido enviado com sucesso!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Aguardar um pouco para garantir que o listener recebeu a atualização
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(500)
                                    // Navegar imediatamente - o Flow vai atualizar automaticamente
                                    // O addSnapshotListener no Firestore vai detectar a mudança
                                    onNavigate("beneficiarioDashboard")
                                }
                            },
                            onError = { errorMessage ->
                                isSubmitting = false
                                Toast.makeText(
                                    context,
                                    "Erro ao enviar pedido: $errorMessage",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ProductSelectionCard(
    bem: Bem,
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SASWhite),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = bem.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SASGreenDark
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${bem.category} • Stock: ${bem.quantity}",
                fontSize = 12.sp,
                color = SASGray
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 0) onQuantityChange(quantity - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                    }
                    
                    Text(
                        text = "$quantity",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    IconButton(
                        onClick = { 
                            if (quantity < bem.quantity) {
                                onQuantityChange(quantity + 1)
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        enabled = quantity < bem.quantity
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar")
                    }
                }
                
                if (quantity > 0) {
                    Text(
                        text = "Selecionado",
                        fontSize = 12.sp,
                        color = SASGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

