package com.grupo3.sasocial.presentation.pedidos

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo3.sasocial.domain.model.Pedido
import com.grupo3.sasocial.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoDetailView(
    pedidoId: String,
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: PedidoDetailViewModel = viewModel { PedidoDetailViewModel(application) }
    
    val pedido by viewModel.pedido.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val approvalResult by viewModel.approvalResult.collectAsState()
    
    // Estados para controlar processamento e erros
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(pedidoId) {
        viewModel.loadPedido(pedidoId)
        viewModel.loadBens()
    }
    
    LaunchedEffect(approvalResult) {
        approvalResult?.onSuccess {
            isProcessing = false
            onNavigateBack()
        }
        approvalResult?.onFailure {
            isProcessing = false
            errorMessage = it.message
        }
    }
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Pedido", color = SASWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SASGreen),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = SASWhite
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SASGreen)
            }
        } else if (pedido == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pedido não encontrado",
                    color = SASGray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SASBackground)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Informações do Beneficiário
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SASWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Beneficiário",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SASGreenDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nome: ${pedido!!.beneficiarioNome}",
                                fontSize = 14.sp,
                                color = SASGray
                            )
                            Text(
                                text = "Email: ${pedido!!.beneficiarioEmail}",
                                fontSize = 14.sp,
                                color = SASGray
                            )
                        }
                    }
                }
                
                // Status
                item {
                    val statusColor = when (pedido!!.status) {
                        "PENDENTE" -> SASOrange
                        "APROVADO" -> SASGreenApproved
                        "REJEITADO" -> SASRed
                        "ENTREGUE" -> SASGreenDark
                        else -> SASGray
                    }
                    
                    val statusText = when (pedido!!.status) {
                        "PENDENTE" -> "Pendente"
                        "APROVADO" -> "Aprovado"
                        "REJEITADO" -> "Rejeitado"
                        "ENTREGUE" -> "Entregue"
                        else -> pedido!!.status
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = statusColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Status: $statusText",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SASWhite,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                
                // Itens do Pedido
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SASWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Itens do Pedido (${pedido!!.items.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SASGreenDark
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            pedido!!.items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.productName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SASGreenDark
                                        )
                                        Text(
                                            text = item.productCategory,
                                            fontSize = 12.sp,
                                            color = SASGray
                                        )
                                    }
                                    Text(
                                        text = "${item.quantity}x",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SASGreenDark
                                    )
                                }
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
                
                // Observações
                if (pedido!!.observacoes.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SASWhite),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Observações",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SASGreenDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = pedido!!.observacoes,
                                    fontSize = 14.sp,
                                    color = SASGray
                                )
                            }
                        }
                    }
                }
                
                // Informações de Data
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SASWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Informações",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SASGreenDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Criado: ${pedido!!.createdAt?.let { dateFormat.format(it.toDate()) } ?: "N/A"}",
                                fontSize = 14.sp,
                                color = SASGray
                            )
                            pedido!!.dataAprovacao?.let {
                                Text(
                                    text = "Aprovado: ${dateFormat.format(it.toDate())}",
                                    fontSize = 14.sp,
                                    color = SASGray
                                )
                            }
                            pedido!!.dataEntrega?.let {
                                Text(
                                    text = "Entregue: ${dateFormat.format(it.toDate())}",
                                    fontSize = 14.sp,
                                    color = SASGray
                                )
                            }
                        }
                    }
                }
                
                // Mostrar erro se houver
                errorMessage?.let { error ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SASRed.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = error,
                                color = SASRed,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                
                // Botões de Ação
                if (pedido!!.status == "PENDENTE") {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (!isProcessing) {
                                        isProcessing = true
                                        errorMessage = null
                                        viewModel.rejeitarPedido(
                                            pedidoId = pedidoId,
                                            onSuccess = { },
                                            onError = { }
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SASRed),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isProcessing
                            ) {
                                Text("Rejeitar", fontWeight = FontWeight.Bold)
                            }
                            
                            Button(
                                onClick = {
                                    if (!isProcessing) {
                                        isProcessing = true
                                        errorMessage = null
                                        viewModel.aprovarPedido(
                                            pedidoId = pedidoId,
                                            onSuccess = { },
                                            onError = { }
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SASGreen),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isProcessing
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = SASWhite,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Aprovar e Dar Baixa", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else if (pedido!!.status == "APROVADO") {
                    item {
                        Button(
                            onClick = {
                                viewModel.marcarComoEntregue(
                                    pedidoId = pedidoId,
                                    onSuccess = { },
                                    onError = { }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SASGreenDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Marcar como Entregue", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

