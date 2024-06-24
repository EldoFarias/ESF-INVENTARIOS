package com.farias.inventario.BancoDados

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.farias.inventario.Modelos.Configuracoes

class ConfiguracaoDAO(context: Context) : IConfiguracaoDAO {

    // Propriedade estática para armazenar a única instância da classe
    companion object {
        @Volatile
        private var instance: ConfiguracaoDAO? = null

        // Método estático para obter a instância única da classe
        fun getInstance(context: Context): ConfiguracaoDAO {
            return instance ?: synchronized(this) {
                instance ?: ConfiguracaoDAO(context.applicationContext).also { instance = it }
            }
        }
    }

    private val escrita = DatabaseHelper(context).writableDatabase
    private val leitura = DatabaseHelper(context).readableDatabase


    override fun configuracoes(
        lista: List<Boolean>,
        codP: Int,
        codA: Int,
        delimitador: String,
        senha: String

    ): Boolean {
        val valores = ContentValues().apply {
            put("importarCadastro", if (lista[0]) 1 else 0)
            put("codigoProduto", if (lista[1]) 1 else 0)
            put("codigoAuxiliar", if (lista[2]) 1 else 0)
            put("descricao", if (lista[3]) 1 else 0)

            put("delimitador", delimitador)
            put("tamanhoCodigoProduto", codP)
            put("tamanhoCodigoAuxiliar", codA)
            put("senhaAcesso", senha)

        }

        try {
            escrita.insert(
                DatabaseHelper.TABELA_CONFIGURACOES,
                null,
                valores
            )
        } catch (e: Exception) {
            Log.i("TAG", "Erro ao salvar: ${e.message}")
            return false
        }
        return true
    }

    override fun deletarConfiguracoes(): Boolean {

        try {
            escrita.delete(DatabaseHelper.TABELA_CONFIGURACOES, null, null)
        } catch (e: Exception) {
            Log.i("ROLA_ERRO", "ERRO AO DELETAR: ${e.message.toString()} ")
            return false
        }
        return true
    }

    @SuppressLint("Range")
    override fun verificarConfiguracao(): Configuracoes? {

        val sql = "SELECT * FROM ${DatabaseHelper.TABELA_CONFIGURACOES}"
        try {
            val cursor = leitura.rawQuery(sql, null)
            cursor.use {

                if (cursor.moveToFirst()) {
                    val importarCadastro = cursor.getInt(cursor.getColumnIndex("importarCadastro")) == 1
                    val delimitador = cursor.getString(cursor.getColumnIndex("delimitador"))
                    val codigoProduto = cursor.getInt(cursor.getColumnIndex("codigoProduto")) == 1
                    val tamanhoCodigoProduto = cursor.getInt(cursor.getColumnIndex("tamanhoCodigoProduto"))
                    val codigoAuxiliar = cursor.getInt(cursor.getColumnIndex("codigoAuxiliar")) == 1
                    val tamanhoCodigoAuxiliar = cursor.getInt(cursor.getColumnIndex("tamanhoCodigoAuxiliar"))
                    val descricao = cursor.getInt(cursor.getColumnIndex("descricao")) == 1
                    val senhaAcesso = cursor.getString(cursor.getColumnIndex("senhaAcesso"))

                    val configuracoes = Configuracoes(
                        importarCadastro,
                        delimitador,
                        codigoProduto,
                        tamanhoCodigoProduto,
                        codigoAuxiliar,
                        tamanhoCodigoAuxiliar,
                        descricao,
                        senhaAcesso
                    )

                    return configuracoes
                } else {
                    // Nenhuma configuração encontrada no banco de dados
                    return null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}