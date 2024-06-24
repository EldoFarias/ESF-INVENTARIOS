package com.farias.inventario.BancoDados

import ProdutoDAO
import com.farias.inventario.Modelos.ErroImportacaoProduto
import com.farias.inventario.Modelos.Produto

interface IProdutoDAO {

    fun salvarCompleto(produto: Produto): Boolean
    fun salvarCodigo(produto: Produto): Boolean
    fun salvarCodigoCodigoAuxiliar(produto: Produto): Boolean
    fun salvarCodigoDescricao(produto: Produto): Boolean
    fun editar(produto: Produto): Boolean
    fun deletar(idProduto: Int): Boolean
    fun listar(): List<Produto>
    fun deletarTudo(idProduto: Int): Boolean
    fun limparListaErros(): Boolean
    fun verificarDuplicadosImportacao(codigoProduto: String): Boolean
    fun salvarProdutosDuplicados(produto: ErroImportacaoProduto): Boolean
    fun listarErrosImportacao(): List<ErroImportacaoProduto>
    fun verificarProduto(codigo: String): String
    fun contarItens(): Int
    fun importarBase(): Boolean

}