package com.farias.inventario.Modelos

import java.io.Serializable

data class Usuario (

    val id_usuario: Int = 0,
    val codigo_usuario: Int = 0,
    val nome_usuario: String = "",
    val senha_usuario: String = "",
    val status: String = ""

): Serializable