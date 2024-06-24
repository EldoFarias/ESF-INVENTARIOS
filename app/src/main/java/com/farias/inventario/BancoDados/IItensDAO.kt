package com.farias.inventario.BancoDados

import com.farias.inventario.Modelos.Itens

interface IItensDAO {

    fun listarItens(endereco: String, numeroContagem: String): List<Itens>
    fun listarItensResumido(endereco: String, numeroContagem: String): List<Itens>

    fun totalizarItensLidos(endereco: String, numeroContagem: String) : Int
    fun totalizarReferencias(endereco: String, numeroContagem: String) : Int

}