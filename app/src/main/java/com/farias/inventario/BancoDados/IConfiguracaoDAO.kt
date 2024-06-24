package com.farias.inventario.BancoDados

import com.farias.inventario.Modelos.Configuracoes

interface IConfiguracaoDAO {

    fun configuracoes(lista: List<Boolean>,codP: Int, codA: Int, delimitador: String, senha: String): Boolean
    fun deletarConfiguracoes(): Boolean
    fun verificarConfiguracao(): Configuracoes?

}