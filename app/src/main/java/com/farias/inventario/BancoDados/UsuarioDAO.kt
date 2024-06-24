package com.farias.inventario.BancoDados

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.farias.inventario.Modelos.Produto
import com.farias.inventario.Modelos.Usuario
import java.lang.reflect.Executable

class UsuarioDAO(context: Context) : IUsuarioDAO {

    companion object {

        private var instance: UsuarioDAO? = null
        fun getInstance(context: Context): UsuarioDAO{
            return UsuarioDAO.instance ?: synchronized(this) {
                UsuarioDAO.instance ?: UsuarioDAO(context.applicationContext).also { UsuarioDAO.instance = it }
            }
        }
    }

    private val escrita = DatabaseHelper(context).writableDatabase
    private val leitura = DatabaseHelper(context).readableDatabase

    override fun cadastrarUsuario(usuario: Usuario): Boolean {

        val valores = ContentValues()
        valores.put("codigo_usuario", usuario.codigo_usuario)
        valores.put("nome_usuario", usuario.nome_usuario)
        valores.put("senha_usuario", usuario.senha_usuario)

        try {
            escrita.insert(
                DatabaseHelper.TABELA_USUARIOS,
                null,
                valores
            )

            //  Log.i("status", "Sucesso ao criar usuario")

        } catch (e: Exception) {
            Log.i("TAG", "Erro ao salvar:")
            return false
        }
        return true
    }

    override fun verificarCodigoUsuario(codigo: String): String {
        val codigoUsuario = codigo
        val sql =
            "SELECT nome_usuario FROM ${DatabaseHelper.TABELA_USUARIOS} WHERE codigo_usuario = ?"
        val cursor = leitura.rawQuery(sql, arrayOf(codigoUsuario))
        var nome = ""

        try {
            if (cursor.moveToFirst()) {
                val indexNomeUsuario = cursor.getColumnIndex("nome_usuario")
                nome = "Olá ${cursor.getString(indexNomeUsuario)}, informe sua senha."
            }else{
                nome = "Este código não pertence a nenhum usuario cadastrado, verifique e tente novamente!"
            }
        } catch (e: Exception) {
            e.message

        } finally {
            cursor.close()
        }
        return nome
    }

    override fun fazerLogin(codigoAcesso: String, senha: String): Boolean {
        val sql = "SELECT senha_usuario FROM ${DatabaseHelper.TABELA_USUARIOS} WHERE codigo_usuario = ?"
        val cursor = leitura.rawQuery(sql, arrayOf(codigoAcesso))

        try {
            if (cursor.moveToFirst()) {
                val indexSenha = cursor.getColumnIndex("senha_usuario")
                val senhaArmazenada = cursor.getString(indexSenha)
                return senha == senhaArmazenada
            } else {
                // Se o cursor não tiver nenhum resultado, o usuário não foi encontrado
                return false
            }
        } catch (e: Exception) {
            e.message
            return false
        } finally {
            cursor.close()
        }
    }


    override fun deletarUsuario(idUsuario: Int): Boolean {
        val args = arrayOf(idUsuario.toString())
        try {
            escrita.delete(
                DatabaseHelper.TABELA_USUARIOS,
                "id_usuario = ?",
                args
            )
           // Log.i("TAG", "Sucesso ao deletar")
        } catch (e: Exception) {
            Log.i("TAG", "Erro ao Deletar:")
            return false
        }
        return true
    }

    override fun deletarTodosUsuarios(): Boolean {
        try {
            escrita.delete(
                DatabaseHelper.TABELA_USUARIOS,
                null,
                null
            )
           // Log.i("TAG", "Sucesso ao Deletar tudo.")
        } catch (e: Exception) {
            //Log.i("TAG", "Erro ao Deletar tudo.")
            return false
        }
        return true
    }

    override fun listar(): List<Usuario> {
        val listaUsuario = mutableListOf<Usuario>()
        val sql = "SELECT * FROM ${DatabaseHelper.TABELA_USUARIOS}"

        val cursor = leitura.rawQuery(sql, null)

        val indiceId_usuario = cursor.getColumnIndex("id_usuario")
        val indiceCodigo_usuario = cursor.getColumnIndex("codigo_usuario")
        val indiceNome_usuario = cursor.getColumnIndex("nome_usuario")
        val indiceSenha_usuario = cursor.getColumnIndex("senha_usuario")


        while (cursor.moveToNext()) {
            val id = cursor.getInt(indiceId_usuario)
            val codigoUsuario = cursor.getInt(indiceCodigo_usuario)
            val nomeUsuario = cursor.getString(indiceNome_usuario)
            val senha = cursor.getString(indiceSenha_usuario)


            listaUsuario.add(
                Usuario(id,
                    codigoUsuario,
                    nomeUsuario,
                    senha
                )
            )
        }

        cursor.close()
        return listaUsuario
    }

    override fun selecionarUsuario(codigo: String): Usuario? {
        val sql = "SELECT * FROM ${DatabaseHelper.TABELA_USUARIOS} WHERE codigo_usuario = ?"
        val cursor = leitura.rawQuery(sql, arrayOf(codigo))

        var usuario: Usuario? = null

        try {
            if (cursor.moveToFirst()) {

                val indexId = cursor.getColumnIndex("id_usuario")
                val indexCodigo = cursor.getColumnIndex("codigo_usuario")
                val indexNome = cursor.getColumnIndex("nome_usuario")
                val indexSenha = cursor.getColumnIndex("senha_usuario")

                val id = cursor.getInt(indexId) ?: -1
                val codigo = cursor.getString(indexCodigo)
                val nome = cursor.getString(indexNome)
                val senha = cursor.getString(indexSenha)

                 usuario = Usuario(id, codigo.toInt(),nome, senha,"TESTE")

            } else {
                return null
            }
        } catch (e: Exception) {
            e.message
        }
        return usuario
    }

}