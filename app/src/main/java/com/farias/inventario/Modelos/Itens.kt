package com.farias.inventario.Modelos

import java.io.Serializable

data class Itens (

    val id_item: Int = 0,
    val endereco_contagem: String = "",
    val numero_contagem: String = "",
    val codigo_produto: String = "",
    val descricao_produto: String = "",
    val quantidade: Int = 0,
    val hora_abertura_contagem: String = "",
    val hora_fechamento_contagem: String = "",
    val codigo_operador: String = "",
    val idCabecalho: Int = 0

): Serializable