package com.grupo3.sasocial.domain.model

import com.google.firebase.Timestamp

data class Entrega(
    val id: String = "",
    val beneficiaryEmail: String = "",
    val beneficiaryId: String = "",
    val beneficiaryName: String = "",
    val deliveryStatusId: String = "", // Referência a delivery_status
    val createdAt: Timestamp? = null,
    val deliveryDate: Timestamp? = null,
    val notes: String = "",
    val productCategory: String = "",
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val stockAfter: Int = 0,
    val stockBefore: Int = 0
)
