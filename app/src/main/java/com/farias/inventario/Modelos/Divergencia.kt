package com.farias.inventario.Modelos

import android.content.ContentValues
import java.io.Serializable

data class Divergencia(
    var enderecoDivergencia: String = "",
    var codigoProdutoDivergencia: String = "",
    var descricao: String = "",
    var quantidadeContagem1: Int = 0,
    var quantidadeContagem2: Int = 0,
    var quantidadeContagem3: Int = 0

    ): Serializable

data class DivergenciaPorContagem(
    var enderecoDivergencia: String = "",
    var codigoProdutoDivergencia: String = "",
    var descricao: String = "",
    var quantidadeContagem1: Int = 0

): Serializable



fun Divergencia.toContentValues(): ContentValues {

    val contentValues = ContentValues()
    contentValues.put("endereco_divergencia", enderecoDivergencia)
    contentValues.put("codigo_produto_divergencia", codigoProdutoDivergencia)
    contentValues.put("descricao_produto_divergencia", descricao)
    contentValues.put("quantidade1", quantidadeContagem1)
    contentValues.put("quantidade2", quantidadeContagem2)
    contentValues.put("quantidade3", quantidadeContagem3)

    return contentValues
}


