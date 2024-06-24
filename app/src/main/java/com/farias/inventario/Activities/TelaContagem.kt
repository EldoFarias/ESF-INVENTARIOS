package com.farias.inventario.Activities

import ProdutoDAO
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.annotation.RequiresApi
import com.farias.inventario.BancoDados.ConfiguracaoDAO
import com.farias.inventario.BancoDados.ContagemDAO
import com.farias.inventario.BancoDados.EnderecoDAO
import com.farias.inventario.Modelos.Cabecalho
import com.farias.inventario.Modelos.Itens
import com.farias.inventario.Utilidades.DadosUsuarioLogado
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaContagemBinding
import com.google.zxing.integration.android.IntentIntegrator
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class TelaContagem : AppCompatActivity() {

    private lateinit var campoQuantidade: EditText
    private var autoQtd: Boolean = false
    private val binding by lazy {
        ActivityTelaContagemBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        campoQuantidade = binding.editQuantidade

        inicializarToolbar()
        iniciarEventosClick()
        receberEndereco()
        desabilitarCampos()
        desabilitarScaner()

    }

    private fun desabilitarScaner() {
        campoQuantidade.setOnFocusChangeListener { view, hasFocus ->
            binding.btnScaner.isEnabled = !hasFocus
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun iniciarEventosClick() {

        binding.editCodigo.setOnFocusChangeListener { _, hasFocus ->

            if (!hasFocus) {
                if (autoQtd) {
                    val codigo = binding.editCodigo.text.toString()
                    var descricao = ""
                    var quantidade = binding.editQuantidade.text.toString()

                    if (codigo.isNotEmpty() && codigo != null) {
                        descricao = checarCodigoBaseDados(codigo)
                        binding.editDescricao.setText(descricao)
                        if (descricao.isNotEmpty() && descricao != "NÃO CADASTRADO") {
                            if (quantidade.isNotEmpty()) {
                                salvarItem(codigo, descricao, quantidade.toInt())
                            }
                        }
                    }

                } else {

                    val codigo = binding.editCodigo.text.toString()
                    var descricao = ""

                    if (codigo.isNotEmpty() && codigo != null) {
                        descricao = checarCodigoBaseDados(codigo)
                        binding.editDescricao.setText(descricao)
                        binding.btnScaner.isEnabled = false
                    }
                }
            }
        }

        binding.btnScaner.setOnClickListener {
            iniciarScanner()
        }

        binding.btnLimpar.setOnClickListener {
            limparCampos()
        }
        binding.btnConfirmarContagem.setOnClickListener {
            val contagem = binding.editNumeroContagem.text.toString()

            if (contagem.trim() == "1" || contagem.trim() == "2" || contagem.trim() == "3") {
                if (contagem.trim().isNotEmpty()) {
                    habilitarCampos()
                    salvarCabecalhoContagem()
                } else {
                    exibirMensagem("Informe o numero da contagem")
                }
            } else {
                exibirMensagem("Contagem deve ser 1, 2 ou 3.")
            }
        }
        binding.btnConfirmarQuantidade.setOnClickListener {

            var comCadastro = false
            var tamanhoCodigo = 0
            val configuracaoDAO = ConfiguracaoDAO.getInstance(this)
            val configuracoes = configuracaoDAO.verificarConfiguracao()

            if (configuracoes != null) {
                comCadastro = configuracoes.importarCadastro
                tamanhoCodigo = configuracoes.tamanhoCodigoProduto
            }

            val codigo = binding.editCodigo.text.toString()
            var descricao = ""
            val quantidade = binding.editQuantidade.text.toString()

            if (comCadastro) {
                if (codigo.isNotEmpty()) {
                    if (codigo.length > tamanhoCodigo) {
                        exibirMensagem("Código maior que o configurado")
                    } else {
                        descricao = checarCodigoBaseDados(codigo)
                        binding.editDescricao.setText(descricao)
                        if (descricao.isNotEmpty() && descricao != "NÃO CADASTRADO") {
                            if (quantidade.isNotEmpty()) {
                                salvarItem(codigo.trim(), descricao.trim(), quantidade.toInt())
                            } else {
                                exibirMensagem("Informe uma quantidade")
                            }
                        } else {
                            exibirMensagem("Esse produto não está cadastrado")
                        }
                    }
                }
            } else {
                if (quantidade.isNotEmpty()) {
                    descricao = checarCodigoBaseDados(codigo)
                    salvarItem(codigo.trim(), descricao.trim(), quantidade.toInt())
                } else {
                    exibirMensagem("Informe uma quantidade")
                }
            }
        }

        binding.btnItensLidos.setOnClickListener {
            val endereco = binding.editEndereco.text.toString()
            val numeroContagem = binding.editNumeroContagem.text.toString()

            val intent = Intent(this, TelaItensLidos::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            intent.putExtra("tipo", 0) // Tipo detalhado = 0
            intent.putExtra("endereco", endereco)
            intent.putExtra("numeroContagem", numeroContagem)
            startActivity(intent)
        }

        binding.btnTotais.setOnClickListener {
            val endereco = binding.editEndereco.text.toString()
            val numeroContagem = binding.editNumeroContagem.text.toString()

            val intent = Intent(this, TelaItensLidos::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            intent.putExtra("tipo", 1) // Tipo resumido = 1
            intent.putExtra("endereco", endereco)
            intent.putExtra("numeroContagem", numeroContagem)
            startActivity(intent)
        }

        binding.btnFecharEndereco.setOnClickListener {

            val endereco = binding.editEndereco.text.toString()
            val numeroContagem = binding.editNumeroContagem.text.toString()
            val enderecoDAO = EnderecoDAO.getInstance(this)

            if (enderecoDAO.fecharEndereco(endereco, numeroContagem)) {
                exibirMensagem("Endereço: ${endereco} fechado.")
                atualizarTelaEndereco(endereco)

            }
        }

        binding.switchTipoContagem.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {
                //Habilitado
                autoQtd = true
                if (!binding.editQuantidade.text.isNotEmpty()) {
                    binding.switchTipoContagem.isChecked = false
                    binding.editQuantidade.setBackgroundColor(Color.rgb(255, 148, 148))

                } else {
                    binding.editQuantidade.setBackgroundColor(Color.YELLOW)
                    binding.editQuantidade.isEnabled = false
                }

            } else {
                //Desabilitado
                autoQtd = false
                if (!binding.editQuantidade.text.isNotEmpty()) {
                    binding.editQuantidade.setBackgroundColor(Color.WHITE)
                    binding.editQuantidade.isEnabled = true
                    binding.editCodigo.requestFocus()
                } else {
                    binding.editQuantidade.setBackgroundColor(Color.WHITE)
                    binding.editQuantidade.isEnabled = true
                }
            }
        }
    }

    private fun atualizarTelaEndereco(endereco: String) {
        val enderecoDAO = EnderecoDAO.getInstance(this)
        if (enderecoDAO.atualizarEnderecoFechado(endereco)) {
            val intent = Intent(this, TelaListagemEnderecos::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    private fun checarCodigoBaseDados(codigo: String): String {

        val produtoDAO = ProdutoDAO(this)
        return produtoDAO.verificarProduto(codigo)  // Aqui eu tenho um retorno "LEITE"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun salvarCabecalhoContagem() {

        var dataAtual: String

        if (Build.VERSION.SDK_INT >= 26) {
            // Usar LocalDateTime para versões mais recentes do Android
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            dataAtual = currentDateTime.format(formatter)
        } else {
            // Usar Date para versões mais antigas do Android
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = Date()
            dataAtual = dateFormat.format(date)
        }

        val dadosUsuarioLogado = DadosUsuarioLogado.getInstance()
        val codigoUsuario = dadosUsuarioLogado.codigoUsuario

        val contagemDAO = ContagemDAO.getInstance(this) //Instancia DAO

        val enderecoContagem = binding.editEndereco.text.toString()
        val numeroContagem = binding.editNumeroContagem.text.toString()
        val codigoOperador = "000" + codigoUsuario
        val codigoAcesso = codigoUsuario
        val hora_abertura_contagem = dataAtual
        val hora_fechamento_contagem = dataAtual
        val status_contagem = "Aberto"
        val identificador = "*"

        if (numeroContagem.isNotEmpty() && enderecoContagem.isNotEmpty()) {
            if (!verificarCabecalhoExiste(numeroContagem, enderecoContagem)) {
                val cabecalho = Cabecalho(
                    -1,
                    1,
                    enderecoContagem,
                    numeroContagem,
                    codigoOperador,
                    codigoAcesso,
                    hora_abertura_contagem,
                    hora_fechamento_contagem,
                    status_contagem,
                    identificador
                )
                contagemDAO.inserirCabecalho(cabecalho)
            }
        }
    }

    private fun verificarCabecalhoExiste(numeroContagem: String, endereco: String): Boolean {

        val contagemDAO = ContagemDAO.getInstance(this) //Instancia DAO
        return contagemDAO.verificarCabecalhoExiste(numeroContagem, endereco)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun salvarItem(codigo: String, descricao: String, quantidade: Int) {

        var dataAtual: String

        if (Build.VERSION.SDK_INT >= 26) {
            // Usar LocalDateTime para versões mais recentes do Android
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            dataAtual = currentDateTime.format(formatter)
        } else {
            // Usar Date para versões mais antigas do Android
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = Date()
            dataAtual = dateFormat.format(date)
        }

        val dadosUsuarioLogado = DadosUsuarioLogado.getInstance()
        val codigoUsuario = dadosUsuarioLogado.codigoUsuario
        val codigoOperador = codigoUsuario + codigoUsuario

        val enderecoAtual = binding.editEndereco.text.toString()
        val numeroContagem = binding.editNumeroContagem.text.toString()

        val contagemDAO = ContagemDAO.getInstance(this) //Instancia DAO
        val item = Itens(
            -1, enderecoAtual, numeroContagem, codigo, descricao,
            quantidade, dataAtual, dataAtual, codigoOperador, -1
        )

        if (contagemDAO.inserirItens(item)) {
            limparCampos()
        } else {

        }
    }

    private fun habilitarCampos() {

        binding.btnConfirmarContagem.isEnabled = false // Exceto esse aqui
        binding.editNumeroContagem.isEnabled = false // Exceto esse aqui
        binding.btnLimpar.isEnabled = true
        binding.btnScaner.isEnabled = true
        binding.editCodigo.isEnabled = true
        binding.editQuantidade.isEnabled = true
        binding.btnConfirmarQuantidade.isEnabled = true
        binding.editCodigo.requestFocus()
    }

    private fun desabilitarCampos() {
        binding.btnConfirmarContagem.isEnabled = true // Exceto esse aqui
        binding.editNumeroContagem.isEnabled = true // Exceto esse aqui
        binding.btnLimpar.isEnabled = false
        binding.btnScaner.isEnabled = false
        binding.editCodigo.isEnabled = false
        binding.btnConfirmarQuantidade.isEnabled = false
        binding.editQuantidade.isEnabled = false

    }

    private fun limparCampos() {
        binding.editCodigo.setText("")
        binding.editDescricao.setText("")

        if (!autoQtd) {
            binding.editQuantidade.setText("")
            binding.editCodigo.postDelayed({
                binding.editCodigo.requestFocus()
            }, 200)

        } else {
            binding.editCodigo.postDelayed({
                binding.editCodigo.requestFocus()
            }, 200)
        }
    }

    private fun receberEndereco() {
        val extras = intent.extras
        if (extras != null) {
            val endereco = extras.getInt("codigo")
            if (endereco != null) {
                val enderecoFormatado = String.format("%04d", endereco)
                binding.editEndereco.setText(enderecoFormatado)
                binding.editNumeroContagem.requestFocus()
            }
        }
    }

    private fun iniciarScanner() {

        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Aponte a câmera para o código de barras")
        integrator.setCameraId(0)  // Use a câmera traseira
        integrator.setOrientationLocked(true)
        integrator.setBeepEnabled(true)  // Desabilita e Habilita o som ao escanear
        integrator.setBarcodeImageEnabled(false)  // Não salva imagens do código escaneado

        integrator.initiateScan()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val codigo = result.contents
                binding.editCodigo.setText("${codigo}")
                abrirTeclado()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
            abrirTeclado()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun abrirTeclado() {
        if (!autoQtd) {
            binding.editQuantidade.requestFocus()
        } else {

            var comCadastro = false
            var tamanhoCodigo = 0
            val configuracaoDAO = ConfiguracaoDAO.getInstance(this)
            val configuracoes = configuracaoDAO.verificarConfiguracao()

            if (configuracoes != null) {
                comCadastro = configuracoes.importarCadastro
                tamanhoCodigo = configuracoes.tamanhoCodigoProduto
            }

            val codigo = binding.editCodigo.text.toString()
            var descricao = ""
            val quantidade = binding.editQuantidade.text.toString()

            if (comCadastro) {
                if (codigo.isNotEmpty()) {
                    if (codigo.length > tamanhoCodigo) {
                        exibirMensagem("Código maior que o configurado")
                    } else {
                        descricao = checarCodigoBaseDados(codigo)
                        binding.editDescricao.setText(descricao)
                        if (descricao.isNotEmpty() && descricao != "NÃO CADASTRADO") {
                            if (quantidade.isNotEmpty()) {
                                salvarItem(codigo.trim(), descricao.trim(), quantidade.toInt())
                            } else {
                                exibirMensagem("Informe uma quantidade")
                            }
                        } else {
                            exibirMensagem("Esse produto não está cadastrado")
                        }
                    }
                }
            } else {
                if (quantidade.isNotEmpty()) {
                    descricao = checarCodigoBaseDados(codigo)
                    salvarItem(codigo.trim(), descricao.trim(), quantidade.toInt())
                } else {
                    exibirMensagem("Informe uma quantidade")
                }
            }
        }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.include.tbPrinciapl
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Contagem de endereço"
            subtitle = "Aberto"
        }
    }
}