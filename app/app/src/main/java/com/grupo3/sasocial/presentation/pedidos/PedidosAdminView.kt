package com.grupo3.sasocial.presentation.pedidos

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.grupo3.sasocial.presentation.components.BottomNavBar
import com.grupo3.sasocial.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosAdminView(
    onNavigate: (String) -> Unit,
    onPedidoClick: (String) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: PedidosAdminViewModel = viewModel { PedidosAdminViewModel(application) }
    
    val pedidos by viewModel.pedidos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var selectedFilter by remember { mutableStateOf("TODOS") } // TODOS, PENDENTE, APROVADO, REJEITADO, ENTREGUE
    
    // Log para debug
    LaunchedEffect(pedidos) {
        android.util.Log.d("PedidosAdminView", "=== PEDIDOS RECEBIDOS ===")
        android.util.Log.d("PedidosAdminView", "Total de pedidos: ${pedidos.size}")
        pedidos.forEach { pedido ->
            android.util.Log.d("PedidosAdminView", "  - ID=${pedido.id}, Email='${pedido.beneficiarioEmail}', Nome='${pedido.beneficiarioNome}', Status=${pedido.status}")
        }
    }
    
    val filteredPedidos = remember(pedidos, selectedFilter) {
        val filtered = when (selectedFilter) {
            "PENDENTE" -> viewModel.getPedidosPendentes()
            "APROVADO" -> viewModel.getPedidosAprovados()
            "REJEITADO" -> viewModel.getPedidosRejeitados()
            "ENTREGUE" -> viewModel.getPedidosEntregues()
            else -> pedidos
        }
        android.util.Log.d("PedidosAdminView", "Filtro: $selectedFilter, Pedidos filtrados: ${filtered.size} de ${pedidos.size}")
        filtered
    }
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = "pedidosAdmin",
                onNavigate = onNavigate,
                isBeneficiario = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SASBackground)
                .padding(paddingValues)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SASGreen)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Gestão de Pedidos",
                    color = SASWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Filtros
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "TODOS",
                    onClick = { selectedFilter = "TODOS" },
                    label = { Text("Todos (${pedidos.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SASGreen,
                        selectedLabelColor = SASWhite
                    )
                )
                FilterChip(
                    selected = selectedFilter == "PENDENTE",
                    onClick = { selectedFilter = "PENDENTE" },
                    label = { Text("Pendentes (${viewModel.getPedidosPendentes().size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SASOrange,
                        selectedLabelColor = SASWhite
                    )
                )
                FilterChip(
                    selected = selectedFilter == "APROVADO",
                    onClick = { selectedFilter = "APROVADO" },
                    label = { Text("Aprovados (${viewModel.getPedidosAprovados().size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SASGreenApproved,
                        selectedLabelColor = SASWhite
                    )
                )
            }
            
            // Lista de Pedidos
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SASGreen)
                }
            } else if (filteredPedidos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum pedido encontrado",
                        color = SASGray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPedidos) { pedido ->
                        PedidoCard(
                            pedido = pedido,
                            dateFormat = dateFormat,
                            onClick = {
                                onPedidoClick(pedido.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PedidoCard(
    pedido: Pedido,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    val statusColor = when (pedido.status) {
        "PENDENTE" -> SASOrange
        "APROVADO" -> SASGreenApproved
        "REJEITADO" -> SASRed
        "ENTREGUE" -> SASGreenDark
        else -> SASGray
    }
    
    val statusText = when (pedido.status) {
        "PENDENTE" -> "Pendente"
        "APROVADO" -> "Aprovado"
        "REJEITADO" -> "Rejeitado"
        "ENTREGUE" -> "Entregue"
        else -> pedido.status
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SASWhite),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pedido.beneficiarioNome,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SASGreenDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pedido.beneficiarioEmail,
                        fontSize = 14.sp,
                        color = SASGray
                    )
                }
                
                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = statusText,
                        color = SASWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Itens: ${pedido.items.size}",
                fontSize = 14.sp,
                color = SASGray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            pedido.items.take(3).forEach { item ->
                Text(
                    text = "• ${item.productName} (${item.quantity}x)",
                    fontSize = 13.sp,
                    color = SASGray,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                )
            }
            
            if (pedido.items.size > 3) {
                Text(
                    text = "... e mais ${pedido.items.size - 3} itens",
                    fontSize = 12.sp,
                    color = SASGray,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Criado: ${pedido.createdAt?.let { dateFormat.format(it.toDate()) } ?: "N/A"}",
                    fontSize = 12.sp,
                    color = SASGray
                )
            }
            
            if (pedido.observacoes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Observações: ${pedido.observacoes}",
                    fontSize = 12.sp,
                    color = SASGray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

