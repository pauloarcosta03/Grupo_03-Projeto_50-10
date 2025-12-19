package com.grupo3.sasocial.presentation.dashboard

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo3.sasocial.domain.model.Bem
import com.grupo3.sasocial.domain.model.Beneficiario
import com.grupo3.sasocial.presentation.components.BottomNavBar
import com.grupo3.sasocial.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.min
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardView(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val isBeneficiarioUseCase = com.grupo3.sasocial.di.AppModule.provideIsBeneficiarioUseCase()
    var isChecking by remember { mutableStateOf(true) }
    
    // Verificar se é beneficiário e redirecionar
    LaunchedEffect(Unit) {
        try {
            val isBenef = isBeneficiarioUseCase.invoke()
            if (isBenef) {
                val beneficiario = isBeneficiarioUseCase.getBeneficiarioAprovado()
                if (beneficiario != null) {
                    onNavigate("beneficiarioDashboard")
                    return@LaunchedEffect
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DashboardView", "Erro ao verificar beneficiário", e)
        } finally {
            isChecking = false
        }
    }
    
    // Mostrar loading enquanto verifica
    if (isChecking) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SASBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SASGreen)
        }
        return
    }
    
    val viewModel: DashboardViewModel = viewModel { DashboardViewModel(application) }
    val bens by viewModel.bens.collectAsState()
    val entregas by viewModel.entregas.collectAsState()
    val beneficiariosPendentes by viewModel.beneficiariosPendentes.collectAsState()
    val beneficiariosAceites by viewModel.beneficiariosAceites.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf<Bem?>(null) }
    
    // Filtros
    var selectedBeneficiario by     remember { mutableStateOf<Beneficiario?>(null) }
    var selectedCategoria by remember { mutableStateOf<String?>(null) }
    var beneficiarioExpanded by remember { mutableStateOf(false) }
    var categoriaExpanded by remember { mutableStateOf(false) }
    
    // Categorias disponíveis
    val categoriasDisponiveis = remember(bens) {
        bens.map { it.category }.distinct().filter { it.isNotEmpty() }.sorted()
    }
    
    // Categorias do beneficiário selecionado
    val categoriasBeneficiario = remember(selectedBeneficiario) {
        selectedBeneficiario?.let { beneficiario ->
            viewModel.getCategoriasBeneficiario(beneficiario)
        } ?: emptyList()
    }
    
    // Filtrar produtos
    val filteredBens = remember(bens, selectedBeneficiario, selectedCategoria) {
        var filtered: List<Bem> = bens
        
        // Filtrar por beneficiário (através das suas categorias)
        selectedBeneficiario?.let { beneficiario ->
            val categorias = viewModel.getCategoriasBeneficiario(beneficiario)
            filtered = filtered.filter { bem ->
                categorias.contains(bem.category)
            }
        }
        
        // Filtrar por categoria específica
        selectedCategoria?.let { categoria ->
            filtered = filtered.filter { bem ->
                bem.category == categoria
            }
        }
        
        filtered
    }
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SAS IPCA - Dashboard",
                color = SASWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onLogout) {
                Text("Sair", color = SASWhite)
            }
        }
        
        // Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            // Stats Cards Row
            item {
                val totalUnidades = bens.sumOf { it.quantity }
                val categorias = bens.map { it.category }.distinct().size
                val itensBaixoStock = bens.count { it.quantity <= it.minStock }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Itens em Stock",
                        value = "$totalUnidades",
                        subtitle = "Total de unidades",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Categorias",
                        value = "$categorias",
                        subtitle = "Categorias distintas",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Baixo Stock",
                        value = "$itensBaixoStock",
                        subtitle = "Requer atenção",
                        valueColor = if (itensBaixoStock > 0) SASRed else SASGreenDark,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Charts Row
            item {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    val isTablet = maxWidth > 600.dp
                    val chartHeight = if (isTablet) 200.dp else 150.dp
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Bar Chart - Top Items
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = SASWhite),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Top 5 Itens",
                                    fontSize = if (isTablet) 16.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SASGreenDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                BarChart(
                                    items = bens.sortedByDescending { it.quantity }.take(5),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(chartHeight),
                                    isTablet = isTablet
                                )
                            }
                        }
                        
                        // Pie Chart - Distribution by Category
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = SASWhite),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Por Categoria",
                                    fontSize = if (isTablet) 16.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SASGreenDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                PieChart(
                                    items = bens,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(chartHeight),
                                    isTablet = isTablet
                                )
                            }
                        }
                    }
                }
            }
            
            // Tabela de Produtos em Stock
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SASWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Produtos em Stock",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SASGreenDark,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // Filtros
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Filtro por Beneficiário
                            ExposedDropdownMenuBox(
                                expanded = beneficiarioExpanded,
                                onExpandedChange = { beneficiarioExpanded = !beneficiarioExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedBeneficiario?.let { "${it.nome} (${it.email})" } ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Filtrar por Beneficiário:") },
                                    placeholder = { Text("Selecione um beneficiário") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = beneficiarioExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SASGreen,
                                        unfocusedBorderColor = SASGray
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = beneficiarioExpanded,
                                    onDismissRequest = { beneficiarioExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Todos os produtos") },
                                        onClick = {
                                            selectedBeneficiario = null
                                            selectedCategoria = null
                                            beneficiarioExpanded = false
                                        }
                                    )
                                    beneficiariosAceites.forEach { beneficiario ->
                                        DropdownMenuItem(
                                            text = { Text("${beneficiario.nome} (${beneficiario.email})") },
                                            onClick = {
                                                selectedBeneficiario = beneficiario
                                                selectedCategoria = null
                                                beneficiarioExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            // Botão Limpar Filtro
                            OutlinedButton(
                                onClick = {
                                    selectedBeneficiario = null
                                    selectedCategoria = null
                                },
                                enabled = selectedBeneficiario != null || selectedCategoria != null
                            ) {
                                Text("Limpar Filtro", fontSize = 12.sp)
                            }
                        }
                        
                        // Filtro por Categoria
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = categoriaExpanded,
                            onExpandedChange = { categoriaExpanded = !categoriaExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedCategoria ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Filtrar por Categoria:") },
                                placeholder = { Text("Selecione uma categoria") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SASGreen,
                                    unfocusedBorderColor = SASGray
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = categoriaExpanded,
                                onDismissRequest = { categoriaExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todas as categorias") },
                                    onClick = {
                                        selectedCategoria = null
                                        categoriaExpanded = false
                                    }
                                )
                                categoriasDisponiveis.forEach { categoria ->
                                    DropdownMenuItem(
                                        text = { Text(categoria) },
                                        onClick = {
                                            selectedCategoria = categoria
                                            categoriaExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Informação sobre filtros ativos
                        if (selectedBeneficiario != null || selectedCategoria != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    if (selectedBeneficiario != null) {
                                        Text(
                                            text = "Categorias do beneficiário: ${categoriasBeneficiario.joinToString(", ")}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1976D2)
                                        )
                                    }
                                    if (selectedBeneficiario != null || selectedCategoria != null) {
                                        val categoriasMostradas = if (selectedCategoria != null) {
                                            listOf(selectedCategoria!!)
                                        } else {
                                            categoriasBeneficiario
                                        }
                                        Text(
                                            text = "Mostrando apenas produtos das categorias: ${categoriasMostradas.joinToString(", ")}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF1976D2),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
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
                                TableHeader("Nome", 100.dp)
                                TableHeader("Categoria", 90.dp)
                                TableHeader("Fornecedor", 100.dp)
                                TableHeader("Quantidade", 70.dp)
                                TableHeader("Mínimo", 60.dp)
                                TableHeader("Data Entrada", 100.dp)
                                TableHeader("Validade", 100.dp)
                                TableHeader("Estado", 70.dp)
                                TableHeader("Ações", 200.dp)
                            }
                            
                            Divider(
                                color = SASGray.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Linhas da Tabela
                            if (filteredBens.isEmpty()) {
                                Text(
                                    text = if (selectedBeneficiario != null || selectedCategoria != null) {
                                        "Sem produtos correspondentes aos filtros"
                                    } else {
                                        "Sem produtos em stock"
                                    },
                                    color = SASGray,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                Column(
                                    modifier = Modifier.height(400.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    filteredBens.forEach { bem ->
                                        BemTableRow(
                                            bem = bem,
                                            dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                                            onDarBaixa = { onNavigate("stock") },
                                            onEdit = { onNavigate("editBem/${bem.id}") },
                                            onDelete = { showDeleteDialog = bem }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Última entrega/baixa
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SASWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Última entrega",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SASGreenDark
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (entregas.isNotEmpty()) {
                            val ultima = entregas.sortedByDescending { it.createdAt }.first()
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SASLightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = ultima.productName,
                                        fontWeight = FontWeight.Bold,
                                        color = SASGreenDark
                                    )
                                    if (ultima.beneficiaryName.isNotEmpty()) {
                                        Text(
                                            text = "Para: ${ultima.beneficiaryName}",
                                            fontSize = 12.sp,
                                            color = SASGray
                                        )
                                    }
                                    ultima.createdAt?.toDate()?.let { date ->
                                        Text(
                                            text = dateFormat.format(date),
                                            fontSize = 11.sp,
                                            color = SASGray
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "-${ultima.quantity}",
                                    fontWeight = FontWeight.Bold,
                                    color = SASRed
                                )
                            }
                        } else {
                            Text(
                                text = "Sem entregas registadas",
                                fontSize = 14.sp,
                                color = SASGray
                            )
                        }
                    }
                }
            }
            
            // Beneficiários por aprovar (como no mockup)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { onNavigate("beneficiarios") },
                    colors = CardDefaults.cardColors(containerColor = SASWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Beneficiários por aprovar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SASGreenDark
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (beneficiariosPendentes.isEmpty()) {
                            Text(
                                text = "Sem candidaturas pendentes",
                                fontSize = 14.sp,
                                color = SASGray
                            )
                        } else {
                            beneficiariosPendentes.take(3).forEach { beneficiario ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(SASLightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${beneficiario.numeroEstudante} - ${beneficiario.nome}",
                                        color = SASGreenDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Surface(
                                        color = SASOrange.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Em aprovação",
                                            color = SASOrange,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Delete Dialog
        showDeleteDialog?.let { bem ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Excluir Produto") },
                text = {
                    Text("Tens a certeza que queres excluir \"${bem.name}\"? Esta ação não pode ser desfeita.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteBem(bem.id)
                            showDeleteDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SASRed)
                    ) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Bottom Navigation
        BottomNavBar(
            currentRoute = "dashboard",
            onNavigate = onNavigate
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    valueColor: Color = SASGreenDark
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SASWhite),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = SASGray)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Text(subtitle, fontSize = 10.sp, color = SASGray)
        }
    }
}

@Composable
fun BarChart(items: List<Bem>, modifier: Modifier = Modifier, isTablet: Boolean = false) {
    val maxQty = items.maxOfOrNull { it.quantity } ?: 1
    val barColor = Color(0xFF2196F3)
    
    Canvas(modifier = modifier) {
        if (items.isEmpty()) return@Canvas
        
        val spacing = if (isTablet) 1.8f else 2f
        val barWidth = size.width / (items.size * spacing)
        val maxHeight = size.height * 0.65f
        val labelHeight = if (isTablet) 30f else 20f
        
        items.forEachIndexed { index, bem ->
            val barHeight = (bem.quantity.toFloat() / maxQty) * maxHeight
            val x = barWidth * (index * spacing + (spacing - 1f) / 2f)
            
            drawRect(
                color = barColor,
                topLeft = Offset(x, size.height - barHeight - labelHeight),
                size = Size(barWidth * 0.7f, barHeight)
            )
            
            // Draw label
            drawContext.canvas.nativeCanvas.apply {
                val textSize = if (isTablet) 22f else 16f
                val maxNameLength = if (isTablet) 12 else 8
                drawText(
                    bem.name.take(maxNameLength) + if (bem.name.length > maxNameLength) ".." else "",
                    x + barWidth / 2,
                    size.height - 4f,
                    android.graphics.Paint().apply {
                        this.textSize = textSize
                        textAlign = android.graphics.Paint.Align.CENTER
                        color = android.graphics.Color.GRAY
                    }
                )
            }
        }
    }
}

@Composable
fun PieChart(items: List<Bem>, modifier: Modifier = Modifier, isTablet: Boolean = false) {
    val categoryTotals = items.groupBy { it.category }
        .mapValues { it.value.sumOf { b -> b.quantity } }
    val total = categoryTotals.values.sum().toFloat()
    
    val colors = listOf(
        SASGreen,
        Color(0xFF2196F3),
        Color(0xFFFFC107),
        Color(0xFFE91E63),
        Color(0xFF9C27B0)
    )
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier
                .weight(if (isTablet) 1.2f else 1f)
                .aspectRatio(1f)
        ) {
            if (total == 0f) return@Canvas
            
            var startAngle = -90f
            val radius = min(size.width, size.height) / 2 * (if (isTablet) 0.85f else 0.8f)
            val strokeWidth = radius * (if (isTablet) 0.35f else 0.4f)
            
            categoryTotals.entries.forEachIndexed { index, (_, qty) ->
                val sweep = (qty / total) * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(
                        (size.width - radius * 2) / 2,
                        (size.height - radius * 2) / 2
                    ),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweep
            }
        }
        
        // Legend
        Column(
            modifier = Modifier
                .weight(if (isTablet) 0.8f else 1f)
                .padding(start = if (isTablet) 8.dp else 4.dp)
        ) {
            categoryTotals.entries.forEachIndexed { index, (cat, qty) ->
                val pct = if (total > 0) (qty / total * 100).toInt() else 0
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = if (isTablet) 2.dp else 1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isTablet) 10.dp else 8.dp)
                            .background(colors[index % colors.size], CircleShape)
                    )
                    Text(
                        text = "${cat.take(if (isTablet) 10 else 6)}: $pct%",
                        fontSize = if (isTablet) 11.sp else 9.sp,
                        color = SASGray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
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
fun BemTableRow(
    bem: Bem,
    dateFormat: SimpleDateFormat,
    onDarBaixa: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val estado = if (bem.quantity <= bem.minStock) "Baixo" else "OK"
    val estadoColor = if (bem.quantity <= bem.minStock) SASRed else SASGreenApproved
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nome - largura fixa
        Text(
            text = bem.name,
            fontSize = 10.sp,
            color = SASGreenDark,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(100.dp)
        )
        
        // Categoria - largura fixa
        Text(
            text = bem.category.ifEmpty { "-" },
            fontSize = 10.sp,
            color = SASGray,
            modifier = Modifier.width(90.dp)
        )
        
        // Fornecedor - largura fixa
        Text(
            text = bem.supplier.ifEmpty { "-" },
            fontSize = 10.sp,
            color = SASGray,
            modifier = Modifier.width(100.dp)
        )
        
        // Quantidade - largura fixa
        Text(
            text = bem.quantity.toString(),
            fontSize = 10.sp,
            color = SASGreenDark,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(70.dp)
        )
        
        // Mínimo - largura fixa
        Text(
            text = bem.minStock.toString(),
            fontSize = 10.sp,
            color = SASGray,
            modifier = Modifier.width(60.dp)
        )
        
        // Data Entrada - largura fixa
        Text(
            text = bem.entryDate?.toDate()?.let { dateFormat.format(it) } ?: "-",
            fontSize = 10.sp,
            color = SASGray,
            modifier = Modifier.width(100.dp)
        )
        
        // Validade - largura fixa
        Text(
            text = bem.validUntil?.toDate()?.let { dateFormat.format(it) } ?: "-",
            fontSize = 10.sp,
            color = if (bem.validUntil != null) {
                val now = System.currentTimeMillis()
                val validUntilMillis = bem.validUntil.toDate().time
                if (validUntilMillis < now) SASRed else SASGray
            } else {
                SASGray
            },
            modifier = Modifier.width(100.dp)
        )
        
        // Estado - largura fixa
        Surface(
            color = estadoColor.copy(alpha = 0.2f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.width(70.dp)
        ) {
            Text(
                text = estado,
                fontSize = 10.sp,
                color = estadoColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
        
        // Ações - largura fixa
        Row(
            modifier = Modifier.width(200.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = onDarBaixa,
                colors = ButtonDefaults.buttonColors(containerColor = SASGreen),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
            ) {
                Text("Dar Baixa", fontSize = 9.sp)
            }
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2196F3))
            ) {
                Text("Editar", fontSize = 9.sp, color = Color(0xFF2196F3))
            }
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SASRed)
            ) {
                Text("Excluir", fontSize = 9.sp, color = SASRed)
            }
        }
    }
}
