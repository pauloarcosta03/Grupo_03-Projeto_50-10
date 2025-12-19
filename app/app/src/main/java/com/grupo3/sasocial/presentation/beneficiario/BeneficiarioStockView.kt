package com.grupo3.sasocial.presentation.beneficiario

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.grupo3.sasocial.domain.model.Bem
import com.grupo3.sasocial.presentation.components.BottomNavBar
import com.grupo3.sasocial.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BeneficiarioStockView(
    categoriasAceites: List<String>,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: BeneficiarioStockViewModel = viewModel { 
        BeneficiarioStockViewModel(context.applicationContext as Application, categoriasAceites) 
    }
    
    val bens by viewModel.bens.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
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
            Text(
                text = "Stock Disponível",
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
                if (bens.isEmpty()) {
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
                } else {
                    items(bens) { bem ->
                        BemCardBeneficiario(bem = bem)
                    }
                }
            }
        }
        
        BottomNavBar(
            currentRoute = "beneficiarioStock",
            onNavigate = onNavigate,
            isBeneficiario = true
        )
    }
}

@Composable
fun BemCardBeneficiario(bem: Bem) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val isExpired = bem.validUntil?.let { 
        it.toDate().before(Date())
    } ?: false
    
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
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SASGreenDark
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Categoria: ${bem.category}",
                        fontSize = 14.sp,
                        color = SASGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Fornecedor: ${bem.supplier}",
                        fontSize = 14.sp,
                        color = SASGray
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Quantidade: ${bem.quantity}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bem.quantity <= bem.minStock) SASRed else SASGreenDark
                    )
                    if (bem.quantity <= bem.minStock) {
                        Text(
                            text = "Stock baixo!",
                            fontSize = 12.sp,
                            color = SASRed
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Divider(color = SASGray.copy(alpha = 0.3f))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                bem.entryDate?.let {
                    Column {
                        Text(
                            text = "Data de Entrada",
                            fontSize = 11.sp,
                            color = SASGray
                        )
                        Text(
                            text = dateFormat.format(it.toDate()),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                bem.validUntil?.let {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Data de Validade",
                            fontSize = 11.sp,
                            color = SASGray
                        )
                        Text(
                            text = dateFormat.format(it.toDate()),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isExpired) SASRed else Color.Unspecified
                        )
                        if (isExpired) {
                            Text(
                                text = "Expirado",
                                fontSize = 10.sp,
                                color = SASRed
                            )
                        }
                    }
                }
            }
        }
    }
}

