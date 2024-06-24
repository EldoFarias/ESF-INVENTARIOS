package com.farias.inventario.BancoDados

import com.farias.inventario.Modelos.Usuario

interface IUsuarioDAO {

    fun cadastrarUsuario(usuario: Usuario): Boolean
    fun verificarCodigoUsuario(codigo: String): String
    fun fazerLogin(codigoAcesso: String, senha: String): Boolean
    fun deletarUsuario(id: Int): Boolean
    fun deletarTodosUsuarios(): Boolean
    fun listar(): List<Usuario>
    fun selecionarUsuario(codigo: String): Usuario?
}