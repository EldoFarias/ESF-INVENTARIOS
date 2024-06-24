package com.farias.inventario.BancoDados

import com.farias.inventario.Modelos.Cabecalho
import com.farias.inventario.Modelos.Itens

interface IContagemDAO {

    fun inserirCabecalho(cabecalho: Cabecalho): Boolean
    fun inserirItens(item: Itens): Boolean

    fun listarCabecalho(): List<Cabecalho>
    fun listarItens(): List<Itens>

    fun verificarStatusEndereco(endereco: String): List<Pair<String, String>>
    fun verificarCabecalhoExiste(numeroContagem: String, endereco: String): Boolean

    fun deletarItemContagem(idItem: Int): Boolean
    fun verificarEnderecoExiste(endereco:String): Boolean

    fun somarItensContagem1(): Int
    fun somarItensContagem2(): Int
    fun somarItensContagem3(): Int

    fun somarPecasContagem1(): Int
    fun somarPecasContagem2(): Int
    fun somarPecasContagem3(): Int

    fun deletarContagem(endereco: String, numeroContagem: Int): Boolean
}