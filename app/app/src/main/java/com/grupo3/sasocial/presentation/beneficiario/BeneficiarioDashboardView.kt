package com.grupo3.sasocial.presentation.beneficiario

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.grupo3.sasocial.domain.model.Beneficiario
import com.grupo3.sasocial.presentation.components.BottomNavBar
import com.grupo3.sasocial.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BeneficiarioDashboardView(
    beneficiario: Beneficiario,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    // Usar key para manter o ViewModel quando navegar de volta
    val viewModel: BeneficiarioDashboardViewModel = viewModel(
        key = "beneficiario_dashboard_${beneficiario.email}"
    ) { 
        BeneficiarioDashboardViewModel(context.applicationContext as Application, beneficiario.email) 
    }
    
    // Log para debug
    LaunchedEffect(beneficiario.email) {
        android.util.Log.d("BeneficiarioDashboardView", "Email do beneficiário: ${beneficiario.email}")
    }
    
    // Forçar atualização quando a view aparece
    LaunchedEffect(Unit) {
        // O Flow já está a escutar mudanças, mas garantimos que está ativo
        android.util.Log.d("BeneficiarioDashboard", "View apareceu, aguardando atualizações do Flow")
    }
    
    val pedidos by viewModel.pedidos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Estatísticas calculadas - atualizam automaticamente quando pedidos mudam
    val pedidosPendentes = remember(pedidos) { pedidos.count { it.status == "PENDENTE" } }
    val pedidosAprovados = remember(pedidos) { pedidos.count { it.status == "APROVADO" } }
    val totalPedidos = remember(pedidos) { pedidos.size }
    
    // Log para debug das estatísticas
    LaunchedEffect(pedidos) {
        android.util.Log.d("BeneficiarioDashboardView", "=== ESTATÍSTICAS ATUALIZADAS ===")
        android.util.Log.d("BeneficiarioDashboardView", "Total de pedidos: $totalPedidos")
        android.util.Log.d("BeneficiarioDashboardView", "Pedidos pendentes: $pedidosPendentes")
        android.util.Log.d("BeneficiarioDashboardView", "Pedidos aprovados: $pedidosAprovados")
        android.util.Log.d("BeneficiarioDashboardView", "Lista completa de pedidos:")
        pedidos.forEach { pedido ->
            android.util.Log.d("BeneficiarioDashboardView", "  - ID=${pedido.id}, Email='${pedido.beneficiarioEmail}', Status=${pedido.status}, Items=${pedido.items.size}")
        }
    }
    
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
            Column {
                Text(
                    text = "Bem-vindo, ${beneficiario.nome}",
                    color = SASWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = beneficiario.email,
                    color = SASWhite.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
            
            TextButton(onClick = onLogout) {
                Text("Sair", color = SASWhite)
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cards de estatísticas
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Pedidos",
                            value = "$totalPedidos",
                            subtitle = "Total",
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
                
                // Informações da candidatura
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SASWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Categorias Aceites",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SASGreenDark
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Mostrar apenas as categorias aceites (marcadas como true)
                            val categoriasAceites = mutableListOf<String>()
                            if (beneficiario.produtosAlimentares) categoriasAceites.add("Alimentos")
                            if (beneficiario.produtosHigienePessoal) categoriasAceites.add("Higiene Pessoal")
                            if (beneficiario.produtosLimpeza) categoriasAceites.add("Limpeza")
                            if (beneficiario.outros) categoriasAceites.add("Outros")
                            
                            if (categoriasAceites.isEmpty()) {
                                Text(
                                    text = "Nenhuma categoria aceite",
                                    fontSize = 14.sp,
                                    color = SASGray,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            } else {
                                categoriasAceites.forEach { categoria ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "• $categoria",
                                            fontSize = 14.sp,
                                            color = SASGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Pedidos recentes
                item {
                    Text(
                        text = "Pedidos Recentes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SASGreenDark,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                item {
                    if (pedidos.isEmpty()) {
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
                                    text = "Ainda não fez nenhum pedido",
                                    fontSize = 14.sp,
                                    color = SASGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onNavigate("criarPedido") },
                                    colors = ButtonDefaults.buttonColors(containerColor = SASGreen)
                                ) {
                                    Text("Criar Primeiro Pedido")
                                }
                            }
                        }
                    }
                }
                
                val pedidosRecentes = pedidos.take(5)
                items(pedidosRecentes) { pedido ->
                    PedidoResumoCard(pedido = pedido)
                }
                
                if (pedidos.size > 5) {
                    item {
                        TextButton(
                            onClick = { onNavigate("beneficiarioPedidos") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ver todos os pedidos")
                        }
                    }
                }
            }
        }
        
        BottomNavBar(
            currentRoute = "beneficiarioDashboard",
            onNavigate = onNavigate,
            isBeneficiario = true
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = SASGreenDark
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SASWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = SASGray
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = SASGray.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PedidoResumoCard(pedido: com.grupo3.sasocial.domain.model.Pedido) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pedido #${pedido.id.take(8)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                pedido.createdAt?.let {
                    Text(
                        text = dateFormat.format(it.toDate()),
                        fontSize = 12.sp,
                        color = SASGray
                    )
                }
                Text(
                    text = "${pedido.items.size} itens",
                    fontSize = 12.sp,
                    color = SASGray
                )
            }
            
            Surface(
                color = statusColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

