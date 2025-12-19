package com.grupo3.sasocial.presentation.entregas

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo3.sasocial.domain.model.Entrega
import com.grupo3.sasocial.presentation.components.BottomNavBar
import com.grupo3.sasocial.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoricoEntregasView(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: HistoricoEntregasViewModel = viewModel { 
        HistoricoEntregasViewModel(context.applicationContext as Application) 
    }
    
    val entregas by viewModel.entregas.collectAsState()
    val totalEntregas = viewModel.totalEntregas
    val totalEntregue = viewModel.totalEntregue
    val entregasHoje = viewModel.entregasHoje
    val quantidadeHoje = viewModel.quantidadeHoje
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())
    
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
                text = "Histórico de Entregas",
                color = SASWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Cards de Resumo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total de Entregas",
                    value = totalEntregas.toString(),
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Entregue",
                    value = totalEntregue.toString(),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Hoje (Entregas)",
                    value = entregasHoje.toString(),
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Hoje (Quantidade)",
                    value = quantidadeHoje.toString(),
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tabela
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = SASWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Container com scroll horizontal para toda a tabela
                    val horizontalScrollState = rememberScrollState()
                    
                    Column(
                        modifier = Modifier.horizontalScroll(horizontalScrollState)
                    ) {
                        // Header da Tabela - larguras fixas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            TableHeader("Data/Hora", 120.dp)
                            TableHeader("Produto", 100.dp)
                            TableHeader("Categoria", 90.dp)
                            TableHeader("Beneficiário", 150.dp)
                            TableHeader("Qtd", 50.dp)
                            TableHeader("Stock Antes", 80.dp)
                            TableHeader("Stock Depois", 80.dp)
                            TableHeader("Observações", 120.dp)
                        }
                        
                        Divider(
                            color = SASGray.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Linhas da Tabela - mesma estrutura de larguras
                        if (entregas.isEmpty()) {
                            Text(
                                text = "Sem registos de entregas",
                                color = SASGray,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier.height(400.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                entregas.forEach { entrega ->
                                    EntregaTableRow(
                                        entrega = entrega,
                                        dateFormat = dateFormat
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        BottomNavBar(
            currentRoute = "historicoEntregas",
            onNavigate = onNavigate
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = SASGray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun TableHeader(
    text: String,
    width: androidx.compose.ui.unit.Dp
) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = SASGreenDark,
        modifier = Modifier.width(width)
    )
}

@Composable
fun EntregaTableRow(
    entrega: Entrega,
    dateFormat: SimpleDateFormat
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Data/Hora - largura fixa igual ao header
        Column(
            modifier = Modifier.width(120.dp),
            horizontalAlignment = Alignment.Start
        ) {
            entrega.createdAt?.toDate()?.let { date ->
                Text(
                    text = dateFormat.format(date),
                    fontSize = 10.sp,
                    color = SASGray
                )
            } ?: Text("-", fontSize = 10.sp, color = SASGray)
        }
        
        // Produto - largura fixa igual ao header
        Text(
            text = entrega.productName.ifEmpty { "-" },
            fontSize = 10.sp,
            color = SASGreenDark,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(100.dp)
        )
        
        // Categoria - largura fixa igual ao header
        Text(
            text = entrega.productCategory.ifEmpty { "-" },
            fontSize = 10.sp,
            color = SASGray,
            modifier = Modifier.width(90.dp)
        )
        
        // Beneficiário - largura fixa igual ao header
        Column(
            modifier = Modifier.width(150.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = entrega.beneficiaryName.ifEmpty { "-" },
                fontSize = 10.sp,
                color = SASGreenDark,
                fontWeight = FontWeight.Medium
            )
            if (entrega.beneficiaryEmail.isNotEmpty()) {
                Text(
                    text = entrega.beneficiaryEmail,
                    fontSize = 9.sp,
                    color = SASGray
                )
            }
        }
        
        // Quantidade - largura fixa igual ao header
        Text(
            text = entrega.quantity.toString(),
            fontSize = 10.sp,
            color = SASGreenDark,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(50.dp)
        )
        
        // Stock Antes - largura fixa igual ao header
        Text(
            text = entrega.stockBefore.toString(),
            fontSize = 10.sp,
            color = SASGray,
            modifier = Modifier.width(80.dp)
        )
        
        // Stock Depois - largura fixa igual ao header
        Text(
            text = entrega.stockAfter.toString(),
            fontSize = 10.sp,
            color = SASGray,
            modifier = Modifier.width(80.dp)
        )
        
        // Observações - largura fixa igual ao header
        Text(
            text = entrega.notes.ifEmpty { "-" },
            fontSize = 10.sp,
            color = SASGray,
            modifier = Modifier.width(120.dp)
        )
    }
}

