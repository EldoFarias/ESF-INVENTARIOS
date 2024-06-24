package com.farias.inventario.BancoDados

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.farias.inventario.Modelos.Cabecalho
import com.farias.inventario.Modelos.Itens
import com.farias.inventario.Modelos.Produto

class ContagemDAO(context: Context): IContagemDAO {

    // Propriedade estática para armazenar a única instância da classe
    companion object {
        @Volatile
        private var instance: ContagemDAO? = null

        // Método estático para obter a instância única da classe
        fun getInstance(context: Context): ContagemDAO {
            return instance ?: synchronized(this) {
                instance ?: ContagemDAO(context.applicationContext).also { instance = it }
            }
        }
    }

    private val escrita = DatabaseHelper(context).writableDatabase
    private val leitura = DatabaseHelper(context).readableDatabase

    override fun inserirCabecalho(cabecalho: Cabecalho): Boolean {
        val valores = ContentValues().apply {
            put("endereco_contagem", cabecalho.endereco_contagem)
            put("id_controle_cabecalho", cabecalho.id_controlecabecalho)
            put("numero_contagem", cabecalho.numero_contagem)
            put("codigo_operador", cabecalho.codigo_operador)
            put("codigo_acesso", cabecalho.codigo_acesso)
            put("hora_abertura_contagem", cabecalho.hora_abertura_contagem)
            put("hora_fechamento_contagem", cabecalho.hora_fechamento_contagem)
            put("status_contagem", cabecalho.status_contagem)
            put("identificador", cabecalho.identificador)
        }

        try {
            escrita.beginTransaction()

            val idCabecalho = escrita.insert(DatabaseHelper.TABELA_CABECALHO_CONTAGENS, null, valores)

            if (idCabecalho == -1L) {
                // Ocorreu um erro ao inserir o cabeçalho, encerrar a transação e retornar falso
                escrita.endTransaction()
                return false
            }

            // Atualizar a coluna id_controle_cabecalho com o mesmo valor de id_cabecalho
            val valoresAtualizacao = ContentValues().apply {
                put("id_controle_cabecalho", "${idCabecalho}${cabecalho.codigo_acesso}")
            }
            val where = "id_cabecalho = ?"
            val args = arrayOf(idCabecalho.toString())

            escrita.update(DatabaseHelper.TABELA_CABECALHO_CONTAGENS, valoresAtualizacao, where, args)

            escrita.setTransactionSuccessful()

        } catch (e: Exception) {
            Log.e("TransmitirReceber", "Erro ao inserir cabeçalho: ${e.message}")
            return false
        } finally {
            escrita.endTransaction()
        }

        Log.i("ROLA", "inserirCabecalho - chave primaria = ${cabecalho.endereco_contagem} ")
        return true
    }


    override fun inserirItens(item: Itens): Boolean {
        val endereco = item.endereco_contagem
        val numeroContagem = item.numero_contagem
        val args = arrayOf(endereco, numeroContagem)
        var cabecalhoId = 0
        val sql = " SELECT id_controle_cabecalho FROM ${DatabaseHelper.TABELA_CABECALHO_CONTAGENS}" +
                " WHERE endereco_contagem = ? AND numero_contagem = ?"
        val cursor = leitura.rawQuery(sql, args)

        try {
            if (cursor.moveToFirst()) {
                val indexIdControleCabecalho = cursor.getColumnIndex("id_controle_cabecalho")
                cabecalhoId = cursor.getInt(indexIdControleCabecalho)
            } else {
                // Não há cabeçalho correspondente encontrado, encerrar a transação e retornar falso
                cursor.close()
                return false
            }
        } catch (e: Exception) {
            cursor.close()
            Log.e("TransmitirReceber", "Erro ao buscar ID do cabeçalho: ${e.message}")
            return false
        } finally {
            cursor.close()
        }

        val valores = ContentValues().apply {
            put("endereco_contagem", item.endereco_contagem)
            put("numero_contagem", item.numero_contagem)
            put("codigo_produto", item.codigo_produto)
            put("descricao_produto", item.descricao_produto)
            put("quantidade", item.quantidade)
            put("hora_abertura_contagem", item.hora_abertura_contagem)
            put("hora_fechamento_contagem", item.hora_fechamento_contagem)
            put("codigo_operador", item.codigo_operador)
            put("idControleCabecalho", cabecalhoId)
        }

        try {
            escrita.beginTransaction()

            escrita.insert(DatabaseHelper.TABELA_ITENS_CONTAGENS, null, valores)

            escrita.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("TransmitirReceber", "Erro ao inserir itens: ${e.message}")
            return false
        } finally {
            escrita.endTransaction()
        }

        Log.i("ROLA", "inserirItem - chave primaria = ${item.endereco_contagem} ")
        return true
    }


