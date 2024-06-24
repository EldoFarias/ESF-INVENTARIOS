package com.farias.inventario.BancoDados

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.farias.inventario.Modelos.Endereco

class EnderecoDAO(context: Context) : IEnderecoDAO {

    // Propriedade estática para armazenar a única instância da classe
    companion object {
        @Volatile
        private var instance: EnderecoDAO? = null

        // Método estático para obter a instância única da classe
        fun getInstance(context: Context): EnderecoDAO {
            return instance ?: synchronized(this) {
                instance ?: EnderecoDAO(context.applicationContext).also { instance = it }
            }
        }
    }

    private val escrita = DatabaseHelper(context).writableDatabase
    private val leitura = DatabaseHelper(context).readableDatabase

    override fun salvar(endereco: Endereco): Boolean {

        val valores = ContentValues()
        valores.put("codigo_endereco", endereco.codigo_endereco)
        valores.put("nome_endereco", endereco.nome_endereco)
        valores.put("status", endereco.status)
        valores.put("data_abertura", endereco.data_abertura)
        valores.put("data_fechado", endereco.data_fechado)

        try {
            escrita.insert(
                DatabaseHelper.TABELA_ENDERECOS,
                null,
                valores
            )



        } catch (e: Exception) {
            Log.i("TAG", "Erro ao salvar:")
            return false
        }

        Log.i("ROLA", "inserirEndereço - chave primaria = ${endereco.codigo_endereco} ")
        return true
    }

    override fun editar(endereco: Endereco): Boolean {
        val args = arrayOf(endereco.id_endereco.toString())

        val conteudo = ContentValues()
        conteudo.put("codigo_endereco", "Colocar o novo conteudo")
        conteudo.put("nome_endereco", "Colocar o novo conteudo")

        try {
            escrita.update(
                DatabaseHelper.TABELA_ENDERECOS,
                conteudo,
                "id_endereco = ?",
                args
            )
            Log.i("TAG", "Sucesso ao Atualizar:")
        } catch (e: Exception) {
            Log.i("TAG", "Erro ao Atualizar:")
            return false
        }
        return true
    }

    override fun deletar(endereco: String): Boolean {
        val args = arrayOf(endereco)

        try {

            escrita.delete(
                DatabaseHelper.TABELA_ITENS_CONTAGENS,
                "endereco_contagem = ?",
                args
            )

            escrita.delete(
                DatabaseHelper.TABELA_CABECALHO_CONTAGENS,
                "endereco_contagem = ?",
                args
            )

            escrita.delete(
                DatabaseHelper.TABELA_ENDERECOS,
                "codigo_endereco = ?",
                args
            )

            Log.i("ROLA", "endereco: ${endereco} Deletado")

            return true
        } catch (e: Exception) {
            Log.i("ROLA", "${e.message}")
            return false
        }
    }


    @SuppressLint("Range")
    override fun listar(): List<Endereco> {

        var listaEnderecos = mutableListOf<Endereco>()

        val sql = "SELECT * FROM ${DatabaseHelper.TABELA_ENDERECOS}"
        val cursor = leitura.rawQuery(sql, null)

        val indiceIdEndereco = cursor.getColumnIndex("id_endereco")
        val indiceCodigoEndereco = cursor.getColumnIndex("codigo_endereco")
        val indiceNomeEndereco = cursor.getColumnIndex("nome_endereco")
        val indiceStatus = cursor.getColumnIndex("status")
        val indiceData_abertura = cursor.getColumnIndex("data_abertura")
        val indiceData_fechado = cursor.getColumnIndex("data_fechado")

        while (cursor.moveToNext()) {
            val idEndereco = cursor.getInt(indiceIdEndereco)
            val codigoEndereco = cursor.getString(indiceCodigoEndereco)
            val nomeEndereco = cursor.getString(indiceNomeEndereco)
            val status = cursor.getString(indiceStatus)
            val data_abertura = cursor.getString(indiceData_abertura)
            val data_fechado = cursor.getString(indiceData_fechado)

            listaEnderecos.add(Endereco(idEndereco, codigoEndereco, nomeEndereco, status, data_abertura, data_fechado))
        }


        cursor.close()
        return listaEnderecos
    }

