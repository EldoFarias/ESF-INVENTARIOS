
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.farias.inventario.BancoDados.DatabaseHelper
import com.farias.inventario.BancoDados.IProdutoDAO
import com.farias.inventario.Modelos.ErroImportacaoProduto
import com.farias.inventario.Modelos.Produto

class ProdutoDAO(context: Context) : IProdutoDAO {

    // Propriedade estática para armazenar a única instância da classe
    companion object {
        @Volatile
        private var instance: ProdutoDAO? = null

        // Método estático para obter a instância única da classe
        fun getInstance(context: Context): ProdutoDAO {
            return instance ?: synchronized(this) {
                instance ?: ProdutoDAO(context.applicationContext).also { instance = it }
            }
        }
    }

    private val escrita = DatabaseHelper(context).writableDatabase
    private val leitura = DatabaseHelper(context).readableDatabase

    override fun salvarCompleto(produto: Produto): Boolean {
        val valores = ContentValues().apply {
            put("codigo_produto", produto.codigo_produto)
            put("codigo_auxiliar_produto", produto.codigo_auxiliar_produto)
            put("descricao_produto", produto.descricao_produto)
            put("data_cadastro", produto.data_cadastro)
        }

        return try {
            escrita.beginTransaction()
            escrita.insert(DatabaseHelper.TABELA_PRODUTOS, null, valores)
            escrita.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            Log.i("ROLA", "Erro ao Salvar: ${e.message}")
            false
        } finally {
            escrita.endTransaction()
        }
    }

    override fun salvarCodigo(produto: Produto): Boolean {
        val valores = ContentValues().apply {
            put("codigo_produto", produto.codigo_produto)
            put("codigo_auxiliar_produto", "")
            put("descricao_produto", produto.descricao_produto)
            put("data_cadastro", produto.data_cadastro)
        }

        return try {
            escrita.beginTransaction()
            escrita.insert(DatabaseHelper.TABELA_PRODUTOS, null, valores)
            escrita.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            Log.i("ROLA", "Erro ao Salvar: ${e.message}")
            false
        } finally {
            escrita.endTransaction()
        }
    }


    override fun salvarCodigoCodigoAuxiliar(produto: Produto): Boolean {
        val valores = ContentValues().apply {
            put("codigo_produto", produto.codigo_produto)
            put("codigo_auxiliar_produto", produto.codigo_auxiliar_produto)
            put("descricao_produto", produto.descricao_produto)
            put("data_cadastro", produto.data_cadastro)
        }

        return try {
            escrita.beginTransaction()
            escrita.insert(DatabaseHelper.TABELA_PRODUTOS, null, valores)
            escrita.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            Log.i("ROLA", "Erro ao Salvar: ${e.message}")
            false
        } finally {
            escrita.endTransaction()
        }
    }


    override fun salvarCodigoDescricao(produto: Produto): Boolean {
        val valores = ContentValues().apply {
            put("codigo_produto", produto.codigo_produto)
            put("codigo_auxiliar_produto", produto.codigo_auxiliar_produto)
            put("descricao_produto", produto.descricao_produto)
            put("data_cadastro", produto.data_cadastro)
        }

        return try {
            escrita.beginTransaction()
            escrita.insert(DatabaseHelper.TABELA_PRODUTOS, null, valores)
            escrita.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            Log.i("ROLA", "Erro ao Salvar: ${e.message}")
            false
        } finally {
            escrita.endTransaction()
        }
    }


    override fun editar(produto: Produto): Boolean {
        val args = arrayOf(produto.id_produto.toString())

        val conteudo = ContentValues()
        conteudo.put("codigo_produto", produto.codigo_produto)
        conteudo.put("codigo_auxiliar_produto", produto.codigo_auxiliar_produto)
        conteudo.put("descricao_produto", produto.descricao_produto)

        try {
            escrita.update(
                DatabaseHelper.TABELA_PRODUTOS,
                conteudo,
                "id_produto = ?",
                args
            )
            //Log.i("TAG", "Sucesso ao Atualizar:")
        } catch (e: Exception) {
            //Log.i("TAG", "Erro ao Atualizar:")
            return false
        }
        return true
    }

    override fun deletar(idProduto: Int): Boolean {
        val args = arrayOf(idProduto.toString())
        try {
            escrita.delete(
                DatabaseHelper.TABELA_PRODUTOS,
                "id_produto = ?",
                args
            )
            //Log.i("TAG", "Sucesso ao Deletar:")
        } catch (e: Exception) {
            //Log.i("TAG", "Erro ao Deletar:")
            return false
        }
        return true
    }