    override fun listarCabecalho(): List<Cabecalho> {
        var listaCabecalho = mutableListOf<Cabecalho>()
        val sql = "SELECT * FROM ${DatabaseHelper.TABELA_CABECALHO_CONTAGENS}"

        val cursor = leitura.rawQuery(sql, null)

        val indexIdCabecalho = cursor.getColumnIndex("id_cabecalho")
        val indexIdControleCabecalho = cursor.getColumnIndex("id_controle_cabecalho")
        val indexEnderecoContagem = cursor.getColumnIndex("endereco_contagem")
        val indexNumeroContagem = cursor.getColumnIndex("numero_contagem")
        val indexCodigoOperadordor = cursor.getColumnIndex("codigo_operador")
        val indexCodigoAcesso = cursor.getColumnIndex("codigo_acesso")
        val indexHoraAbertura = cursor.getColumnIndex("hora_abertura_contagem")
        val indexHorafechamento = cursor.getColumnIndex("hora_fechamento_contagem")
        val indexStatus = cursor.getColumnIndex("status_contagem")
        val indexidentificador = cursor.getColumnIndex("identificador")


        while (cursor.moveToNext()) {
            val idCabecalho = cursor.getInt(indexIdCabecalho)
            val idControleCabecalho = cursor.getInt(indexIdControleCabecalho)
            val enderecoContagem = cursor.getString(indexEnderecoContagem)
            val numeroContagem = cursor.getString(indexNumeroContagem)
            val codigoOperador = cursor.getString(indexCodigoOperadordor)
            val codigoAcesso = cursor.getString(indexCodigoAcesso)
            val horaAbertura = cursor.getString(indexHoraAbertura)
            val horaFechamento = cursor.getString(indexHorafechamento)
            val statusEndereco = cursor.getString(indexStatus)
            val identificador = cursor.getString(indexidentificador)


            listaCabecalho.add(
                Cabecalho( idCabecalho, idControleCabecalho, enderecoContagem, numeroContagem,
                    codigoOperador, codigoAcesso, horaAbertura,
                    horaFechamento, statusEndereco, identificador)
            )
        }

        cursor.close()
        return listaCabecalho
    }

    override fun listarItens(): List<Itens> {
        var listaItens = mutableListOf<Itens>()
        val sql = "SELECT  * FROM ${DatabaseHelper.TABELA_ITENS_CONTAGENS}"

        val cursor = leitura.rawQuery(sql, null)

        val indexIditem = cursor.getColumnIndex("id_item")
        val indexEnderecoContagem = cursor.getColumnIndex("endereco_contagem")
        val indexNumeroContagem = cursor.getColumnIndex("numero_contagem")
        val indexCodigoProduto = cursor.getColumnIndex("codigo_produto")
        val indexDescricaoProduto = cursor.getColumnIndex("descricao_produto")
        val indexQuantidade = cursor.getColumnIndex("quantidade")
        val indexHoraAbertura = cursor.getColumnIndex("hora_abertura_contagem")
        val indexHoraFechado = cursor.getColumnIndex("hora_fechamento_contagem")
        val indexCodigoOperador = cursor.getColumnIndex("codigo_operador")
        val indexidCabecalho = cursor.getColumnIndex("idControleCabecalho")

        while (cursor.moveToNext()) {

            val idItem = cursor.getInt(indexIditem)
            val enderecoContagem = cursor.getString(indexEnderecoContagem)
            val numeroContagem = cursor.getString(indexNumeroContagem)
            val codigoProduto = cursor.getString(indexCodigoProduto)
            val descricaoProduto = cursor.getString(indexDescricaoProduto)
            val quantidade = cursor.getInt(indexQuantidade)
            val horaAbertura = cursor.getString(indexHoraAbertura)
            val horaFechado = cursor.getString(indexHoraFechado)
            val codigoOperador = cursor.getString(indexCodigoOperador)
            val idCabecalho = cursor.getInt(indexidCabecalho)

            listaItens.add(Itens( idItem, enderecoContagem, numeroContagem,
                    codigoProduto, descricaoProduto, quantidade,
                    horaAbertura, horaFechado, codigoOperador, idCabecalho )
            )
        }

        cursor.close()
        return listaItens
    }