    override fun deletarTudo(idEndereco: Int): Boolean {
        try {


            escrita.delete(
                DatabaseHelper.TABELA_ITENS_CONTAGENS,
                null,
                null
            )

            escrita.delete(
                DatabaseHelper.TABELA_CABECALHO_CONTAGENS,
                null,
                null
            )

            escrita.delete(
                DatabaseHelper.TABELA_ENDERECOS,
                null,
                null
            )

            Log.i("TAG", "Sucesso ao Deletar tudo.")
        } catch (e: Exception) {
            Log.i("TAG", "Erro ao Deletar tudo.")
            return false
        }
        return true
    }

    override fun fecharEndereco(endereco: String, numeroContagem: String): Boolean {
        val args = arrayOf(endereco, numeroContagem)
        val conteudo = ContentValues()
        conteudo.put("status_contagem", "Fechado")

        try {
            escrita.update(
                DatabaseHelper.TABELA_CABECALHO_CONTAGENS,
                conteudo,
                " endereco_contagem = ? AND numero_contagem = ?",
                args
            )
            //Log.i("TAG", "Sucesso ao Atualizar:")
        } catch (e: Exception) {
            //Log.i("TAG", "Erro ao Atualizar:")
            return false
        }
        return true
    }

    override fun atualizarEnderecoFechado(endereco: String): Boolean {

        val args = arrayOf(endereco)
        val conteudo = ContentValues()
        conteudo.put(" status ", "Fechado")

        try {
            escrita.update(
                DatabaseHelper.TABELA_ENDERECOS,
                conteudo,
                " codigo_endereco = ? ",
                args
            )
        } catch (e: Exception) {
            return false
        }
        return true
    }

    override fun totalEnderecos(): Int {

        try {
            val sql = "SELECT COUNT(codigo_endereco) AS codigo_endereco FROM ${DatabaseHelper.TABELA_ENDERECOS}"
            val cursor = leitura.rawQuery(sql, null)

            if (cursor == null) {
                return 0
            }

            cursor.moveToFirst()
            val indexQtdEndereco = cursor.getColumnIndex("codigo_endereco")
            val quantidadeDeEndereco = cursor.getInt(indexQtdEndereco)
            cursor.close()
            return quantidadeDeEndereco

        }catch (e: Exception){
            e.printStackTrace()
            return 0
        }
    }

    override fun totalAbertos(): Int {
        val args = arrayOf("Aguardando contagens") // Aqui usamos um array para passar os argumentos
        val sql = "SELECT COUNT(codigo_endereco) AS codigo_endereco FROM ${DatabaseHelper.TABELA_ENDERECOS} where status = ?"

        try {
            val cursor = leitura.rawQuery(sql, args)

            cursor.moveToFirst()
            val indexQtdEnderecoAberto = cursor.getColumnIndex("codigo_endereco")
            val quantidadeDeEnderecoAbertos = cursor.getInt(indexQtdEnderecoAberto)

            cursor.close()
            return quantidadeDeEnderecoAbertos
        } catch (e: Exception) {
            // Lidar com a exceção aqui, por exemplo, imprimir um log ou retornar um valor padrão
            e.printStackTrace()
            return 0
        }
    }


    override fun totalFechados(): Int {
        val args = arrayOf("Fechado") // Aqui usamos um array para passar os argumentos
        val sql = "SELECT COUNT(codigo_endereco) AS codigo_endereco FROM ${DatabaseHelper.TABELA_ENDERECOS} where status = ?"

        try {
            val cursor = leitura.rawQuery(sql, args)

            cursor.moveToFirst()
            val indexQtdEnderecoFechado = cursor.getColumnIndex("codigo_endereco")
            val quantidadeDeEnderecoFechado = cursor.getInt(indexQtdEnderecoFechado)

            cursor.close()
            return quantidadeDeEnderecoFechado
        } catch (e: Exception) {
            // Lidar com a exceção aqui, por exemplo, imprimir um log ou retornar um valor padrão
            e.printStackTrace()
            return 0
        }
    }
}