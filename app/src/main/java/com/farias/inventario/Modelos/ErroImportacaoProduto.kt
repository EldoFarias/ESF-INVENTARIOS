package com.farias.inventario.Modelos

import java.io.Serializable

data class ErroImportacaoProduto (

    val id_produto: Int = 0,
    val linha_erro: Int = 0,
    val codigo_produto: String = "",
    val data_cadastro: String = ""

):Serializable