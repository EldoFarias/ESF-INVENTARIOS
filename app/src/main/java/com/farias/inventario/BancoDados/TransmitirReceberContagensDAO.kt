package com.farias.inventario.BancoDados

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.util.Log
import com.farias.inventario.BancoDados.DatabaseHelper.Companion.TABELA_CABECALHO_CONTAGENS
import com.farias.inventario.BancoDados.DatabaseHelper.Companion.TABELA_ITENS_CONTAGENS
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class TransmitirReceberContagensDAO(context: Context) : ITransmitirReceberContagensDAO {

    // Propriedade estática para armazenar a única instância da classe
    companion object {
        @Volatile
        private var instance: TransmitirReceberContagensDAO? = null

        // Método estático para obter a instância única da classe
        fun getInstance(context: Context): TransmitirReceberContagensDAO {
            return instance ?: synchronized(this) {
                instance ?: TransmitirReceberContagensDAO(context.applicationContext).also { instance = it }
            }
        }
    }

    private val escrita = DatabaseHelper(context).writableDatabase
    private val leitura = DatabaseHelper(context).readableDatabase

    override fun  receberContagens(): Boolean {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        val cabecalhoFile = File(downloadsDir, "CONTAGENS EXPORTADAS/CABECALHO_CONTAGENS.txt")
        val itensFile = File(downloadsDir, "CONTAGENS EXPORTADAS/ITENS_CONTAGENS.txt")

        if (!cabecalhoFile.exists() || !itensFile.exists()) {
            return false
        }

        val cabecalhoReader = cabecalhoFile.bufferedReader()
        val itensReader = itensFile.bufferedReader()

        escrita.beginTransaction()

        try {
            // Ler a primeira linha (cabeçalho) do arquivo de cabeçalho
            val linhaCabecalho = cabecalhoReader.readLine() ?: return false
            if (linhaCabecalho.isBlank() || !linhaCabecalho.contains(",")) {
                return false
            }

            // Ler linhas do arquivo de cabeçalho
            while (true) {
                val linhaCabecalho = cabecalhoReader.readLine() ?: break
                val dadosCabecalho = linhaCabecalho.split(",")
                if (dadosCabecalho.size < 9) {
                    // A linha não tem dados suficientes, pule para a próxima
                    continue
                }

                // Inserir dados do cabeçalho na tabela TABELA_CABECALHO_CONTAGENS
                val valoresCabecalho = ContentValues().apply {
                    put("id_controle_cabecalho", dadosCabecalho[1])
                    put("endereco_contagem", dadosCabecalho[2])
                    put("numero_contagem", dadosCabecalho[3])
                    put("codigo_operador", dadosCabecalho[4])
                    put("codigo_acesso", dadosCabecalho[5])
                    put("hora_abertura_contagem", dadosCabecalho[6])
                    put("hora_fechamento_contagem", dadosCabecalho[7])
                    put("status_contagem", dadosCabecalho[8])
                    put("identificador", dadosCabecalho[9])
                }
                escrita.insert(TABELA_CABECALHO_CONTAGENS, null, valoresCabecalho)

                // Ler linhas do arquivo de itens relacionadas ao cabeçalho atual
                while (true) {
                    val linhaItem = itensReader.readLine() ?: break
                    val dadosItem = linhaItem.split(",")
                    if (dadosItem.size < 10) {
                        // A linha não tem dados suficientes, pule para a próxima
                        continue
                    }

                    // Inserir dados do item na tabela TABELA_ITENS_CONTAGENS
                    val valoresItens = ContentValues().apply {
                        put("endereco_contagem", dadosItem[1])
                        put("numero_contagem", dadosItem[2])
                        put("codigo_produto", dadosItem[3])
                        put("descricao_produto", dadosItem[4])
                        put("quantidade", dadosItem[5])
                        put("hora_abertura_contagem", dadosItem[6])
                        put("hora_fechamento_contagem", dadosItem[7])
                        put("codigo_operador", dadosItem[8])
                        put("idControleCabecalho", dadosItem[9].toInt())
                    }
                    escrita.insert(TABELA_ITENS_CONTAGENS, null, valoresItens)
                }
            }
        } catch (e: Exception) {
            Log.e("TransmitirReceber", "Erro ao receber contagens: ${e.message}")
            return false
        } finally {
            cabecalhoReader.close()
            itensReader.close()
            escrita.setTransactionSuccessful()
            escrita.endTransaction()
        }
        return true
    }


    override fun transmitirContagens(): Boolean {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        // Criar pasta CONTAGENS EXPORTADAS se não existir
        val exportedFilesDir = File(downloadsDir, "CONTAGENS EXPORTADAS")
        if (!exportedFilesDir.exists()) {
            exportedFilesDir.mkdir()
        }
        // Abrir arquivos de escrita
        val cabecalhoFile = File(exportedFilesDir, "CABECALHO_CONTAGENS.txt")
        val itensFile = File(exportedFilesDir, "ITENS_CONTAGENS.txt")
        val cabecalhoWriter = cabecalhoFile.bufferedWriter()
        val itensWriter = itensFile.bufferedWriter()
        // Escrever cabeçalho do arquivo de cabeçalho
        //cabecalhoWriter.write("ID Cabeçalho,Endereço Contagem,Número Contagem,Código Operador,Código Acesso,Hora Abertura,Hora Fechamento,Status,Identificador")
        //cabecalhoWriter.newLine()
        // Obter dados do cabeçalho
        val cursorCabecalho = escrita.rawQuery(
            "SELECT * FROM ${TABELA_CABECALHO_CONTAGENS}", null )
        while (cursorCabecalho.moveToNext()) {
            val idCabecalho = cursorCabecalho.getInt(0)
            val idControleCabecalho = cursorCabecalho.getInt(1)
            val enderecoContagem = cursorCabecalho.getString(2)
            val numeroContagem = cursorCabecalho.getString(3)
            val codigoOperador = cursorCabecalho.getString(4)
            val codigoAcesso = cursorCabecalho.getString(5)
            val horaAbertura = cursorCabecalho.getString(6)
            val horaFechamento = cursorCabecalho.getString(7)
            val status = cursorCabecalho.getString(8)
            val identificador = cursorCabecalho.getString(9)

            // Escrever dados do cabeçalho no arquivo de cabeçalho
            cabecalhoWriter.write("$idCabecalho,${idControleCabecalho.toInt()},$enderecoContagem,$numeroContagem,$codigoOperador,$codigoAcesso,$horaAbertura,$horaFechamento,$status,$identificador")
            cabecalhoWriter.newLine()

            // Obter dados dos itens relacionados ao cabeçalho atual
            val cursorItens = escrita.rawQuery(
                "SELECT * FROM ${TABELA_ITENS_CONTAGENS}" , null )
            while (cursorItens.moveToNext()) {
                val idItem = cursorItens.getInt(0)
                val endereco = cursorItens.getString(1)
                val numeroContagemItem = cursorItens.getString(2)
                val codigoProduto = cursorItens.getString(3)
                val descricaoProduto = cursorItens.getString(4)
                val quantidade = cursorItens.getInt(5)
                val horaAberturaItem = cursorItens.getString(6)
                val horaFechamentoItem = cursorItens.getString(7)
                val codigoOperadorItem = cursorItens.getString(8)
                val idCabecalhoItem = cursorItens.getInt(9)

                // Escrever dados dos itens no arquivo de itens
                itensWriter.write("$idItem,$endereco,$numeroContagemItem,$codigoProduto,$descricaoProduto,$quantidade,$horaAberturaItem,$horaFechamentoItem,$codigoOperadorItem,$idCabecalhoItem")
                itensWriter.newLine()
            }
            cursorItens.close()
        }
        cursorCabecalho.close()

        // Fechar os arquivos
        cabecalhoWriter.close()
        itensWriter.close()

        return true
    }

}