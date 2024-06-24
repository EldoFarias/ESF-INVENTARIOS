package com.farias.inventario.PDFs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.farias.inventario.Modelos.Divergencia
import com.farias.inventario.Modelos.DivergenciaPorContagem
import com.farias.inventario.Modelos.EnderecoReduzido
import com.farias.inventario.R
import com.farias.inventario.Utilidades.ClasseCabecalhoRelatorios
import com.farias.inventario.Utilidades.ClasseConteudoRelatorios
import com.farias.inventario.Utilidades.ClasseTitulosRelatorios
import com.farias.inventario.Utilidades.exibirMensagem
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Table
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import java.io.File

class GerarRelatorios private constructor(private val context: Context) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: GerarRelatorios? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(context: Context): GerarRelatorios {
            return instance ?: synchronized(this) {
                instance ?: GerarRelatorios(context.applicationContext).also { instance = it }
            }
        }
    }

    fun gerarPDF(listaDivergencia: List<Divergencia>, uri: Uri): Boolean {
        return try {
            val contentResolver = context.contentResolver
            val rootDir = DocumentFile.fromTreeUri(context, uri)

            // Cria um nome único para o arquivo PDF
            val newFileName = "RelatorioDivergenciaGeral.pdf"

            // Cria o novo arquivo no diretório selecionado
            val newFile = rootDir?.createFile("application/pdf", newFileName)
            newFile?.uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    val pdfWriter = PdfWriter(outputStream)
                    val pdfDocument = PdfDocument(pdfWriter)
                    val document = Document(pdfDocument, PageSize.A4) // Orientação paisagem

                    // Convertê-lo para um Bitmap
                    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.contagem_background)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    val imagemByteArray = byteArrayOutputStream.toByteArray()
                    byteArrayOutputStream.close()

                    val imagemMarcaDagua = Image(ImageDataFactory.create(imagemByteArray))
                    imagemMarcaDagua.scaleToFit(50f, 50f) // Ajuste o tamanho conforme necessário
                    imagemMarcaDagua.setOpacity(.5f)
                    document.add(imagemMarcaDagua)

                    // Adicionar cabeçalho para o endereço
                    val title = ClasseTitulosRelatorios(
                        "RELATÓRIO CONTAGEM GERAL"
                    )
                    document.add(title)

                    // Criar a tabela com 5 colunas
                    val table = Table(floatArrayOf(1f, 2f, 0.5f, 0.5f, 0.5f)) // Largura das colunas
                    table.useAllAvailableWidth()

                    // Adicionar cabeçalhos da tabela para as 5 colunas
                    val headerFontSize = 11f // Definir o tamanho da fonte para os cabeçalhos
                    table.addCell(ClasseCabecalhoRelatorios("Código", headerFontSize))
                    table.addCell(ClasseCabecalhoRelatorios("Descrição", headerFontSize))
                    table.addCell(ClasseCabecalhoRelatorios("1º Contagem", headerFontSize))
                    table.addCell(ClasseCabecalhoRelatorios("2º Contagem", headerFontSize))
                    table.addCell(ClasseCabecalhoRelatorios("3º Contagem", headerFontSize))

                    // Adicionar dados da lista à tabela
                    val dataFontSize = 10f // Definir o tamanho da fonte para os dados
                    for (divergencia in listaDivergencia) {
                        table.addCell(ClasseConteudoRelatorios(divergencia.codigoProdutoDivergencia, dataFontSize))
                        table.addCell(ClasseConteudoRelatorios(divergencia.descricao, dataFontSize))
                        table.addCell(ClasseConteudoRelatorios(divergencia.quantidadeContagem1.toString(), dataFontSize))
                        table.addCell(ClasseConteudoRelatorios(divergencia.quantidadeContagem2.toString(), dataFontSize))
                        table.addCell(ClasseConteudoRelatorios(divergencia.quantidadeContagem3.toString(), dataFontSize))
                    }

                    document.add(table)
                    document.close()
                } ?: run {
                    Log.e("ROLA_ERRO_PDF", "OutputStream is null")
                    return false
                }
            }
            true
        } catch (e: Exception) {
            Log.e("ROLA_PDF", "Error generating PDF: ${e.message}")
            false
        }
    }

    fun gerarPDFPorContagem(listaDivergencia: List<DivergenciaPorContagem>, numeroContagem: Int, uri: Uri): Boolean {
        var nomeDaContagem = when (numeroContagem) {
            1 -> "PRIMEIRA"
            2 -> "SEGUNDA"
            3 -> "TERCEIRA"
            else -> ""
        }

        return try {
            // Obtenha o ContentResolver
            val contentResolver = context.contentResolver
            val rootDir = DocumentFile.fromTreeUri(context, uri)

            val groupedByEndereco = listaDivergencia.groupBy { it.enderecoDivergencia }
            var contador = 0

            for ((endereco, divergencias) in groupedByEndereco) {
                contador++  // Incrementa o contador para cada grupo de endereço

                // Cria um nome único para cada arquivo PDF
                val newFileName = "Relatorio_${nomeDaContagem}_Contagem_$contador.pdf"

                // Cria o novo arquivo no diretório selecionado
                val newFile = rootDir?.createFile("application/pdf", newFileName)
                newFile?.uri?.let {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        val pdfWriter = PdfWriter(outputStream)
                        val pdfDocument = PdfDocument(pdfWriter)
                        val document = Document(pdfDocument, PageSize.A4) // Orientação paisagem

                        // Convertê-lo para um Bitmap
                        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.contagem_background)
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                        val imagemByteArray = byteArrayOutputStream.toByteArray()
                        byteArrayOutputStream.close()

                        val imagemMarcaDagua = Image(ImageDataFactory.create(imagemByteArray))
                        imagemMarcaDagua.scaleToFit(50f, 50f) // Ajuste o tamanho conforme necessário
                        imagemMarcaDagua.setOpacity(.5f)
                        document.add(imagemMarcaDagua)

                        // Adicionar cabeçalho para o endereço
                        val title = ClasseTitulosRelatorios(
                            "Endereço - $endereco \n RELATÓRIO ${nomeDaContagem} CONTAGEM"
                        )
                        document.add(title)

                        // Criar a tabela com 3 colunas
                        val table = Table(floatArrayOf(1f, 2f, 0.5f)) // Largura das colunas
                        table.useAllAvailableWidth()

                        // Adicionar cabeçalhos da tabela para as 5 colunas
                        val headerFontSize = 11f // Definir o tamanho da fonte para os cabeçalhos
                        table.addCell(ClasseCabecalhoRelatorios("Código", headerFontSize))
                        table.addCell(ClasseCabecalhoRelatorios("Descrição", headerFontSize))
                        table.addCell(ClasseCabecalhoRelatorios("Quantidade", headerFontSize))

                        // Adicionar dados da lista à tabela
                        val dataFontSize = 10f // Definir o tamanho da fonte para os dados
                        for (divergencia in divergencias) {
                            table.addCell(ClasseConteudoRelatorios(divergencia.codigoProdutoDivergencia, dataFontSize))
                            table.addCell(ClasseConteudoRelatorios(divergencia.descricao, dataFontSize))
                            table.addCell(ClasseConteudoRelatorios(divergencia.quantidadeContagem1.toString(), dataFontSize))
                        }

                        // Adicionar a tabela ao documento
                        document.add(table)
                        document.close()
                    } ?: run {
                        Log.e("ROLA_ERRO_PDF", "OutputStream is null")
                        return false
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e("ROLA_PDF", "Error generating PDF: ${e.message}")
            false
        }
    }



    fun createUniqueFileUri(fileName: String): Uri {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Relatorios")
        }
        return resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)!!
    }


    fun gerarPdfEnderecosNaoContados(pair: Pair<List<EnderecoReduzido>, Int>, uri: Uri): Boolean {
        val (listaEnderecos, qtdEnderecos) = pair
        try {
            // Obtenha o OutputStream do URI usando ContentResolver
            val contentResolver = context.contentResolver
            val outputStream = contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                val pdfWriter = PdfWriter(outputStream)
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument, PageSize.A4) // Orientação paisagem

                // Convertê-lo para um Bitmap
                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.contagem_background)
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val imagemByteArray = byteArrayOutputStream.toByteArray()
                byteArrayOutputStream.close()

                val imagemMarcaDagua = Image(ImageDataFactory.create(imagemByteArray))
                imagemMarcaDagua.scaleToFit(50f, 50f) // Ajuste o tamanho conforme necessário
                imagemMarcaDagua.setOpacity(.5f)
                document.add(imagemMarcaDagua)

                // Adicionar título para o endereço
                val title = ClasseTitulosRelatorios(
                    "ENDEREÇOS NÃO COLETADOS \n" +
                            "Atualmente: $qtdEnderecos endereços."
                )
                document.add(title)

                // Criar a tabela com 2 colunas (ajustado para o número de colunas necessário)
                val table = Table(floatArrayOf(1f, 2f)) // Largura das colunas
                table.useAllAvailableWidth()

                // Adicionar cabeçalhos da tabela para as 2 colunas
                val headerFontSize = 11f // Definir o tamanho da fonte para os cabeçalhos
                table.addCell(ClasseCabecalhoRelatorios("Local Endereço", headerFontSize))
                table.addCell(ClasseCabecalhoRelatorios("Endereço", headerFontSize))

                // Adicionar dados da lista à tabela
                val dataFontSize = 10f // Definir o tamanho da fonte para os dados
                for (endereco in listaEnderecos) {
                    table.addCell(ClasseConteudoRelatorios(endereco.nome_endereco, dataFontSize))
                    table.addCell(ClasseConteudoRelatorios(endereco.codigo_endereco, dataFontSize))
                }

                // Adicionar a tabela ao documento
                document.add(table)
                document.close()
                Log.i("ROLA_ERRO_PDF", "ACABOU DE RODAR")

                // Fechar o OutputStream
                outputStream.close()

            } else {
                Log.e("ROLA_ERRO_PDF", "OutputStream is null")
                return false
            }
        } catch (e: Exception) {
            Log.e("ROLA_PDF", "Erro ao gerar PDF: ${e.message}")
            return false
        }

        return true
    }

}