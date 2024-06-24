package com.farias.inventario.BancoDados

import android.content.Context
import android.util.Log
import com.farias.inventario.Modelos.Itens
import com.farias.inventario.Modelos.Produto

class ItensDAO(context: Context) : IItensDAO {

    // Propriedade estática para armazenar a única instância da classe
    companion object {
        @Volatile
        private var instance: ItensDAO? = null

        // Método estático para obter a instância única da classe
        fun getInstance(context: Context): ItensDAO {
            return instance ?: synchronized(this) {
                instance ?: ItensDAO(context.applicationContext).also { instance = it }
            }
        }
    }

    private val escrita = DatabaseHelper(context).writableDatabase
    private val leitura = DatabaseHelper(context).readableDatabase


    override fun listarItens(endereco: String, nContagem: String): List<Itens> {

        val listaItens = mutableListOf<Itens>()

        try {
            val args = arrayOf(endereco, nContagem)
            val sql =
                "SELECT * FROM ${DatabaseHelper.TABELA_ITENS_CONTAGENS} WHERE endereco_contagem = ? and numero_contagem = ?"
            val cursor = leitura.rawQuery(sql, args)

            val idItemLido = cursor.getColumnIndex("id_item")
            val indiceEderecoContagem = cursor.getColumnIndex("endereco_contagem")
            val indiceNumeroContagem = cursor.getColumnIndex("numero_contagem")
            val indiceCodigoProduto = cursor.getColumnIndex("codigo_produto")
            val indiceDescricao = cursor.getColumnIndex("descricao_produto")
            val indiceQuantidade = cursor.getColumnIndex("quantidade")
            val indiceDataAberto = cursor.getColumnIndex("hora_abertura_contagem")
            val indiceDataFechado = cursor.getColumnIndex("hora_fechamento_contagem")
            val indiceCodigoOperador = cursor.getColumnIndex("codigo_operador")
            val indeceIdCabecalho = cursor.getColumnIndex("idControleCabecalho")


            while (cursor.moveToNext()) {
                val idItem = cursor.getInt(idItemLido)
                val endereco_ = cursor.getString(indiceEderecoContagem)
                val numeroContagem_ = cursor.getString(indiceNumeroContagem)
                val codigoProduto = cursor.getString(indiceCodigoProduto)
                val descricao = cursor.getString(indiceDescricao)
                val quantidade = cursor.getInt(indiceQuantidade)
                val dataAbertura = cursor.getString(indiceDataAberto)
                val dataFechado = cursor.getString(indiceDataFechado)
                val codigoOperador = cursor.getString(indiceCodigoOperador)
                val idCabecalho = cursor.getInt(indeceIdCabecalho)

                listaItens.add(
                    Itens(
                        idItem,
                        endereco_,
                        numeroContagem_,
                        codigoProduto,
                        descricao,
                        quantidade,
                        dataAbertura,
                        dataFechado,
                        codigoOperador,
                        idCabecalho
                    )
                )
            }

        } catch (e: Exception) {
            e.message
        }
        return listaItens
    }

    override fun listarItensResumido(endereco: String, numeroContagem: String): List<Itens> {
        val listaItens = mutableListOf<Itens>()

        try {
            val args = arrayOf(endereco, numeroContagem)
            val sql =
                "SELECT endereco_contagem, numero_contagem, codigo_produto, descricao_produto, SUM(quantidade) as quantidade " +
                        "FROM ${DatabaseHelper.TABELA_ITENS_CONTAGENS} WHERE endereco_contagem = ? and numero_contagem = ? " +
                        "GROUP BY endereco_contagem, numero_contagem, codigo_produto, descricao_produto"
            val cursor = leitura.rawQuery(sql, args)

            val indiceEderecoContagem = cursor.getColumnIndex("endereco_contagem")
            val indiceNumeroContagem = cursor.getColumnIndex("numero_contagem")
            val indiceCodigoProduto = cursor.getColumnIndex("codigo_produto")
            val indiceDescricao = cursor.getColumnIndex("descricao_produto")
            val indiceQuantidade = cursor.getColumnIndex("quantidade")

            while (cursor.moveToNext()) {

                val endereco_ = cursor.getString(indiceEderecoContagem)
                val numeroContagem_ = cursor.getString(indiceNumeroContagem)
                val codigoProduto = cursor.getString(indiceCodigoProduto)
                val descricao = cursor.getString(indiceDescricao)
                val quantidade = cursor.getInt(indiceQuantidade)

                listaItens.add(
                    Itens(
                        1,
                        endereco_,
                        numeroContagem_,
                        codigoProduto,
                        descricao,
                        quantidade,
                        "dataAbertura",
                        "dataFechado",
                        "codigoOperador",
                        1
                    )
                )
            }

        } catch (e: Exception) {
            e.message
        }
        return listaItens
    }

    override fun totalizarItensLidos(endereco: String, numeroContagem: String): Int {
        var totalQuantidade = 0
        val args = arrayOf(endereco, numeroContagem)

        val sql = "SELECT SUM(quantidade) as quantidade " +
                "FROM ${DatabaseHelper.TABELA_ITENS_CONTAGENS} " +
                "WHERE endereco_contagem = ? and numero_contagem = ? "

        val cursor = leitura.rawQuery(sql, args)
        if (cursor.moveToFirst()) {
            val indiceQuantidade = cursor.getColumnIndex("quantidade")
            totalQuantidade = cursor.getInt(indiceQuantidade)
        }

        return totalQuantidade
    }

    override fun totalizarReferencias(endereco: String, numeroContagem: String): Int {
        var totalQuantidade = 0
        val args = arrayOf(endereco, numeroContagem)

        val sql = "SELECT COUNT(DISTINCT codigo_produto) as quantidade " +
                "FROM ${DatabaseHelper.TABELA_ITENS_CONTAGENS} " +
                "WHERE endereco_contagem = ? and numero_contagem = ? "

        val cursor = leitura.rawQuery(sql, args)
        if (cursor.moveToFirst()) {
            val indiceQuantidade = cursor.getColumnIndex("quantidade")
            totalQuantidade = cursor.getInt(indiceQuantidade)
        }

        return totalQuantidade
    }
}