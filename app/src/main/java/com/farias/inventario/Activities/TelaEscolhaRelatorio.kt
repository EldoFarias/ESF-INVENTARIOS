package com.farias.inventario.Activities

import DivergenciaDAO
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.farias.inventario.PDFs.GerarRelatorios
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaEscolhaRelatorioBinding

class TelaEscolhaRelatorio : AppCompatActivity() {

    private var numeroContagem: Int = 0
    private lateinit var createDocumentLauncherPdfGeral: ActivityResultLauncher<Intent>
    private lateinit var selecionarDiretorioLauncher: ActivityResultLauncher<Intent>
    private lateinit var createDocumentLauncherPdfEnderecosNaoContados: ActivityResultLauncher<Intent>

    private val binding by lazy {
        ActivityTelaEscolhaRelatorioBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarRelatorios()
        inicializarToolbar()
        inicializarEventosClick()
    }

    private fun criarRelatorioGeral() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        createDocumentLauncherPdfGeral.launch(intent)
    }

    private fun selecionarDiretorio() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        selecionarDiretorioLauncher.launch(intent)
    }

    private fun gerarEnderecosNaoColetados() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TITLE, "EnderecosNaoContados.PDF")
        }
        createDocumentLauncherPdfEnderecosNaoContados.launch(intent)
    }

    private fun inicializarEventosClick() {
        binding.btnGerarRelatorioContagemGeral.setOnClickListener {
            criarRelatorioGeral()
        }

        binding.btnGerarPrimeiraContagem.setOnClickListener {
            numeroContagem = 1
            selecionarDiretorio()
        }

        binding.btnGerarSegundaContagem.setOnClickListener {
            numeroContagem = 2
            selecionarDiretorio()
        }

        binding.btnGerarTerceiraContagem.setOnClickListener {
            numeroContagem = 3
            selecionarDiretorio()
        }

        binding.btnGerarEnderecoSemContagem.setOnClickListener {
            gerarEnderecosNaoColetados()
        }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeEscolhaRelatorio.tbPrinciapl
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Escolha de relatório"
            subtitle = "Selecione o relatório que deseja gerar"
            // setDisplayHomeAsUpEnabled(true)
        }
    }

    /* fun abrirPDF(uri: Uri) {
         val intent = Intent(Intent.ACTION_VIEW).apply {
             setDataAndType(uri, "application/pdf")
             flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
         }
         // Verifica se existe um aplicativo capaz de abrir PDF
         val packageManager = this.packageManager
         if (intent.resolveActivity(packageManager) != null) {
             startActivity(intent)
         } else {
             exibirMensagem("Não há aplicativo disponível para abrir PDF")
         }
     }

     */

    private fun inicializarRelatorios() {

        createDocumentLauncherPdfGeral =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    data?.data?.let { uri ->

                        val divergenciaDAO = DivergenciaDAO.getInstance(this)
                        val listaDivergenciasPdf = divergenciaDAO.gerarPDF()

                        if (listaDivergenciasPdf.isNotEmpty()) {
                            val pdfUtil = GerarRelatorios.getInstance(this)
                            if (pdfUtil.gerarPDF(listaDivergenciasPdf, uri)) {
                                exibirMensagem("Relatório gerado com sucesso.")
                            } else {
                                exibirMensagem("Erro ao gerar relatório.")
                            }
                        } else {
                            exibirMensagem("Não existem itens para gerar o relatório")
                        }
                    }
                }
            }

        selecionarDiretorioLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    data?.data?.let { uri ->

                        val divergenciaDAO = DivergenciaDAO.getInstance(this)
                        val listaDivergenciasPdf = divergenciaDAO.gerarPDFPorContagem(1)

                        if (listaDivergenciasPdf.isNotEmpty()) {
                            val pdfUtil1 = GerarRelatorios.getInstance(this)
                            if (pdfUtil1.gerarPDFPorContagem(listaDivergenciasPdf, numeroContagem, uri)) {
                                exibirMensagem("Relatório gerado com sucesso.")
                            } else {
                                exibirMensagem("Erro ao gerar relatório.")
                            }
                        } else {
                            exibirMensagem("Não existem itens para gerar o relatório")
                        }
                    }
                }
            }

        createDocumentLauncherPdfEnderecosNaoContados =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    data?.data?.let { uri ->

                        val divergenciaDAO = DivergenciaDAO.getInstance(this)
                        val listaEnderecosNaoColetados =
                            divergenciaDAO.gerarPDFEnderecosNaoContados()

                        if (listaEnderecosNaoColetados.first.isNotEmpty()) {
                            val pdfUtil4 = GerarRelatorios.getInstance(this)
                            if (pdfUtil4.gerarPdfEnderecosNaoContados(
                                    listaEnderecosNaoColetados,
                                    uri
                                )
                            ) {
                                exibirMensagem("Relaório gerado com sucesso.")
                            } else {
                                exibirMensagem("Não há endereços para gerar o relatório.")
                            }
                        } else {
                            exibirMensagem("Não existem enderecos a serem mostrados.")
                        }

                    }
                }
            }
    }

}