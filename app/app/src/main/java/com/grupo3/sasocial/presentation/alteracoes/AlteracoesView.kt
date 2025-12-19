package com.grupo3.sasocial.presentation.alteracoes

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo3.sasocial.domain.model.Alteracao
import com.grupo3.sasocial.presentation.components.BottomNavBar
import com.grupo3.sasocial.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlteracoesView(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: AlteracoesViewModel = viewModel { AlteracoesViewModel(context.applicationContext as Application) }
    val alteracoes by viewModel.alteracoes.collectAsState()
    
    var selectedFilter by remember { mutableStateOf("Todos") }
    
    val filteredAlteracoes = alteracoes.filter {
        selectedFilter == "Todos" || it.tipo == selectedFilter
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
                text = "Alterações",
                color = SASWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Filtro:", color = SASGray)
            Spacer(modifier = Modifier.width(8.dp))
            
            listOf("Todos", "aprovacao", "inventario").forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { 
                        Text(
                            when(filter) {
                                "aprovacao" -> "Aprovações"
                                "inventario" -> "Inventário"
                                else -> "Todos"
                            },
                            fontSize = 12.sp
                        ) 
                    },
                    modifier = Modifier.padding(end = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SASGreen,
                        selectedLabelColor = SASWhite
                    )
                )
            }
        }
        
        // List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            items(filteredAlteracoes) { alteracao ->
                AlteracaoItem(alteracao = alteracao)
            }
        }
        
        BottomNavBar(
            currentRoute = "alteracoes",
            onNavigate = onNavigate
        )
    }
}

@Composable
fun AlteracaoItem(alteracao: Alteracao) {
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alteracao.descricao,
                    color = SASGreenDark,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${alteracao.funcionarioNome} (${alteracao.funcionarioNumero})",
                    color = SASGray,
                    fontSize = 12.sp
                )
            }
            Column {
                Text(
                    text = alteracao.data,
                    color = SASGray,
                    fontSize = 12.sp
                )
                Text(
                    text = alteracao.hora,
                    color = SASGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}
