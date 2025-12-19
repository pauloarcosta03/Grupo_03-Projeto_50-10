package com.grupo3.sasocial.domain.model

import com.google.firebase.Timestamp

data class Bem(
    val id: String = "",
    val name: String = "",
    val category: String = "", // Mantém para compatibilidade, mas pode usar goodTypeId
    val goodTypeId: String = "", // Referência a good_types
    val quantity: Int = 0,
    val minStock: Int = 0,
    val supplier: String = "",
    val locationId: String = "", // Referência a locations (futuro)
    val statusId: String = "", // Referência a inventory_status
    val createdAt: Timestamp? = null,
    val entryDate: Timestamp? = null,
    val validUntil: Timestamp? = null
)
