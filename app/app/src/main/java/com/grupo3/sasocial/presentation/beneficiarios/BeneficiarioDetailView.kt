package com.grupo3.sasocial.presentation.beneficiarios

import android.app.Application
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo3.sasocial.domain.model.DocumentoFile
import com.grupo3.sasocial.presentation.components.BottomNavBar
import com.grupo3.sasocial.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiarioDetailView(
    beneficiarioId: String,
    onNavigate: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: BeneficiarioDetailViewModel = viewModel { 
        BeneficiarioDetailViewModel(context.applicationContext as Application, beneficiarioId) 
    }
    val beneficiario by viewModel.beneficiario.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val approvalResult by viewModel.approvalResult.collectAsState()
    
    var selectedDocument by remember { mutableStateOf<DocumentoFile?>(null) }
    var showApprovalDialog by remember { mutableStateOf(false) }
    var approvalPassword by remember { mutableStateOf("") }
    var approvalError by remember { mutableStateOf("") }
    
    // Observar resultado da aprovação
    LaunchedEffect(approvalResult) {
        approvalResult?.let { result ->
            if (result.isSuccess) {
                approvalPassword = result.getOrNull() ?: ""
                showApprovalDialog = true
            } else {
                approvalError = result.exceptionOrNull()?.message ?: "Erro ao aprovar beneficiário"
                showApprovalDialog = true
            }
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = SASWhite)
            }
            Text(
                text = "Detalhes da Candidatura",
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
            beneficiario?.let { b ->
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    // Status badge
                    item {
                        val (color, text) = when (b.status) {
                            "aceite", "aprovado" -> SASGreenApproved to "Aprovado"
                            "rejeitado", "nao_aprovado" -> SASRed to "Não Aprovado"
                            else -> SASOrange to "Em Aprovação"
                        }
                        
                        Surface(
                            color = color.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = "Estado: $text",
                                color = color,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                    
                    // Identificação
                    item {
                        SectionCard(title = "Identificação do Candidato") {
                            DetailRow("Nome:", b.nome)
                            DetailRow("Data Nascimento:", b.dataNascimento)
                            DetailRow("CC/Passaporte:", b.ccPassaporte)
                            DetailRow("Telemóvel:", b.telemovel)
                            DetailRow("Email:", b.email)
                        }
                    }
                    
                    // Dados Académicos
                    item {
                        SectionCard(title = "Dados Académicos") {
                            DetailRow("Ano Letivo:", b.anoLetivo)
                            DetailRow("Nº Estudante:", b.numeroEstudante)
                            DetailRow("Curso:", b.curso)
                            val graus = mutableListOf<String>()
                            if (b.licenciatura) graus.add("Licenciatura")
                            if (b.mestrado) graus.add("Mestrado")
                            if (b.ctesp) graus.add("CTeSP")
                            if (graus.isNotEmpty()) {
                                DetailRow("Grau:", graus.joinToString(", "))
                            }
                        }
                    }
                    
                    // Tipologia do Pedido
                    item {
                        SectionCard(title = "Tipologia do Pedido") {
                            val produtos = mutableListOf<String>()
                            if (b.produtosAlimentares) produtos.add("Produtos Alimentares")
                            if (b.produtosHigienePessoal) produtos.add("Produtos de Higiene Pessoal")
                            if (b.produtosLimpeza) produtos.add("Produtos de Limpeza")
                            if (b.outros) produtos.add("Outros")
                            
                            if (produtos.isEmpty()) {
                                Text("Nenhum produto selecionado", color = SASGray, fontSize = 14.sp)
                            } else {
                                produtos.forEach { produto ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("✓ ", color = SASGreen, fontWeight = FontWeight.Bold)
                                        Text(produto, color = SASGreenDark)
                                    }
                                }
                            }
                        }
                    }
                    
                    // Outros Apoios
                    item {
                        SectionCard(title = "Outros Apoios") {
                            DetailRow("Apoiado FAES:", if (b.apoiadoFAES == "sim") "Sim" else "Não")
                            DetailRow("Beneficiário Bolsa:", if (b.beneficiarioBolsa == "sim") "Sim" else "Não")
                            if (b.entidadeValorBolsa.isNotEmpty()) {
                                DetailRow("Entidade/Valor:", b.entidadeValorBolsa)
                            }
                        }
                    }
                    
                    // Declarações
                    item {
                        SectionCard(title = "Declarações") {
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = if (b.declaracaoVeracidade) "✓" else "✗",
                                    color = if (b.declaracaoVeracidade) SASGreen else SASRed,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Declaração de Veracidade", color = SASGreenDark)
                            }
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = if (b.declaracaoRGPD) "✓" else "✗",
                                    color = if (b.declaracaoRGPD) SASGreen else SASRed,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Consentimento RGPD", color = SASGreenDark)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow("Data:", b.data)
                            DetailRow("Assinatura:", b.assinatura)
                        }
                    }
                    
                    // Data de Submissão
                    item {
                        b.createdAt?.let { timestamp ->
                            val date = timestamp.toDate()
                            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "PT"))
                            SectionCard(title = "Submissão") {
                                DetailRow("Data:", formatter.format(date))
                            }
                        }
                    }
                    
                    // Documentos
                    if (b.files.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = SASWhite),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Documentos Anexados (${b.files.size})",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SASGreen
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    b.files.forEach { file ->
                                        DocumentoItem(
                                            file = file,
                                            onClick = { selectedDocument = file }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = SASWhite),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Documentos Anexados",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SASGreen
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Nenhum documento anexado",
                                        color = SASGray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    // Botões de ação (só se pendente)
                    if (b.status == "pendente" || b.status.isEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { 
                                        viewModel.rejeitarBeneficiario()
                                        onNavigateBack()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SASRed),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Não Aprovar", fontWeight = FontWeight.Bold)
                                }
                                
                                Button(
                                    onClick = { 
                                        viewModel.aprovarBeneficiario()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SASGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Aprovar", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    
                    // Botão para criar conta se já estiver aprovado mas não tiver conta
                    if ((b.status == "aceite" || b.status == "aprovado") && b.email.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SASOrange.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "⚠️ Conta não criada",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SASOrange,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = "Este beneficiário foi aprovado pelo website mas não tem conta criada. Clique no botão abaixo para criar a conta.",
                                        fontSize = 12.sp,
                                        color = SASGray,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    Button(
                                        onClick = { 
                                            viewModel.criarContaParaBeneficiarioAprovado()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SASGreen),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Criar Conta para Beneficiário", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
        
        // Dialog de feedback da aprovação
        if (showApprovalDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showApprovalDialog = false
                    if (approvalError.isEmpty()) {
                        onNavigateBack()
                    }
                },
                title = {
                    Text(
                        text = if (approvalError.isEmpty()) {
                            if (beneficiario?.status == "aceite" || beneficiario?.status == "aprovado") {
                                "Conta Criada"
                            } else {
                                "Beneficiário Aprovado"
                            }
                        } else {
                            "Erro"
                        },
                        fontWeight = FontWeight.Bold,
                        color = if (approvalError.isEmpty()) SASGreen else SASRed
                    )
                },
                text = {
                    Column {
                        if (approvalError.isEmpty()) {
                            if (beneficiario?.status == "aceite" || beneficiario?.status == "aprovado") {
                                Text(
                                    text = "Conta criada com sucesso para o beneficiário já aprovado!",
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            } else {
                                Text(
                                    text = "A candidatura foi aprovada com sucesso!",
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            if (approvalPassword.isNotEmpty()) {
                                Text(
                                    text = "Uma conta foi criada para o beneficiário:",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Text(
                                    text = "Email: ${beneficiario?.email ?: "N/A"}",
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "Palavra-passe temporária: $approvalPassword",
                                    fontWeight = FontWeight.Bold,
                                    color = SASGreen,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "Um email foi enviado ao beneficiário com instruções para fazer login na app.",
                                    fontSize = 12.sp,
                                    color = SASGray
                                )
                            } else {
                                Text(
                                    text = "O status foi atualizado, mas não foi possível criar conta (email não disponível).",
                                    fontSize = 12.sp,
                                    color = SASGray
                                )
                            }
                        } else {
                            Text(
                                text = approvalError,
                                color = SASRed
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            showApprovalDialog = false
                            if (approvalError.isEmpty()) {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
        
        BottomNavBar(
            currentRoute = "beneficiarios",
            onNavigate = onNavigate
        )
    }
    
    // Document Preview Dialog
    selectedDocument?.let { doc ->
        DocumentPreviewDialog(
            document = doc,
            onDismiss = { selectedDocument = null }
        )
    }
}

@Composable
fun DocumentPreviewDialog(
    document: DocumentoFile,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SASWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = document.documentoLabel.ifEmpty { document.documentoType },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SASGreenDark
                        )
                        Text(
                            text = document.name,
                            fontSize = 12.sp,
                            color = SASGray
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Fechar", color = SASGreen)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(SASLightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (document.type.startsWith("image/") && document.data.isNotEmpty()) {
                        // Decode image outside composable
                        val bitmap = remember(document.data) {
                            try {
                                val base64Data = document.data.substringAfter("base64,")
                                val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = document.name,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = SASGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Não foi possível carregar a imagem",
                                    color = SASGray
                                )
                            }
                        }
                    } else if (document.type == "application/pdf") {
                        // PDF placeholder
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = SASRed
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Documento PDF",
                                color = SASGreenDark,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = document.name,
                                color = SASGray,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${document.size / 1024} KB",
                                color = SASGray,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        // Generic file
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = SASGreen
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = document.name,
                                color = SASGreenDark,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${document.size / 1024} KB",
                                color = SASGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Info
                Card(
                    colors = CardDefaults.cardColors(containerColor = SASLightGray.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        DetailRow("Tipo:", document.documentoLabel.ifEmpty { document.documentoType })
                        DetailRow("Ficheiro:", document.name)
                        DetailRow("Formato:", document.type)
                        DetailRow("Tamanho:", "${document.size / 1024} KB")
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = SASWhite),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SASGreen
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    if (value.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Medium,
                color = SASGreenDark,
                modifier = Modifier.width(120.dp)
            )
            Text(
                text = value,
                color = SASGray
            )
        }
    }
}

@Composable
fun DocumentoItem(
    file: DocumentoFile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SASLightGray.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when {
                file.type.startsWith("image/") -> Icons.Default.Image
                file.type == "application/pdf" -> Icons.Default.PictureAsPdf
                else -> Icons.Default.Description
            }
            val iconColor = when {
                file.type.startsWith("image/") -> SASGreen
                file.type == "application/pdf" -> SASRed
                else -> SASGreen
            }
            
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.documentoLabel.ifEmpty { file.documentoType },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SASGreenDark
                )
                Text(
                    text = file.name,
                    fontSize = 12.sp,
                    color = SASGray
                )
                Text(
                    text = "${file.size / 1024} KB • ${file.type}",
                    fontSize = 10.sp,
                    color = SASGray
                )
            }
            Icon(
                Icons.Default.Visibility,
                contentDescription = "Ver",
                tint = SASGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
