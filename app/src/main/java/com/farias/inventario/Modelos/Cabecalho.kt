package com.farias.inventario.Modelos

import java.io.Serializable

data class Cabecalho (

    val id_cabecalho: Int = 0,
    val id_controlecabecalho: Int = 0,
    val endereco_contagem: String = "",
    val numero_contagem: String = "",
    val codigo_operador: String = "",
    val codigo_acesso: String = "",
    val hora_abertura_contagem: String = "",
    val hora_fechamento_contagem: String = "",
    val status_contagem: String = "",
    val identificador: String = ""

): Serializable