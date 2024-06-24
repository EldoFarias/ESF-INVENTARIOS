package com.farias.inventario.Modelos

import java.io.Serializable

data class Endereco(
    val id_endereco: Int = 0,
    val codigo_endereco: String = "",
    val nome_endereco: String = "",
    val status: String = "",
    val data_abertura: String = "",
    val data_fechado: String = "",
    val exibirContagem1: Boolean = false,
    val exibirContagem2: Boolean = false,
    val exibirContagem3: Boolean = false
): Serializable


data class EnderecoReduzido(
    val codigo_endereco: String = "",
    val nome_endereco: String = "",
    val status: String = "",
): Serializable
