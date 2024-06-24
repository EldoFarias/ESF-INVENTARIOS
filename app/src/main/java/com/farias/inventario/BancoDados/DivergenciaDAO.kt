import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.farias.inventario.BancoDados.DatabaseHelper
import com.farias.inventario.BancoDados.IDivergenciaDAO
import com.farias.inventario.Modelos.Divergencia
import com.farias.inventario.Modelos.DivergenciaPorContagem
import com.farias.inventario.Modelos.Endereco
import com.farias.inventario.Modelos.EnderecoReduzido
import com.farias.inventario.Modelos.toContentValues

class DivergenciaDAO(context: Context) : IDivergenciaDAO {

    // Propriedade estática para armazenar a única instância da classe
    companion object {
        @Volatile
        private var instance: DivergenciaDAO? = null

        // Método estático para obter a instância única da classe
        fun getInstance(context: Context): DivergenciaDAO {
            return instance ?: synchronized(this) {
                instance ?: DivergenciaDAO(context.applicationContext).also { instance = it }
            }
        }
    }

    private val escrita = DatabaseHelper(context).writableDatabase
    private val leitura = DatabaseHelper(context).readableDatabase

    @SuppressLint("Range")
    override fun compararDivergencia(): Boolean {

        var numeroDivergencia = 0
        var cursor: Cursor? = null
        try {

            val sql = "SELECT * FROM ${DatabaseHelper.TABELA_ITENS_CONTAGENS}"
            cursor = leitura.rawQuery(sql, null)

            // Percorre os registros do cursor
            while (cursor.moveToNext()) {
                numeroDivergencia += 1
                // Cria um novo registro para a tabela de divergências
                val divergencia = Divergencia(
                    cursor.getString(cursor.getColumnIndex("endereco_contagem")),
                    cursor.getString(cursor.getColumnIndex("codigo_produto")),
                    cursor.getString(cursor.getColumnIndex("descricao_produto"))
                )

                // Divide a quantidade do produto em várias colunas
                val quantidade = cursor.getInt(cursor.getColumnIndex("quantidade"))
                if (quantidade >= 1) {
                    for (i in 1..quantidade) {
                        // Verifica o número da contagem
                        val numeroContagem = cursor.getInt(cursor.getColumnIndex("numero_contagem"))
                        when (numeroContagem) {
                            1 -> divergencia.quantidadeContagem1 = quantidade
                            2 -> divergencia.quantidadeContagem2 = quantidade
                            3 -> divergencia.quantidadeContagem3 = quantidade
                            else -> {
                                // Não é necessário fazer nada para as demais colunas
                            }
                        }
                    }
                    // Aqui eu vou inserir na tabela
                    inserirDivergencia(divergencia)
                }
            }

            if (numeroDivergencia > 0){
                return true
                cursor.close()
            }else{
                return false
                cursor.close()
            }

        } catch (e: Exception) {
            return false
        } finally {
            // Feche o cursor
            cursor?.close()
        }
    }