    override fun listar(): List<Produto> {
        var listaProdutos = mutableListOf<Produto>()
        val sql = "SELECT * FROM ${DatabaseHelper.TABELA_PRODUTOS}"

        val cursor = leitura.rawQuery(sql, null)

        val indiceId_produto = cursor.getColumnIndex("id_produto")
        val indiceCodigo_produto = cursor.getColumnIndex("codigo_produto")
        val indiceCodigo_auxiliar_produto = cursor.getColumnIndex("codigo_auxiliar_produto")
        val indiceDescricao_produto = cursor.getColumnIndex("descricao_produto")
        val indiceData_cadastro = cursor.getColumnIndex("data_cadastro")

        while (cursor.moveToNext()) {
            val idProduto = cursor.getInt(indiceId_produto)
            val codigoProduto = cursor.getString(indiceCodigo_produto)
            val codigoAuxiliarProduto = cursor.getString(indiceCodigo_auxiliar_produto)
            val descricaoProduto = cursor.getString(indiceDescricao_produto)
            val dataCadastroProduto = cursor.getString(indiceData_cadastro)

            listaProdutos.add(
                Produto(
                    idProduto,
                    codigoProduto,
                    codigoAuxiliarProduto,
                    descricaoProduto,
                    dataCadastroProduto
                )
            )
        }

        cursor.close()
        return listaProdutos
    }

    override fun deletarTudo(idProduto: Int): Boolean {
        try {
            escrita.delete(
                DatabaseHelper.TABELA_PRODUTOS,
                null,
                null
            )
            //Log.i("TAG", "Sucesso ao Deletar:")
        } catch (e: Exception) {
            //Log.i("TAG", "Erro ao Deletar:")
            return false
        }
        escrita.close()
        return true
    }

    override fun verificarDuplicadosImportacao( codigoProduto: String): Boolean {
        val args = arrayOf(codigoProduto)
        val sql = "SELECT * FROM ${DatabaseHelper.TABELA_PRODUTOS} WHERE codigo_produto = ?"
        val cursor = leitura.rawQuery(sql, args)

        cursor.moveToFirst()
        val count = cursor.count

        Log.i("TAPORRA", "${count} ")
        cursor.close()
        return count != 0
    }

    override fun salvarProdutosDuplicados(produto: ErroImportacaoProduto): Boolean {

        val valores = ContentValues()
        valores.put("linha_erro", produto.linha_erro)
        valores.put("codigo_produto", produto.codigo_produto)
        valores.put("data_cadastro", produto.data_cadastro)

        try {
            escrita.insert(
                DatabaseHelper.TABELA_ERROS_IMPORTACAO_PRODUTOS,
                null,
                valores
            )


        } catch (e: Exception) {

            return false
        }
        escrita.close()
        return true
    }

    override fun listarErrosImportacao(): List<ErroImportacaoProduto> {
        var listaProdutos = mutableListOf<ErroImportacaoProduto>()
        val sql = "SELECT * FROM ${DatabaseHelper.TABELA_ERROS_IMPORTACAO_PRODUTOS}"

        val cursor = leitura.rawQuery(sql, null)

        val indiceIdProduto = cursor.getColumnIndex("id_produto")
        val indiceLinha = cursor.getColumnIndex("linha_erro")
        val indiceCodigo_produto = cursor.getColumnIndex("codigo_produto")
        val indiceData_cadastro = cursor.getColumnIndex("data_cadastro")

        while (cursor.moveToNext()) {
            val idProduto = cursor.getInt(indiceIdProduto)
            val linha = cursor.getInt(indiceLinha)
            val codigoProduto = cursor.getString(indiceCodigo_produto)
            val dataCadastroProduto = cursor.getString(indiceData_cadastro)

            listaProdutos.add(
                ErroImportacaoProduto(
                    idProduto,
                    linha,
                    codigoProduto,
                    dataCadastroProduto
                )
            )
        }

        cursor.close()
        return listaProdutos
    }

    override fun verificarProduto(codigoProduto: String): String {
        var descricao = ""
        try {
            val args = arrayOf(codigoProduto)
            val sql =
                "SELECT descricao_produto FROM ${DatabaseHelper.TABELA_PRODUTOS} WHERE codigo_produto = ?"
            val cursor = leitura.rawQuery(sql, args)

            if (cursor.moveToFirst()) {
                val indexDescricao = cursor.getColumnIndex("descricao_produto")
                descricao = cursor.getString(indexDescricao)
                cursor.close()
            } else {
                descricao = "NÃO CADASTRADO"
                cursor.close()
            }

        } catch (e: Exception) {
            e.message
        }
        return descricao
    }

    override fun contarItens(): Int {

        val sql =
            "SELECT COUNT(codigo_produto) AS codigo_produto FROM ${DatabaseHelper.TABELA_PRODUTOS}"
        val cursor = leitura.rawQuery(sql, null)

        if (cursor == null) {
            return 0
        }

        cursor.moveToFirst()
        val indexQtdProdutos = cursor.getColumnIndex("codigo_produto")
        val quantidadeDeProdutos = cursor.getInt(indexQtdProdutos)

        cursor.close()
        return quantidadeDeProdutos

    }


    override fun limparListaErros(): Boolean {
        try {
            escrita.delete(
                DatabaseHelper.TABELA_ERROS_IMPORTACAO_PRODUTOS,
                null,
                null
            )
            //Log.i("TAG", "Sucesso ao Deletar:")
        } catch (e: Exception) {
            // Log.i("TAG", "Erro ao Deletar:")
            return false
        }
        escrita.close()
        return true
    }


    override fun importarBase(): Boolean {
        TODO("Not yet implemented")
    }



}
