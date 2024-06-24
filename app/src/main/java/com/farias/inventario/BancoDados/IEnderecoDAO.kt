package com.farias.inventario.BancoDados

import com.farias.inventario.Modelos.Endereco

interface IEnderecoDAO {

    fun salvar(endereco: Endereco): Boolean
    fun editar(endereco: Endereco): Boolean
    fun deletar(endereco: String): Boolean
    fun listar(): List<Endereco>
    fun deletarTudo(idEndereco: Int): Boolean
    fun fecharEndereco(endereco: String, numeroContagem: String): Boolean
    fun atualizarEnderecoFechado(endereco: String): Boolean
    fun totalEnderecos(): Int
    fun totalAbertos(): Int
    fun totalFechados(): Int
}