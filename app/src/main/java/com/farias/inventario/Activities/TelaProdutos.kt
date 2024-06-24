package com.farias.inventario.Activities

import ProdutoDAO
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.farias.inventario.Adapters.ProdutoAdapter
import com.farias.inventario.BancoDados.ConfiguracaoDAO
import com.farias.inventario.Modelos.Configuracoes
import com.farias.inventario.Modelos.ErroImportacaoProduto
import com.farias.inventario.Modelos.Produto
import com.farias.inventario.R
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaProdutosBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class TelaProdutos : AppCompatActivity() {

    private lateinit var produtoAdapter: ProdutoAdapter
    private var listaProdutos = emptyList<Produto>()
    private lateinit var context: Context // Correção aqui
    private var qtdProdutos: Int = 0

    private var tipoImportacao: Int = 0


    private val binding by lazy {
        ActivityTelaProdutosBinding.inflate(layoutInflater)
    }

    // Definindo o contrato para a seleção de arquivos
    @RequiresApi(Build.VERSION_CODES.O)
    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                binding.progressBar.visibility = View.VISIBLE
                uri?.let { importarDados(it) }
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        criarMenu()

        context = applicationContext // Correção aqui

        produtoAdapter = ProdutoAdapter(

            { produto, tipo -> passarTelaEditarProduto(produto, tipo) },
            { id -> excluirProduto(id) }

        )

        binding.rvProdutos.layoutManager = LinearLayoutManager(this)
        binding.rvProdutos.adapter = produtoAdapter


        contarProdutos()
        inicializarEventosClick()

    }

    @SuppressLint("NewApi")
    fun importarDados(uri: Uri) {
        var mensagemExibida = false
        val dialog = AlertDialog.Builder(this)
            .setTitle("Importação em andamento")
            .setMessage("Por favor, aguarde até que todos os dados sejam importados.")
            .setCancelable(false)
            .create()

        dialog.show()


        val configuracaoDAO = ConfiguracaoDAO.getInstance(this)
        val configuracoes = configuracaoDAO.verificarConfiguracao()
        val delimitadorConfigurado = configuracoes?.delimitador.toString()
        val tipoImportacao = calcularTipoImportacao(configuracoes)

        var numerosLinhasErros = 0
        var numerosLinhasCorredo = 0
        var totalLinhas = 0
        val UPDATE_INTERVAL = 50 // Atualizar a ProgressBar a cada 50 linhas processadas
        val BLOCK_SIZE = 100 // Processar 100 linhas por bloco
        var lineCount = 0 // Contador de linhas processadas

        GlobalScope.launch {

            try {
                val dataAtual = getCurrentDateTime()

                val assetFileDescriptor: AssetFileDescriptor? =
                    contentResolver.openAssetFileDescriptor(uri, "r")
                val inputStream = assetFileDescriptor?.createInputStream()
                val reader = BufferedReader(InputStreamReader(inputStream))

                while (reader.readLine() != null) {
                    totalLinhas++
                }

                assetFileDescriptor?.close()
                inputStream?.close()
                reader.close()

                val newassetFileDescriptor: AssetFileDescriptor? =
                    contentResolver.openAssetFileDescriptor(uri, "r")
                val newinputStream = newassetFileDescriptor?.createInputStream()
                val newreader = BufferedReader(InputStreamReader(newinputStream))

                var line: String?
                var numeroLinha = 0

                val produtoDAO = ProdutoDAO(context)

                // Processamento em lotes
                while (newreader.readLine().also { line = it } != null) {
                    numeroLinha++
                    lineCount++

                    val campos = line?.split(delimitadorConfigurado)

                    if (campos != null && campos.isNotEmpty()) {
                        val codigo = campos[0].trim()
                        val codigoAuxiliar = if (campos.size > 1) campos[1].trim() else ""
                        val descricao = if (campos.size > 2) campos[2].trim() else ""

                        if (codigo.length > configuracoes!!.tamanhoCodigoProduto || codigoAuxiliar.length > configuracoes.tamanhoCodigoAuxiliar) {
                            if (!mensagemExibida) {
                                mensagemExibida = true
                                exibirMensagem("Códigos maiores que o configurado!")
                            }
                        } else {
                            if (!verificarDuplicadosImportacao(codigo)) {
                                numerosLinhasCorredo++

                                val produto = Produto(
                                    -1,
                                    codigo,
                                    codigoAuxiliar,
                                    descricao,
                                    dataAtual
                                )

                                salvarProduto(produtoDAO, produto, tipoImportacao)
                            } else {
                                numerosLinhasErros++
                                salvarListaProdutosDuplicados(numeroLinha, codigo, dataAtual)
                            }
                        }

                        if (lineCount >= UPDATE_INTERVAL || numeroLinha == totalLinhas) {
                            val progresso = (numeroLinha.toFloat() / totalLinhas.toFloat() * 100).toInt()
                            atualizarProgressBar(progresso)
                            lineCount = 0 // Resetar o contador

                            // Dar uma pausa curta após processar um bloco
                            delay(100) // Por exemplo, uma pausa de 100 milissegundos
                        }
                    }
                }

                newinputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            launch(Dispatchers.Main) {
                atualizarListaProdutos()
                binding.progressBar.visibility = View.GONE
                mostrarToast(numerosLinhasCorredo, numerosLinhasErros)
                dialog.dismiss()
            }
        }
    }


    private fun calcularTipoImportacao(configuracoes: Configuracoes?): Int {
        return when {
            configuracoes == null -> 0
            configuracoes.importarCadastro == true -> {
                if (configuracoes.codigoProduto && configuracoes.codigoAuxiliar && configuracoes.descricao) {
                    3
                } else if (configuracoes.codigoProduto && configuracoes.codigoAuxiliar) {
                    2
                } else if (configuracoes.codigoProduto && configuracoes.descricao) {
                    4
                } else if (configuracoes.codigoProduto) {
                    1
                } else {
                    0
                }
            }
            else -> 0
        }
    }

    private fun salvarProduto(produtoDAO: ProdutoDAO, produto: Produto, tipoImportacao: Int) {
        try {
            when (tipoImportacao) {
                3 -> produtoDAO.salvarCompleto(produto)
                4 -> produtoDAO.salvarCodigoDescricao(produto)
                2 -> produtoDAO.salvarCodigoCodigoAuxiliar(produto)
                else -> exibirMensagem("Configuração diferente do arquivo.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCurrentDateTime(): String {
        return if (Build.VERSION.SDK_INT >= 26) {
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            currentDateTime.format(formatter)
        } else {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            dateFormat.format(Date())
        }
    }


    private fun salvarListaProdutosDuplicados(
        numeroLinha: Int,
        codigo: String,
        dataFormatada: String
    ) {

        val produtoDAO = ProdutoDAO(this)
        val produtoDuplicado = ErroImportacaoProduto(
            -1, numeroLinha,
            codigo, dataFormatada
        )

        if (produtoDAO.salvarProdutosDuplicados(produtoDuplicado)) {

        } else {

        }
    }

    private fun verificarDuplicadosImportacao(codigo: String): Boolean {
        // Exemplo fictício: return dbHelper.verificarCodigoExistente(codigo)
        val produtoDAO = ProdutoDAO.getInstance(this)
        return produtoDAO.verificarDuplicadosImportacao(codigo)

    }

    private fun atualizarProgressBar(progresso: Int) {
        runOnUiThread {
            binding.progressBar.progress = progresso
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun escolherArquivo() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*" // Pode ser qualquer tipo de arquivo, ajuste conforme necessário
        }
        getContent.launch(intent)
    }


    private fun mostrarToast(numeroLinhasCorreto: Int, numeroLinhasErros: Int) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle("STATUS IMPORTAÇÃO PRODUTOS")
        alertBuilder.setMessage(
            "Foram importadas ${numeroLinhasCorreto} linhas com sucesso!\n \n" +
                    "Existem ${numeroLinhasErros} linhas com códigos duplicados, verifique arquivo."
        )
        alertBuilder.create().show()
        contarProdutos()
    }

    private fun excluirProduto(id: Int) {
        val alertBuilder = AlertDialog.Builder(this)

        alertBuilder.setTitle("Exclusão de Produto")
        alertBuilder.setMessage("Deseja realmente excluir este produto, esta ação não poderá ser desfeita")
        alertBuilder.setPositiveButton("SIM") { _, _ ->
            val produtoDAO = ProdutoDAO(this)
            if (produtoDAO.deletar(id)) {
                exibirMensagem("Produto removido com sucesso")
                atualizarListaProdutos()
            } else {

            }
        }
        alertBuilder.setNegativeButton("NÃO") { _, _ -> }
        alertBuilder.create().show()
    }

    override fun onStart() {
        super.onStart()
        atualizarListaProdutos()
    }

    private fun atualizarListaProdutos() {
        val produtoDAO = ProdutoDAO(this)
        listaProdutos = produtoDAO.listar()
        produtoAdapter.atualizarLista(listaProdutos)

    }

    private fun passarTelaEditarProduto(produto: Produto, tipo: Int) {

        val intent = Intent(this, TelaCadastroProdutos::class.java)
        val bundle = Bundle()
        bundle.putSerializable("produto", produto)
        intent.putExtras(bundle)
        intent.putExtra("tipo", tipo)

        startActivity(intent)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun inicializarEventosClick() {
        binding.imgImportar.setOnClickListener {
            escolherArquivo()
        }

        binding.imgCadastrar.setOnClickListener {
            val intent = Intent(this, TelaCadastroProdutos::class.java)
            startActivity(intent)
        }

        binding.imgExluirTudo.setOnClickListener {
            confirmarExclusaoTodosProdutos()
        }
        binding.imgErrosImportacao.setOnClickListener {
            val intent = Intent(this, TelaErrosImportacaoProdutos::class.java)
            startActivity(intent)
        }
    }

    private fun confirmarExclusaoTodosProdutos() {
        val alertBuilder = AlertDialog.Builder(this)

        alertBuilder.setTitle("Exclusão da base de Produto")
        alertBuilder.setMessage("Deseja realmente excluir todos os produtos, esta ação não poderá ser desfeita")
        alertBuilder.setPositiveButton("SIM") { _, _ ->
            val produtoDAO = ProdutoDAO(this)
            if (produtoDAO.deletarTudo(1)) {
                exibirMensagem("Todos os produtos foram removidos")
                atualizarListaProdutos()
            } else {

            }
        }
        alertBuilder.setNegativeButton("NÃO") { _, _ -> }
        alertBuilder.create().show()
    }

    private fun contarProdutos() {
        val produtoDAO = ProdutoDAO(this)
        qtdProdutos = produtoDAO.contarItens()
        inicializarToolbar(qtdProdutos)

    }

    private fun inicializarToolbar(qtdProdutos: Int) {

        val toolbar = binding.includeToolbarProdutos.tbPrinciapl
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Tela de produtos"
            subtitle = "Existem ${qtdProdutos} produtos cadastrados"
            // setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onRestart() {
        super.onRestart()
        contarProdutos()
    }

    private fun criarMenu() {
        addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_tela_produto, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {

                        R.id.menu_ajuda -> {
                            val intent =
                                Intent(applicationContext, TelaLayoutImportacao::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                        }
                    }
                    return true
                }
            }
        )
    }
}