package com.farias.inventario.BancoDados

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.farias.inventario.Utilidades.DadosUsuarioLogado
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.Charset

class ExportarTodasContagemDAO(context: Context) : IExportarTodasContagens {

    // Propriedade estática para armazenar a única instância da classe
    companion object {
        @Volatile
        private var instance: ExportarTodasContagemDAO? = null

        // Método estático para obter a instância única da classe
        fun getInstance(context: Context): ExportarTodasContagemDAO {
            return instance ?: synchronized(this) {
                instance ?: ExportarTodasContagemDAO(context.applicationContext).also { instance = it }
            }
        }
    }
    private val leitura = DatabaseHelper(context).readableDatabase
    override fun exportarLayoutInvMC(query: String, context: Context, uri: Uri): Boolean {
        var excecao: Exception? = null

        try {
            val cursor = leitura.rawQuery(query, null)
            cursor.use {
                val mapeamentoEnderecos = mutableMapOf<String, MutableList<String>>()
                val mapeamentoCabecalhos = mutableMapOf<String, String>()

                while (it.moveToNext()) {
                    val endereco = cursor.getString(1)
                    val numeroContagem = cursor.getString(3)
                    val chaveArquivo = "$endereco-$numeroContagem"

                    if (!mapeamentoEnderecos.containsKey(chaveArquivo)) {
                        mapeamentoEnderecos[chaveArquivo] = mutableListOf()
                        if (!mapeamentoCabecalhos.containsKey(chaveArquivo)) {
                            mapeamentoCabecalhos[chaveArquivo] = "*" +
                                    "EST0" + // Endereço com 4
                                    cursor.getString(1) + // Endereço com 4
                                    cursor.getString(3) + // Numero contagem
                                    cursor.getString(6) + // Codigo do Operador
                                    cursor.getString(7) + // numero do Usuario
                                    cursor.getString(11).substring(8, 10) + // Dia 1ª
                                    cursor.getString(11).substring(5, 7) +  // Mes 2ª
                                    cursor.getString(11).substring(0, 4) + // Ano 4 caracteresª
                                    cursor.getString(11).substring(11, 13) + // Hora
                                    cursor.getString(11).substring(14, 16) +  // Minuto
                                    cursor.getString(12).substring(8, 10) + // Dia 1ª
                                    cursor.getString(12).substring(5, 7) +  // Mes 2ª
                                    cursor.getString(12).substring(0, 4) + // Ano 4 caracteresª
                                    cursor.getString(12).substring(11, 13) + // Hora
                                    cursor.getString(12).substring(14, 16)   // Minuto
                        }
                    }

                    mapeamentoEnderecos[chaveArquivo]!!.add(
                        "!" +
                                cursor.getString(8) + "    " +
                                cursor.getString(10).padStart(6, '0')  // colocar mais 0 a esquerda
                    )
                }

                val contentResolver = context.contentResolver
                val rootDir = DocumentFile.fromTreeUri(context, uri)

                for (chaveArquivo in mapeamentoEnderecos.keys) {
                    val newFile = rootDir?.createFile("text/plain", "$chaveArquivo.txt")
                    newFile?.uri?.let { fileUri ->
                        contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                            outputStream.bufferedWriter().use { writer ->
                                writer.write(mapeamentoCabecalhos[chaveArquivo])
                                writer.newLine()
                                mapeamentoEnderecos[chaveArquivo]?.forEach { linha ->
                                    writer.write(linha)
                                    writer.newLine()
                                }
                            }
                        } ?: run {
                            Log.e("ROLA_ERRO_EXPORTACAO", "OutputStream is null")
                            return false
                        }
                    }
                }
            }
        } catch (e: IOException) {
            excecao = e
            Log.i("C6000", "CAIU NO CATCH: ${excecao.message}")
        }

        return excecao == null
    }
    override fun exportarCodigosQuantidades(query: String, delimitador: String, uri: Uri, context: Context): Boolean {
        var excecao: Exception? = null

        try {
            val cursor = leitura.rawQuery(query, null)
            cursor.use {
                val listaDados = mutableListOf<String>()

                while (it.moveToNext()) {
                    val codigo = cursor.getString(0)
                    val quantidade = cursor.getInt(2)

                    listaDados.add("${codigo}${delimitador}$quantidade")
                }

                Log.e("ROLA_EXPORTACAO", "${cursor.count}")

                val contentResolver = context.contentResolver
                val rootDir = DocumentFile.fromTreeUri(context, uri)

                // Cria um nome único para o arquivo exportado
                val newFileName = "ExportacaoCodigosQuantidades.txt"

                // Cria o novo arquivo no diretório selecionado
                val newFile = rootDir?.createFile("text/txt", newFileName)
                newFile?.uri?.let {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.bufferedWriter().use { writer ->
                            listaDados.forEach { linha ->
                                writer.write(linha)
                                writer.newLine()
                            }
                        }
                    } ?: run {
                        Log.e("ROLA_ERRO_EXPORTACAO", "OutputStream is null")
                        return false
                    }
                }
            }
        } catch (e: IOException) {
            excecao = e
        }

        return excecao == null
    }
    override fun exportarCodigoDescricaoQuantidade(query: String, delimitador: String, uri: Uri, context: Context): Boolean {
        var excecao: Exception? = null

        try {
            val cursor = leitura.rawQuery(query, null)
            cursor.use {
                val listaDados = mutableListOf<String>()

                while (it.moveToNext()) {
                    val codigo = cursor.getString(0)
                    val descricao = cursor.getString(1)
                    val quantidade = cursor.getString(2)

                    listaDados.add("${codigo}${delimitador}${descricao}${delimitador}${quantidade}")
                }

                val contentResolver = context.contentResolver
                val rootDir = DocumentFile.fromTreeUri(context, uri)

                // Cria um nome único para o arquivo exportado
                val newFileName = "ExportacaoCodigoDescricaoQuantidade.txt"

                // Cria o novo arquivo no diretório selecionado
                val newFile = rootDir?.createFile("text/txt", newFileName)
                newFile?.uri?.let {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.bufferedWriter().use { writer ->
                            listaDados.forEach { linha ->
                                writer.write(linha)
                                writer.newLine()
                            }
                        }
                    } ?: run {
                        Log.e("ROLA_ERRO_EXPORTACAO", "OutputStream is null")
                        return false
                    }
                }
            }
        } catch (e: IOException) {
            excecao = e
        }

        return excecao == null
    }

}