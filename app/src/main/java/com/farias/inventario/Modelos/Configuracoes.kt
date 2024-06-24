package com.farias.inventario.Modelos

data class Configuracoes(
    val importarCadastro: Boolean,
    val delimitador: String,
    val codigoProduto: Boolean,
    val tamanhoCodigoProduto: Int,
    val codigoAuxiliar: Boolean,
    val tamanhoCodigoAuxiliar: Int,
    val descricao: Boolean,
    val senhaAcesso: String
)