package com.farias.inventario.Modelos

import java.io.Serializable

data class UsuarioFirebase (

    val idUsuarioFirebase: String = "",
    val nomeUsuarioFirebase: String = "",
    val emailUsuarioFirebase: String = "",
    val dataCompraLicenca: String,
    val dataCompraExpira: String,
    val dataAtual: String,
    val diasRestanteLicenca: Int = 0

): Serializable