    fun inserirDivergencia(divergencia: Divergencia) {

        try {
            val contentValues = divergencia.toContentValues()
            escrita.insert(DatabaseHelper.TABELA_DIVERGENCIAS, null, contentValues)
            Log.i("inserirDivergencia", "Sucesso ao inserir ")
        } catch (e: Exception) {
            val erro = e.message.toString()
            Log.i("inserirDivergencia", "${erro}: ")
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun listarDivergencias(): List<Divergencia> {
        var listaDivergencia = mutableListOf<Divergencia>()
        // val sql = "SELECT * FROM ${DatabaseHelper.TABELA_DIVERGENCIAS}"
        val sql =
            "SELECT endereco_divergencia, codigo_produto_divergencia, descricao_produto_divergencia," +
                    " SUM(quantidade1) AS quantidade1," +
                    " SUM(quantidade2) AS quantidade2," +
                    " SUM(quantidade3) AS quantidade3" +
                    " FROM ${DatabaseHelper.TABELA_DIVERGENCIAS}" +
                    " GROUP BY endereco_divergencia, codigo_produto_divergencia, descricao_produto_divergencia"

        val cursor = leitura.rawQuery(sql, null)

        //val indiceIdDivergencia = cursor.getColumnIndex("id_divergencia")
        val indiceEnderecoDivergencia = cursor.getColumnIndex("endereco_divergencia")
        val indiceCodigoDivergencia = cursor.getColumnIndex("codigo_produto_divergencia")
        val indiceDescricaoProduto = cursor.getColumnIndex("descricao_produto_divergencia")
        val indiceQtd1 = cursor.getColumnIndex("quantidade1")
        val indiceQtd2 = cursor.getColumnIndex("quantidade2")
        val indiceQtd3 = cursor.getColumnIndex("quantidade3")


        while (cursor.moveToNext()) {
            //val idDivergencia = cursor.getInt(indiceIdDivergencia)
            val enderecoDivergencia = cursor.getString(indiceEnderecoDivergencia)
            val codigoDivergencia = cursor.getString(indiceCodigoDivergencia)
            val descricaoDivergencia = cursor.getString(indiceDescricaoProduto)
            val qtd1 = cursor.getInt(indiceQtd1)
            val qtd2 = cursor.getInt(indiceQtd2)
            val qtd3 = cursor.getInt(indiceQtd3)

            listaDivergencia.add(
                Divergencia(
                    enderecoDivergencia, codigoDivergencia, descricaoDivergencia,
                    qtd1, qtd2, qtd3
                )
            )
        }

        cursor.close()
        return listaDivergencia
    }


    override fun deletarListaDivergencia(): Boolean {
        try {
            val sql = "DELETE FROM ${DatabaseHelper.TABELA_DIVERGENCIAS}"
            escrita.execSQL(sql)

        } catch (e: Exception) {
            return false
        }
        return true
    }

    override fun filtrarDivergencias(endereco: String): List<Divergencia> {
        var listaDivergencia = mutableListOf<Divergencia>()
        val args = arrayOf(endereco)

        try {
            val sql =
                "SELECT endereco_divergencia, codigo_produto_divergencia, descricao_produto_divergencia," +
                        " SUM(quantidade1) AS quantidade1," +
                        " SUM(quantidade2) AS quantidade2," +
                        " SUM(quantidade3) AS quantidade3" +
                        " FROM ${DatabaseHelper.TABELA_DIVERGENCIAS}" +
                        " WHERE endereco_divergencia = ?" +
                        " GROUP BY endereco_divergencia, codigo_produto_divergencia, descricao_produto_divergencia"


            val cursor = leitura.rawQuery(sql, args)


            //val indiceIdDivergencia = cursor.getColumnIndex("id_divergencia")
            val indiceEnderecoDivergencia = cursor.getColumnIndex("endereco_divergencia")
            val indiceCodigoDivergencia = cursor.getColumnIndex("codigo_produto_divergencia")
            val indiceDescricaoProduto = cursor.getColumnIndex("descricao_produto_divergencia")
            val indiceQtd1 = cursor.getColumnIndex("quantidade1")
            val indiceQtd2 = cursor.getColumnIndex("quantidade2")
            val indiceQtd3 = cursor.getColumnIndex("quantidade3")


            while (cursor.moveToNext()) {
                //val idDivergencia = cursor.getInt(indiceIdDivergencia)
                val enderecoDivergencia = cursor.getString(indiceEnderecoDivergencia)
                val codigoDivergencia = cursor.getString(indiceCodigoDivergencia)
                val descricaoDivergencia = cursor.getString(indiceDescricaoProduto)
                val qtd1 = cursor.getInt(indiceQtd1)
                val qtd2 = cursor.getInt(indiceQtd2)
                val qtd3 = cursor.getInt(indiceQtd3)

                listaDivergencia.add(
                    Divergencia(
                        enderecoDivergencia, codigoDivergencia, descricaoDivergencia,
                        qtd1, qtd2, qtd3
                    )
                )
            }
            cursor.close()
        } catch (e: Exception) {

            return listaDivergencia

        }
        return listaDivergencia
    }

    override fun gerarPDF(): List<Divergencia> {
        var listaDivergencia = mutableListOf<Divergencia>()
        //val args = arrayOf(endereco)

        try {
            val sql =
                "SELECT endereco_divergencia, codigo_produto_divergencia, descricao_produto_divergencia," +
                        " SUM(quantidade1) AS quantidade1," +
                        " SUM(quantidade2) AS quantidade2," +
                        " SUM(quantidade3) AS quantidade3" +
                        " FROM ${DatabaseHelper.TABELA_DIVERGENCIAS}" +
                        " GROUP BY endereco_divergencia, codigo_produto_divergencia, descricao_produto_divergencia"


            val cursor = leitura.rawQuery(sql, null)


            //val indiceIdDivergencia = cursor.getColumnIndex("id_divergencia")
            val indiceEnderecoDivergencia = cursor.getColumnIndex("endereco_divergencia")
            val indiceCodigoDivergencia = cursor.getColumnIndex("codigo_produto_divergencia")
            val indiceDescricaoProduto = cursor.getColumnIndex("descricao_produto_divergencia")
            val indiceQtd1 = cursor.getColumnIndex("quantidade1")
            val indiceQtd2 = cursor.getColumnIndex("quantidade2")
            val indiceQtd3 = cursor.getColumnIndex("quantidade3")


            while (cursor.moveToNext()) {
                //val idDivergencia = cursor.getInt(indiceIdDivergencia)
                val enderecoDivergencia = cursor.getString(indiceEnderecoDivergencia)
                val codigoDivergencia = cursor.getString(indiceCodigoDivergencia)
                val descricaoDivergencia = cursor.getString(indiceDescricaoProduto)
                val qtd1 = cursor.getInt(indiceQtd1)
                val qtd2 = cursor.getInt(indiceQtd2)
                val qtd3 = cursor.getInt(indiceQtd3)

                listaDivergencia.add(
                    Divergencia(
                        enderecoDivergencia, codigoDivergencia, descricaoDivergencia,
                        qtd1, qtd2, qtd3
                    )
                )
            }
            cursor.close()
        } catch (e: Exception) {

            return listaDivergencia

        }
        return listaDivergencia
    }

    override fun gerarPDFPorContagem(contagem: Int): List<DivergenciaPorContagem> {  //Receber o numero da conatgem aqui como paramentro.

        var listaDivergencia = mutableListOf<DivergenciaPorContagem>()
        //val args = arrayOf(endereco)

        try {
            val sql =
                "SELECT endereco_divergencia, codigo_produto_divergencia, descricao_produto_divergencia," +
                        " SUM(quantidade$contagem) AS quantidade$contagem" +
                        " FROM ${DatabaseHelper.TABELA_DIVERGENCIAS}" +
                        " GROUP BY endereco_divergencia, codigo_produto_divergencia, descricao_produto_divergencia"


            val cursor = leitura.rawQuery(sql, null)

            //val indiceIdDivergencia = cursor.getColumnIndex("id_divergencia")
            val indiceEnderecoDivergencia = cursor.getColumnIndex("endereco_divergencia")
            val indiceCodigoDivergencia = cursor.getColumnIndex("codigo_produto_divergencia")
            val indiceDescricaoProduto = cursor.getColumnIndex("descricao_produto_divergencia")
            val indiceQtd = cursor.getColumnIndex("quantidade$contagem")

            while (cursor.moveToNext()) {
                //val idDivergencia = cursor.getInt(indiceIdDivergencia)
                val enderecoDivergencia = cursor.getString(indiceEnderecoDivergencia)
                val codigoDivergencia = cursor.getString(indiceCodigoDivergencia)
                val descricaoDivergencia = cursor.getString(indiceDescricaoProduto)
                val qtd = cursor.getInt(indiceQtd)

                listaDivergencia.add(
                    DivergenciaPorContagem(
                        enderecoDivergencia, codigoDivergencia, descricaoDivergencia,
                        qtd
                    )
                )
            }
            cursor.close()
        } catch (e: Exception) {
            return listaDivergencia
        }
        return listaDivergencia
    }

    override fun gerarPDFEnderecosNaoContados(): Pair<List<EnderecoReduzido>, Int> {

        var listaEnderecos = mutableListOf<EnderecoReduzido>()
        var qtdEnderecos = 0
        val args = arrayOf("Aguardando contagens")

        try {
            val sql = "SELECT codigo_endereco, nome_endereco  FROM ${DatabaseHelper.TABELA_ENDERECOS} WHERE status = ?"
            val cursor = leitura.rawQuery(sql, args)

            val indiceCodigoEndereco = cursor.getColumnIndex("codigo_endereco")
            val indiceNomeEndereco = cursor.getColumnIndex("nome_endereco")

            while (cursor.moveToNext()) {
                val codigoEndereco = cursor.getString(indiceCodigoEndereco)
                val nomeEndereco = cursor.getString(indiceNomeEndereco)

                listaEnderecos.add(EnderecoReduzido(codigoEndereco, nomeEndereco))
                qtdEnderecos ++
            }
            cursor.close()
        } catch (e: Exception) {
            return Pair(listaEnderecos, qtdEnderecos)
        }
        return Pair(listaEnderecos, qtdEnderecos)
    }
}
