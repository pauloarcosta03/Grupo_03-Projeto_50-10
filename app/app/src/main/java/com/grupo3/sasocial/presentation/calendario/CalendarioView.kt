package com.grupo3.sasocial.presentation.calendario

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo3.sasocial.presentation.components.BottomNavBar
import com.grupo3.sasocial.ui.theme.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarioView(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: CalendarioViewModel = viewModel { CalendarioViewModel(context.applicationContext as Application) }
    val entregas by viewModel.entregas.collectAsState()
    
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val currentMonth = remember { mutableStateOf(YearMonth.now()) }
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
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
        
        // Calendar Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SASWhite),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = currentMonth.value.month.getDisplayName(TextStyle.FULL, Locale("pt", "PT"))
                        .replaceFirstChar { it.uppercase() },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SASGreenDark,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom").forEach { day ->
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            color = if (day == "Sáb" || day == "Dom") SASRed else SASGray,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val firstDayOfMonth = currentMonth.value.atDay(1)
                val daysInMonth = currentMonth.value.lengthOfMonth()
                val startDayOfWeek = (firstDayOfMonth.dayOfWeek.value - 1) % 7
                
                val days = mutableListOf<Int?>()
                repeat(startDayOfWeek) { days.add(null) }
                (1..daysInMonth).forEach { days.add(it) }
                while (days.size % 7 != 0) { days.add(null) }
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(240.dp),
                    userScrollEnabled = false
                ) {
                    items(days) { day ->
                        val isSelected = day == selectedDate.dayOfMonth && 
                            currentMonth.value == YearMonth.from(selectedDate)
                        val dayIndex = days.indexOf(day)
                        val isWeekend = day != null && dayIndex >= 0 && (dayIndex % 7) >= 5
                        
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) SASGreen else Color.Transparent)
                                .clickable { 
                                    day?.let { selectedDate = currentMonth.value.atDay(it) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            day?.let {
                                Text(
                                    text = it.toString(),
                                    fontSize = 14.sp,
                                    color = when {
                                        isSelected -> SASWhite
                                        isWeekend -> SASRed
                                        else -> SASGreenDark
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Entregas list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    text = "Últimas Entregas/Baixas (${entregas.size})",
                    fontWeight = FontWeight.Bold,
                    color = SASGreenDark,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            if (entregas.isEmpty()) {
                item {
                    Text(
                        text = "Sem registos de entregas",
                        color = SASGray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            items(entregas.sortedByDescending { it.createdAt }) { entrega ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = SASWhite),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = entrega.productName,
                                fontWeight = FontWeight.Bold,
                                color = SASGreenDark
                            )
                            Text(
                                text = "-${entrega.quantity}",
                                fontWeight = FontWeight.Bold,
                                color = SASRed
                            )
                        }
                        
                        if (entrega.beneficiaryName.isNotEmpty()) {
                            Text(
                                text = "Beneficiário: ${entrega.beneficiaryName}",
                                fontSize = 12.sp,
                                color = SASGray
                            )
                        }
                        
                        Text(
                            text = "Stock: ${entrega.stockBefore} → ${entrega.stockAfter}",
                            fontSize = 12.sp,
                            color = SASGray
                        )
                        
                        entrega.createdAt?.toDate()?.let { date ->
                            Text(
                                text = dateFormat.format(date),
                                fontSize = 11.sp,
                                color = SASGray
                            )
                        }
                        
                        if (entrega.notes.isNotEmpty()) {
                            Text(
                                text = "Notas: ${entrega.notes}",
                                fontSize = 11.sp,
                                color = SASGray,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }
        
        BottomNavBar(
            currentRoute = "calendario",
            onNavigate = onNavigate
        )
    }
}
