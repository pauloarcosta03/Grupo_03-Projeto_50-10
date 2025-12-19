package com.grupo3.sasocial.domain.model

data class Beneficiario(
    val id: String = "",
    
    // Ano Letivo
    val anoLetivo: String = "",
    
    // Identificação do candidato
    val nome: String = "",
    val dataNascimento: String = "",
    val ccPassaporte: String = "",
    val telemovel: String = "",
    val email: String = "",
    
    // Dados académicos
    val licenciatura: Boolean = false,
    val mestrado: Boolean = false,
    val ctesp: Boolean = false,
    val curso: String = "",
    val numeroEstudante: String = "",
    
    // Tipologia do pedido
    val produtosAlimentares: Boolean = false,
    val produtosHigienePessoal: Boolean = false,
    val produtosLimpeza: Boolean = false,
    val outros: Boolean = false,
    
    // Outros apoios
    val apoiadoFAES: String = "",
    val beneficiarioBolsa: String = "",
    val entidadeValorBolsa: String = "",
    
    // Declarações
    val declaracaoVeracidade: Boolean = false,
    val declaracaoRGPD: Boolean = false,
    
    // Data e assinatura
    val data: String = "",
    val assinatura: String = "",
    
    // Metadata
    val createdAt: com.google.firebase.Timestamp? = null,
    val status: String = "pendente", // "pendente", "aprovado", "rejeitado"
    
    // Documentos (lista de ficheiros em base64)
    val files: List<DocumentoFile> = emptyList()
)

data class DocumentoFile(
    val name: String = "",
    val type: String = "",
    val size: Long = 0,
    val data: String = "", // base64
    val documentoType: String = "",
    val documentoLabel: String = ""
)
