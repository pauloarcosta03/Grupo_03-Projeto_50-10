package com.grupo3.sasocial.domain.model

data class Alteracao(
    val id: String = "",
    val tipo: String = "", // "aprovacao", "inventario"
    val descricao: String = "",
    val funcionarioNome: String = "",
    val funcionarioNumero: String = "",
    val data: String = "",
    val hora: String = ""
)