    override fun verificarStatusEndereco(endereco: String): List<Pair<String, String>> {
        val resultList = mutableListOf<Pair<String, String>>()

        val args = arrayOf(endereco)
        val sql = "SELECT status_contagem, numero_contagem " +
                " FROM ${DatabaseHelper.TABELA_CABECALHO_CONTAGENS}" +
                " WHERE endereco_contagem = ?"

        leitura.rawQuery(sql, args).use { cursor ->
            try {
                while (cursor.moveToNext()) {
                    val indexStaus = cursor.getColumnIndex("status_contagem")
                    val indexNumeroContagem = cursor.getColumnIndex("numero_contagem")

                    val status = cursor.getString(indexStaus)
                    val contagem = cursor.getString(indexNumeroContagem)

                    resultList.add(Pair(status, contagem))

                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
        return resultList
    }

    override fun verificarCabecalhoExiste( numeroContagem: String, endereco: String ): Boolean {

        var cabecalhoExiste = false

        val args = arrayOf(numeroContagem, endereco)

        val sql = "SELECT numero_contagem, endereco_contagem " +
                " FROM ${DatabaseHelper.TABELA_CABECALHO_CONTAGENS}" +
                " WHERE numero_contagem = ? AND endereco_contagem = ? "

        leitura.rawQuery(sql, args).use { cursor ->
            try {
                cabecalhoExiste = cursor.moveToFirst()
            } catch (e: Exception) {
                // Lógica para tratar a exceção, como logging ou notificação do usuário
                e.printStackTrace()
            }

        }
        return cabecalhoExiste
    }

    override fun deletarItemContagem(idItem: Int): Boolean {
        val args = arrayOf(idItem.toString())
        try {
            escrita.delete(
                DatabaseHelper.TABELA_ITENS_CONTAGENS,
                "id_item = ?",
                args
            )
            //Log.i("TAG", "Sucesso ao Deletar:")
        } catch (e: Exception) {
            //Log.i("TAG", "Erro ao Deletar:")
            return false
        }
        return true
    }

    override fun verificarEnderecoExiste(endereco: String): Boolean {

        var enderecoExiste = false
        val args = arrayOf(endereco)
        val sql = "SELECT codigo_endereco " +
                " FROM ${DatabaseHelper.TABELA_ENDERECOS}" +
                " WHERE codigo_endereco = ? "

        leitura.rawQuery(sql, args).use { cursor ->
            try {
                enderecoExiste = cursor.moveToFirst()
            } catch (e: Exception) {
                // Lógica para tratar a exceção, como logging ou notificação do usuário
                e.printStackTrace()
            }
        }

        return enderecoExiste
    }

    override fun somarItensContagem1(): Int {
        val args = arrayOf("1") // Aqui usamos um array para passar os argumentos
        val sql = "SELECT COUNT(codigo_produto) AS codigo_produto FROM ${DatabaseHelper.TABELA_ITENS_CONTAGENS} where numero_contagem = ?"

        try {
            val cursor = leitura.rawQuery(sql, args)

            cursor.moveToFirst()
            val indexQtdPecas1Contagem = cursor.getColumnIndex("codigo_produto")
            val quantidadePecas1Contagem = cursor.getInt(indexQtdPecas1Contagem)

            cursor.close()
            return quantidadePecas1Contagem
        } catch (e: Exception) {
            // Lidar com a exceção aqui, por exemplo, imprimir um log ou retornar um valor padrão
            e.printStackTrace()
            return 0
        }
    }

    override fun somarItensContagem2(): Int {
        val args = arrayOf("2") // Aqui usamos um array para passar os argumentos
        val sql = "SELECT COUNT(codigo_produto) AS codigo_produto FROM ${DatabaseHelper.TABELA_ITENS_CONTAGENS} where numero_contagem = ?"

        try {
            val cursor = leitura.rawQuery(sql, args)

            cursor.moveToFirst()
            val indexQtdPecas1Contagem = cursor.getColumnIndex("codigo_produto")
            val quantidadePecas1Contagem = cursor.getInt(indexQtdPecas1Contagem)

            cursor.close()
            return quantidadePecas1Contagem
        } catch (e: Exception) {
            // Lidar com a exceção aqui, por exemplo, imprimir um log ou retornar um valor padrão
            e.printStackTrace()
            return 0
        }
    }

    override fun somarItensContagem3(): Int {
        val args = arrayOf("3") // Aqui usamos um array para passar os argumentos
        val sql = "SELECT COUNT(codigo_produto) AS codigo_produto FROM ${DatabaseHelper.TABELA_ITENS_CONTAGENS} where numero_contagem = ?"

        try {
            val cursor = leitura.rawQuery(sql, args)

            cursor.moveToFirst()
            val indexQtdPecas1Contagem = cursor.getColumnIndex("codigo_produto")
            val quantidadePecas1Contagem = cursor.getInt(indexQtdPecas1Contagem)

            cursor.close()
            return quantidadePecas1Contagem
        } catch (e: Exception) {
            // Lidar com a exceção aqui, por exemplo, imprimir um log ou retornar um valor padrão
            e.printStackTrace()
            return 0
        }
    }

    override fun somarPecasContagem1(): Int {
        val args = arrayOf("1") // Aqui usamos um array para passar os argumentos
        val sql = "SELECT SUM(quantidade) AS quantidade FROM ${DatabaseHelper.TABELA_ITENS_CONTAGENS} where numero_contagem = ?"

        try {
            val cursor = leitura.rawQuery(sql, args)

            cursor.moveToFirst()
            val indexQtdPecas3Contagem = cursor.getColumnIndex("quantidade")
            val quantidadePecas3Contagem = cursor.getInt(indexQtdPecas3Contagem)

            cursor.close()
            return quantidadePecas3Contagem
        } catch (e: Exception) {
            // Lidar com a exceção aqui, por exemplo, imprimir um log ou retornar um valor padrão
            e.printStackTrace()
            return 0
        }
    }

    override fun somarPecasContagem2(): Int {
        val args = arrayOf("2") // Aqui usamos um array para passar os argumentos
        val sql = "SELECT SUM(quantidade) AS quantidade FROM ${DatabaseHelper.TABELA_ITENS_CONTAGENS} where numero_contagem = ?"

        try {
            val cursor = leitura.rawQuery(sql, args)

            cursor.moveToFirst()
            val indexQtdPecas3Contagem = cursor.getColumnIndex("quantidade")
            val quantidadePecas3Contagem = cursor.getInt(indexQtdPecas3Contagem)

            cursor.close()
            return quantidadePecas3Contagem
        } catch (e: Exception) {
            // Lidar com a exceção aqui, por exemplo, imprimir um log ou retornar um valor padrão
            e.printStackTrace()
            return 0
        }
    }

    override fun somarPecasContagem3(): Int {
        val args = arrayOf("3") // Aqui usamos um array para passar os argumentos
        val sql = "SELECT SUM(quantidade) AS quantidade FROM ${DatabaseHelper.TABELA_ITENS_CONTAGENS} where numero_contagem = ?"

        try {
            val cursor = leitura.rawQuery(sql, args)

            cursor.moveToFirst()
            val indexQtdPecas3Contagem = cursor.getColumnIndex("quantidade")
            val quantidadePecas3Contagem = cursor.getInt(indexQtdPecas3Contagem)

            cursor.close()
            return quantidadePecas3Contagem
        } catch (e: Exception) {
            // Lidar com a exceção aqui, por exemplo, imprimir um log ou retornar um valor padrão
            e.printStackTrace()
            return 0
        }
    }

    override fun deletarContagem(endereco: String, numeroContagem: Int): Boolean {
        val args = arrayOf( endereco, numeroContagem.toString())
        try {
            escrita.delete(
                DatabaseHelper.TABELA_ITENS_CONTAGENS,
                "endereco_contagem = ? AND numero_contagem = ?",
                args
            )

            escrita.delete(
                DatabaseHelper.TABELA_CABECALHO_CONTAGENS,
                "endereco_contagem = ? AND numero_contagem = ?",
                args
            )

        } catch (e: Exception) {
            Log.i("ROLA_ERRO", "Erro ao Deletar:")
            return false
        }
        return true
    }

}