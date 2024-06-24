package com.farias.inventario.Activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.farias.inventario.BancoDados.ConfiguracaoDAO
import com.farias.inventario.BancoDados.DatabaseHelper
import com.farias.inventario.BancoDados.ExportarTodasContagemDAO
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaEscolhaLayoutExportacaoBinding

class TelaEscolhaLayoutExportacao : AppCompatActivity() {

    private lateinit var selecionarDiretorioExportacaoLauncher: ActivityResultLauncher<Intent>
    private var diretorioUriCodigoDescricaoQuantidade: Uri? = null
    private lateinit var selecionarDiretorioLauncherInvMC: ActivityResultLauncher<Intent>
    private val binding by lazy { ActivityTelaEscolhaLayoutExportacaoBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        selecionarDiretorioExportacaoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    data?.data?.let { uri ->

                        val exportarCodigoQuantidadeDAO = ExportarTodasContagemDAO.getInstance(this)
                        try {
                            if (exportarCodigoQuantidadeDAO.exportarCodigosQuantidades(
                                    "SELECT" +
                                            " codigo_produto," +
                                            " descricao_produto," +
                                            " CAST(SUM(total_contagem) AS INTEGER) AS total_quantidade" +
                                            " FROM (" +
                                            " SELECT" +
                                            " codigo_produto," +
                                            " descricao_produto," +
                                            " CASE" +
                                            " WHEN total_contagem_1 = total_contagem_2 AND total_contagem_1 = total_contagem_3 THEN total_contagem_1" +
                                            " WHEN total_contagem_1 = total_contagem_2 THEN total_contagem_1" +
                                            " WHEN total_contagem_1 = total_contagem_3 THEN total_contagem_1" +
                                            " ELSE" +
                                            " CASE" +
                                            " WHEN total_contagem_2 = total_contagem_3 THEN total_contagem_2" +
                                            " ELSE total_contagem_3" +
                                            " END" +
                                            " END AS total_contagem" +
                                            " FROM (" +
                                            " SELECT" +
                                            " codigo_produto," +
                                            " descricao_produto," +
                                            " CASE" +
                                            " WHEN total_contagem_1 = total_contagem_2 AND total_contagem_1 = total_contagem_3 THEN total_contagem_1" +
                                            " WHEN total_contagem_1 = total_contagem_2 THEN total_contagem_1" +
                                            " WHEN total_contagem_1 = total_contagem_3 THEN total_contagem_1" +
                                            " ELSE 0" +
                                            " END AS total_contagem_1," +
                                            " CASE" +
                                            " WHEN total_contagem_1 = total_contagem_2 AND total_contagem_1 = total_contagem_3 THEN total_contagem_2 " +
                                            " WHEN total_contagem_1 = total_contagem_2 THEN total_contagem_2" +
                                            " WHEN total_contagem_2 = total_contagem_3 THEN total_contagem_2" +
                                            " ELSE 0" +
                                            " END AS total_contagem_2," +
                                            " CASE" +
                                            " WHEN total_contagem_1 = total_contagem_2 AND total_contagem_1 = total_contagem_3 THEN total_contagem_3" +
                                            " WHEN total_contagem_1 = total_contagem_3 THEN total_contagem_3" +
                                            " WHEN total_contagem_2 = total_contagem_3 THEN total_contagem_3" +
                                            " ELSE 0" +
                                            " END AS total_contagem_3" +
                                            " FROM (" +
                                            " SELECT" +
                                            " codigo_produto," +
                                            " descricao_produto," +
                                            " endereco_contagem," +
                                            " SUM(CASE WHEN numero_contagem = 1 THEN quantidade ELSE 0 END) AS total_contagem_1," +
                                            " SUM(CASE WHEN numero_contagem = 2 THEN quantidade ELSE 0 END) AS total_contagem_2," +
                                            " SUM(CASE WHEN numero_contagem = 3 THEN quantidade ELSE 0 END) AS total_contagem_3" +
                                            " FROM itens_contagens" +
                                            " GROUP BY codigo_produto, descricao_produto, endereco_contagem" +
                                            " ) AS subquery" +
                                            " ) AS TempCalculos" +
                                            " WHERE total_contagem_1 <> 0 OR total_contagem_2 <> 0 OR total_contagem_3 <> 0" +
                                            " ) AS final" +
                                            " GROUP BY codigo_produto, descricao_produto;",
                                    recuperarDelimitador(),
                                    uri, this
                                )
                            ) {
                                exibirMensagem("Sucesso ao gerar arquivo")
                            } else {
                                exibirMensagem("Erro ao gerar arquivo")
                            }
                        } catch (e: Exception) {
                            Log.i("ROLA_TENTOU_GERAR", "${e.message.toString()}: ")
                        }
                    }
                }
            }
        selecionarDiretorioLauncherInvMC = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                data?.data?.let { uri ->
                    val exportarTodasContagemDAO = ExportarTodasContagemDAO.getInstance(this)
                    try {
                        if (exportarTodasContagemDAO.exportarLayoutInvMC(
                                "SELECT " +
                                        "idControleCabecalho " +
                                        ",endereco_contagem " +
                                        ",endereco_contagem " +
                                        ",numero_contagem " +
                                        ",numero_contagem " +
                                        ",codigo_operador " +
                                        ",codigo_operador " +
                                        ",COALESCE(substr(codigo_operador, 1, 3), '') " + // Se o valor for vazio, retorne uma string vazia
                                        ",codigo_produto " +
                                        ",descricao_produto " +
                                        ",quantidade " +
                                        ",hora_abertura_contagem " +
                                        ",hora_fechamento_contagem " +
                                        "FROM ${DatabaseHelper.TABELA_ITENS_CONTAGENS}",
                                this, // Passando o contexto
                                uri // Passando o URI do diretório selecionado
                            )
                        ) {
                            exibirMensagem("Arquivos gerados com sucesso")
                        } else {
                            exibirMensagem("Erro ao gerar arquivos")
                        }
                    } catch (e: Exception) {
                        Log.i("ROLA_TENTOU_GERAR", "${e.message.toString()}: ")
                    }
                }
            }
        }

        inicializarToolbar()
        inicializarEventosClick()
    }

    private fun selecionarDiretoriolayoutInvMC() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        selecionarDiretorioLauncherInvMC.launch(intent)
    }
    private fun selecionarDiretorioExportacao() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        selecionarDiretorioExportacaoLauncher.launch(intent)
    }
    private fun recuperarDelimitador(): String {
        val configuracaoDAO = ConfiguracaoDAO.getInstance(this)
        val configuracoes = configuracaoDAO.verificarConfiguracao()
        val delimitadorConfigurado = configuracoes?.delimitador.toString()

        return delimitadorConfigurado
    }
    private fun inicializarEventosClick() {
        binding.btnInvMC.setOnClickListener {
            selecionarDiretoriolayoutInvMC()
        }
        binding.btnCodigoQuantidade.setOnClickListener {
            selecionarDiretorioExportacao()
        }
        binding.btnCodigoDescricaoQuantidade.setOnClickListener {
            selecionarDiretorioParaExportacaoCodigoDescricaoQuantidade()
        }
    }
    private fun selecionarDiretorioParaExportacaoCodigoDescricaoQuantidade() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        selecionarDiretorioExportacaoCodigoDescricaoQuantidadeLauncher.launch(intent)
    }
    private val selecionarDiretorioExportacaoCodigoDescricaoQuantidadeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                data?.data?.let { uri ->
                    diretorioUriCodigoDescricaoQuantidade = uri
                    exportarCodigoDescricaoQuantidade()
                }
            }
        }
    private fun exportarCodigoDescricaoQuantidade() {
        val exportarCodigoQuantidadeDAO = ExportarTodasContagemDAO.getInstance(this)
        val query = "SELECT" +
                " codigo_produto," +
                " descricao_produto," +
                " CAST(SUM(total_contagem) AS INTEGER) AS total_quantidade" +
                " FROM (" +
                " SELECT" +
                " codigo_produto," +
                " descricao_produto," +
                " CASE" +
                " WHEN total_contagem_1 = total_contagem_2 AND total_contagem_1 = total_contagem_3 THEN total_contagem_1" +
                " WHEN total_contagem_1 = total_contagem_2 THEN total_contagem_1" +
                " WHEN total_contagem_1 = total_contagem_3 THEN total_contagem_1" +
                " ELSE" +
                " CASE" +
                " WHEN total_contagem_2 = total_contagem_3 THEN total_contagem_2" +
                " ELSE total_contagem_3" +
                " END" +
                " END AS total_contagem" +
                " FROM (" +
                " SELECT" +
                " codigo_produto," +
                " descricao_produto," +
                " CASE" +
                " WHEN total_contagem_1 = total_contagem_2 AND total_contagem_1 = total_contagem_3 THEN total_contagem_1" +
                " WHEN total_contagem_1 = total_contagem_2 THEN total_contagem_1" +
                " WHEN total_contagem_1 = total_contagem_3 THEN total_contagem_1" +
                " ELSE 0" +
                " END AS total_contagem_1," +
                " CASE" +
                " WHEN total_contagem_1 = total_contagem_2 AND total_contagem_1 = total_contagem_3 THEN total_contagem_2 " +
                " WHEN total_contagem_1 = total_contagem_2 THEN total_contagem_2" +
                " WHEN total_contagem_2 = total_contagem_3 THEN total_contagem_2" +
                " ELSE 0" +
                " END AS total_contagem_2," +
                " CASE" +
                " WHEN total_contagem_1 = total_contagem_2 AND total_contagem_1 = total_contagem_3 THEN total_contagem_3" +
                " WHEN total_contagem_1 = total_contagem_3 THEN total_contagem_3" +
                " WHEN total_contagem_2 = total_contagem_3 THEN total_contagem_3" +
                " ELSE 0" +
                " END AS total_contagem_3" +
                " FROM (" +
                " SELECT" +
                " codigo_produto," +
                " descricao_produto," +
                " endereco_contagem," +
                " SUM(CASE WHEN numero_contagem = 1 THEN quantidade ELSE 0 END) AS total_contagem_1," +
                " SUM(CASE WHEN numero_contagem = 2 THEN quantidade ELSE 0 END) AS total_contagem_2," +
                " SUM(CASE WHEN numero_contagem = 3 THEN quantidade ELSE 0 END) AS total_contagem_3" +
                " FROM itens_contagens" +
                " GROUP BY codigo_produto, descricao_produto, endereco_contagem" +
                " ) AS subquery" +
                " ) AS TempCalculos" +
                " WHERE total_contagem_1 <> 0 OR total_contagem_2 <> 0 OR total_contagem_3 <> 0" +
                " ) AS final" +
                " GROUP BY codigo_produto, descricao_produto;"

        try {
            if (diretorioUriCodigoDescricaoQuantidade != null) {
                if (exportarCodigoQuantidadeDAO.exportarCodigoDescricaoQuantidade(
                        query,
                        recuperarDelimitador(),
                        diretorioUriCodigoDescricaoQuantidade!!,
                        this
                    )
                ) {
                    exibirMensagem("Sucesso ao gerar arquivo")
                } else {
                    exibirMensagem("Erro ao gerar arquivo")
                }
            } else {
                exibirMensagem("Erro: Diretório não selecionado")
            }
        } catch (e: Exception) {
            Log.i("ROLA_TENTOU_GERAR", "${e.message.toString()}: ")
        }
    }
    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbarEscolherLayout.tbPrinciapl
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Escolha de layout"
            subtitle = "Selecione o layout de exportação"
// setDisplayHomeAsUpEnabled(true)
        }
    }
}