package com.grupo3.sasocial.domain.model

data class Pedido(
    val id: String = "",
    val beneficiarioId: String = "",
    val beneficiarioEmail: String = "",
    val beneficiarioNome: String = "",
    val items: List<PedidoItem> = emptyList(),
    val status: String = "PENDENTE", // "PENDENTE", "APROVADO", "REJEITADO", "ENTREGUE"
    val observacoes: String = "",
    val createdAt: com.google.firebase.Timestamp? = null,
    val updatedAt: com.google.firebase.Timestamp? = null,
    val aprovadoPor: String? = null,
    val dataAprovacao: com.google.firebase.Timestamp? = null,
    val dataEntrega: com.google.firebase.Timestamp? = null
)

data class PedidoItem(
    val productId: String = "",
    val productName: String = "",
    val productCategory: String = "",
    val quantity: Int = 0,
    val unit: String = "unidade"
)

