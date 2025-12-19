package com.grupo3.sasocial.presentation.beneficiario

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo3.sasocial.domain.model.Pedido
import com.grupo3.sasocial.presentation.components.BottomNavBar
import com.grupo3.sasocial.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BeneficiarioPedidosView(
    beneficiarioEmail: String,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: BeneficiarioPedidosViewModel = viewModel { 
        BeneficiarioPedidosViewModel(context.applicationContext as Application, beneficiarioEmail) 
    }
    
    val pedidos by viewModel.pedidos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Log para debug
    LaunchedEffect(pedidos) {
        android.util.Log.d("BeneficiarioPedidosView", "=== PEDIDOS ATUALIZADOS ===")
        android.util.Log.d("BeneficiarioPedidosView", "Total de pedidos: ${pedidos.size}")
        android.util.Log.d("BeneficiarioPedidosView", "Email do beneficiário: '$beneficiarioEmail'")
        pedidos.forEach { pedido ->
            android.util.Log.d("BeneficiarioPedidosView", "  - ID=${pedido.id}, Email='${pedido.beneficiarioEmail}', Status=${pedido.status}, Items=${pedido.items.size}")
        }
    }
    
    // Estatísticas
    val pedidosPendentes = pedidos.count { it.status == "PENDENTE" }
    val pedidosAprovados = pedidos.count { it.status == "APROVADO" }
    val pedidosRejeitados = pedidos.count { it.status == "REJEITADO" }
    val pedidosEntregues = pedidos.count { it.status == "ENTREGUE" }
    val totalPedidos = pedidos.size
    
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Meus Pedidos",
                color = SASWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            FloatingActionButton(
                onClick = { onNavigate("criarPedido") },
                modifier = Modifier.size(40.dp),
                containerColor = SASWhite,
                contentColor = SASGreen
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Pedido")
            }
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
                // Cards de Estatísticas
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Total",
                            value = "$totalPedidos",
                            subtitle = "Pedidos",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Pendentes",
                            value = "$pedidosPendentes",
                            subtitle = "Aguardando",
                            valueColor = SASOrange,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Aprovados",
                            value = "$pedidosAprovados",
                            subtitle = "Confirmados",
                            valueColor = SASGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Segunda linha de estatísticas
                if (pedidosRejeitados > 0 || pedidosEntregues > 0) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (pedidosRejeitados > 0) {
                                StatCard(
                                    title = "Rejeitados",
                                    value = "$pedidosRejeitados",
                                    subtitle = "Não aprovados",
                                    valueColor = SASRed,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pedidosEntregues > 0) {
                                StatCard(
                                    title = "Entregues",
                                    value = "$pedidosEntregues",
                                    subtitle = "Completos",
                                    valueColor = Color(0xFF2196F3),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                
                // Título da lista
                item {
                    Text(
                        text = "Histórico de Pedidos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SASGreenDark,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                if (pedidos.isEmpty()) {
                    item {
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
                                    text = "Sem pedidos",
                                    fontSize = 16.sp,
                                    color = SASGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Crie um novo pedido para solicitar produtos",
                                    fontSize = 12.sp,
                                    color = SASGray
                                )
                            }
                        }
                    }
                } else {
                    items(pedidos) { pedido ->
                        PedidoCard(pedido = pedido)
                    }
                }
            }
        }
        
        BottomNavBar(
            currentRoute = "beneficiarioPedidos",
            onNavigate = onNavigate,
            isBeneficiario = true
        )
    }
}

@Composable
fun PedidoCard(pedido: Pedido) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val statusColor = when (pedido.status) {
        "APROVADO" -> SASGreen
        "REJEITADO" -> SASRed
        "ENTREGUE" -> Color(0xFF2196F3)
        else -> SASOrange
    }
    
    val statusText = when (pedido.status) {
        "PENDENTE" -> "Pendente"
        "APROVADO" -> "Aprovado"
        "REJEITADO" -> "Rejeitado"
        "ENTREGUE" -> "Entregue"
        else -> pedido.status
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = "Pedido #${pedido.id.take(8)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SASGreenDark
                )
                
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Data de criação
            pedido.createdAt?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 Criado em: ${dateFormat.format(it.toDate())}",
                        fontSize = 12.sp,
                        color = SASGray
                    )
                }
            }
            
            // Data de aprovação (se aplicável)
            pedido.dataAprovacao?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✅ Aprovado em: ${dateFormat.format(it.toDate())}",
                    fontSize = 12.sp,
                    color = SASGreen
                )
            }
            
            // Data de entrega (se aplicável)
            pedido.dataEntrega?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📦 Entregue em: ${dateFormat.format(it.toDate())}",
                    fontSize = 12.sp,
                    color = Color(0xFF2196F3)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = SASGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Resumo dos itens
            Text(
                text = "Resumo dos Itens (${pedido.items.size} ${if (pedido.items.size == 1) "item" else "itens"}):",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SASGreenDark
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            pedido.items.take(3).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "• ${item.productName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SASGray
                        )
                        Text(
                            text = "   ${item.productCategory}",
                            fontSize = 11.sp,
                            color = SASGray.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = "${item.quantity}x",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SASGreenDark
                    )
                }
            }
            
            // Se houver mais de 3 itens, mostrar resumo
            if (pedido.items.size > 3) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "... e mais ${pedido.items.size - 3} ${if (pedido.items.size - 3 == 1) "item" else "itens"}",
                    fontSize = 11.sp,
                    color = SASGray.copy(alpha = 0.7f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            
            val totalItems = pedido.items.sumOf { it.quantity }
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = SASGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total de unidades:",
                    fontSize = 13.sp,
                    color = SASGray
                )
                Text(
                    text = "$totalItems unidades",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SASGreenDark
                )
            }
            
            if (pedido.observacoes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = SASGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Observações:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SASGray
                )
                Text(
                    text = pedido.observacoes,
                    fontSize = 12.sp,
                    color = SASGray
                )
            }
        }
    }
}

