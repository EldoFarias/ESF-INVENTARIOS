package com.farias.inventario.Modelos

import java.io.Serializable

data class Produto (

    val id_produto: Int = 0,
    val codigo_produto: String = "",
    val codigo_auxiliar_produto: String = "",
    val descricao_produto: String = "",
    val data_cadastro: String? = null

): Serializable