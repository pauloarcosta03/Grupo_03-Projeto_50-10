package com.grupo3.sasocial.presentation.stock

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo3.sasocial.domain.model.Bem
import com.grupo3.sasocial.presentation.components.BottomNavBar
import com.grupo3.sasocial.ui.theme.*

@Composable
fun InventarioView(
    onNavigate: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: StockViewModel = viewModel { StockViewModel(context.applicationContext as Application) }
    val bens by viewModel.bens.collectAsState()
    
    var verificados by remember { mutableStateOf(setOf<String>()) }
    
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
                text = "Fazer Inventário",
                color = SASWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            items(bens) { bem ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = SASWhite),
                    shape = RoundedCornerShape(8.dp)
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
                                text = bem.name,
                                fontWeight = FontWeight.Bold,
                                color = SASGreenDark
                            )
                            Text(
                                text = "Categoria: ${bem.category}",
                                fontSize = 12.sp,
                                color = SASGray
                            )
                            Text(
                                text = "Unidades: ${bem.quantity}",
                                fontSize = 12.sp,
                                color = SASGray
                            )
                            Text(
                                text = "Fornecedor: ${bem.supplier.ifEmpty { "N/A" }}",
                                fontSize = 12.sp,
                                color = SASGray
                            )
                        }
                        
                        Row {
                            IconButton(
                                onClick = {
                                    verificados = if (verificados.contains(bem.id)) {
                                        verificados - bem.id
                                    } else {
                                        verificados + bem.id
                                    }
                                }
                            ) {
                                Icon(
                                    if (verificados.contains(bem.id)) Icons.Default.Check else Icons.Default.Refresh,
                                    contentDescription = "Verificar",
                                    tint = if (verificados.contains(bem.id)) SASGreenApproved else SASGray
                                )
                            }
                            IconButton(onClick = { /* Add more */ }) {
                                Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = SASGreen)
                            }
                        }
                    }
                }
            }
        }
        
        BottomNavBar(
            currentRoute = "stock",
            onNavigate = onNavigate
        )
    }
}
