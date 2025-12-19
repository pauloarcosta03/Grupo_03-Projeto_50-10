package com.grupo3.sasocial.presentation.beneficiarios

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo3.sasocial.domain.model.Beneficiario
import com.grupo3.sasocial.presentation.components.BottomNavBar
import com.grupo3.sasocial.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiariosView(
    onNavigate: (String) -> Unit,
    onBeneficiarioClick: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: BeneficiariosViewModel = viewModel { BeneficiariosViewModel(context.applicationContext as Application) }
    val beneficiarios by viewModel.beneficiarios.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }
    
    val filteredBeneficiarios = beneficiarios.filter { b ->
        (searchQuery.isEmpty() || b.nome.contains(searchQuery, ignoreCase = true) || 
         b.numeroEstudante.contains(searchQuery)) &&
        (selectedFilter == "Todos" || 
         (selectedFilter == "Pendente" && (b.status == "pendente" || b.status.isEmpty())) ||
         (selectedFilter == "Aprovado" && (b.status == "aprovado" || b.status == "aceite")) ||
         (selectedFilter == "Rejeitado" && (b.status == "rejeitado" || b.status == "recusado")))
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
                text = "Candidaturas",
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
                placeholder = { Text("Pesquisar por nome ou nº estudante") },
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
        
        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Todos", "Pendente", "Aprovado", "Rejeitado").forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SASGreen,
                        selectedLabelColor = SASWhite
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            items(filteredBeneficiarios) { beneficiario ->
                BeneficiarioItem(
                    beneficiario = beneficiario,
                    onClick = { onBeneficiarioClick(beneficiario.id) }
                )
            }
            
            if (filteredBeneficiarios.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma candidatura encontrada",
                            color = SASGray
                        )
                    }
                }
            }
        }
        
        BottomNavBar(
            currentRoute = "beneficiarios",
            onNavigate = onNavigate
        )
    }
}

@Composable
fun BeneficiarioItem(
    beneficiario: Beneficiario,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
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
                    text = beneficiario.nome,
                    color = SASGreenDark,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Nº ${beneficiario.numeroEstudante} • ${beneficiario.curso}",
                    color = SASGray,
                    fontSize = 12.sp
                )
                Text(
                    text = beneficiario.email,
                    color = SASGray,
                    fontSize = 12.sp
                )
            }
            
            val (color, text) = when (beneficiario.status) {
                "aprovado", "aceite" -> SASGreenApproved to "Aprovado"
                "rejeitado", "recusado" -> SASRed to "Rejeitado"
                else -> SASOrange to "Pendente"
            }
            
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = text,
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
