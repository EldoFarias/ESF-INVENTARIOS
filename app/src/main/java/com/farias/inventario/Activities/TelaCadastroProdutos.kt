package com.farias.inventario.Activities

import ProdutoDAO
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.farias.inventario.BancoDados.ConfiguracaoDAO
import com.farias.inventario.Modelos.Produto
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaCadastroProdutosBinding
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Suppress("INFERRED_TYPE_VARIABLE_INTO_POSSIBLE_EMPTY_INTERSECTION")
class TelaCadastroProdutos : AppCompatActivity() {

    private var dadosRecebidosProduto: Produto? = null
    private var tipo: Int = 0

    private val binding by lazy {
        ActivityTelaCadastroProdutosBinding.inflate(layoutInflater)
    }

    private var codAuxiliarObrigatorio: Boolean = false
    private var descricaoObrigatorio: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        verificarConfiguracao()
        receberDadosProduto()
        inicializarEventosClick()
        verificarTipo()
    }

    private fun verificarTipo() {
        if (tipo == 0) {
            val tituloToolbar = "Cadastro de produtos"
            val subtitulo = "Faça o cadastro dos produtos"
            inicializarToolbar(tituloToolbar, subtitulo)
            binding.btnSalvarProduto.isEnabled = true
            binding.btnEditarProduto.isEnabled = false

        } else {
            val tituloToolbar = "Tela edição produtos"
            val subtitulo = "Faça as alterações no produto"
            inicializarToolbar(tituloToolbar, subtitulo)
            binding.btnSalvarProduto.isEnabled = false
            binding.btnEditarProduto.isEnabled = true
        }

    }

    private fun receberDadosProduto() {
        val extras = intent.extras
        if (extras != null) {
            dadosRecebidosProduto = extras.getSerializable("produto") as? Produto

            tipo = 1 // Significa que abri a tela para edição

            val id_produto = dadosRecebidosProduto?.id_produto
            val codigo = dadosRecebidosProduto?.codigo_produto.toString()
            val codigoAuxiliar = dadosRecebidosProduto?.codigo_auxiliar_produto.toString()
            val descricao = dadosRecebidosProduto?.descricao_produto.toString()

            binding.edtCodigoProduto.setText(codigo)
            binding.edtCodigoAuxiliarProduto.setText(codigoAuxiliar)
            binding.edtDescricaoProduto.setText(descricao)

            binding.btnEditarProduto.setOnClickListener {
                if (id_produto != null) {
                    atualizarProduto(id_produto)
                }
            }
        } else {
            tipo = 0
        }
    }

    private fun atualizarProduto(idProduto: Int) {
        val produtoDAO = ProdutoDAO(this)

        val codigo = binding.edtCodigoProduto.text.toString()
        val codigoAuxiliar = binding.edtCodigoAuxiliarProduto.text.toString()
        val descricao = binding.edtDescricaoProduto.text.toString()

        val produto = Produto(idProduto, codigo, codigoAuxiliar, descricao)
        if (produtoDAO.editar(produto)) {
            exibirMensagem("Produto alterado com sucesso")
            finish()
        } else {
            exibirMensagem("Erro ao atualizar")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun inicializarEventosClick() {
        binding.btnSalvarProduto.setOnClickListener {
            var mensagemExibida = false

            val configuracaoDAO = ConfiguracaoDAO.getInstance(this)
            val configuracoes = configuracaoDAO.verificarConfiguracao()

            configuracoes?.let {
                if (it.importarCadastro) {
                    val tamanhoCodigo = it.tamanhoCodigoProduto
                    val tamanhoCodigoAuxiliar = it.tamanhoCodigoAuxiliar

                    if (it.codigoProduto && it.codigoAuxiliar && it.descricao) {
                        val codigo = binding.edtCodigoProduto.text.toString()
                        val codigoAuxiliar = binding.edtCodigoAuxiliarProduto.text.toString()
                        val descricao = binding.edtDescricaoProduto.text.toString()

                        if (codigo.isNotEmpty() && codigoAuxiliar.isNotEmpty() && descricao.isNotEmpty()) {
                            if (codigo.length > tamanhoCodigo) {
                                if (!mensagemExibida) {
                                    mensagemExibida = true
                                    exibirMensagem("Cogigo principal maior que o configurado!")
                                }
                            } else if (codigoAuxiliar.length > tamanhoCodigoAuxiliar) {
                                if (!mensagemExibida) {
                                    mensagemExibida = true
                                    exibirMensagem("Cogigo auxiliar maior que o configurado!")
                                }
                            } else {
                                salvarProduto(codigo, codigoAuxiliar, descricao)
                            }

                        } else {
                            exibirMensagem("Código e ou código auxiliar e ou descrição vazio(s)!!")
                        }
                    } else if (it.codigoProduto && it.codigoAuxiliar) {
                        val codigo = binding.edtCodigoProduto.text.toString()
                        val codigoAuxiliar = binding.edtCodigoAuxiliarProduto.text.toString()

                        if (codigo.isNotEmpty() && codigoAuxiliar.isNotEmpty()) {

                            if (codigo.length > tamanhoCodigo) {
                                exibirMensagem("Cogigo principal maior que o configurado!")
                            } else if (codigoAuxiliar.length > tamanhoCodigoAuxiliar) {
                                exibirMensagem("Cogigo auxiliar maior que o configurado!")
                            } else {
                                salvarCodigoCodigoAuxiliar(codigo, codigoAuxiliar)
                            }
                        } else {
                            exibirMensagem("Código e ou código auxiliar estão vazio(s)!!")
                        }
                    } else if (it.codigoProduto && it.descricao) {

                        val codigo = binding.edtCodigoProduto.text.toString()
                        val descricao = binding.edtDescricaoProduto.text.toString()

                        if (codigo.isNotEmpty() && descricao.isNotEmpty()) {

                            if (codigo.length > tamanhoCodigo) {
                                exibirMensagem("Cogigo principal maior que o configurado!")
                            } else if (descricao.length > 100) {
                                exibirMensagem("Descrição maior que o permitido!")
                            } else {
                                salvarCodigoDescricao(codigo, descricao)
                            }
                        } else {
                            exibirMensagem("Código e ou código auxiliar estão vazio(s)!!")
                        }
                    } else if (it.codigoProduto) {
                        val codigo = binding.edtCodigoProduto.text.toString()

                        if (codigo.isNotEmpty()) {

                            if (codigo.length > tamanhoCodigo) {
                                exibirMensagem("Cogigo principal maior que o configurado!")
                            } else {
                                salvarApenasCodigo(codigo)
                            }
                        } else {
                            exibirMensagem("Código está vazio!!")
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun salvarProduto(codigo: String, codigoAuxiliar: String, descricao: String) {

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

        val produtoDAO = ProdutoDAO(this)
        // Verifica se os campos estão dentro do limite permitido
        if (codigo.length <= 20) {
            if (codigoAuxiliar.length <= 20) {
                if (descricao.length <= 100) {
                    val produto = Produto(-1, codigo, codigoAuxiliar, descricao, dataAtual)
                    if (produtoDAO.salvarCompleto(produto)) {
                        exibirMensagem("Salvo com sucesso")
                        limparCampos()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "O comprimento da descrição do produto deve ser menor ou igual a 100 caracteres",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
            } else {
                Toast.makeText(
                    this,
                    "O comprimento do código auxiliar do produto deve ser menor ou igual a 20 caracteres",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        } else {

            Toast.makeText(
                this,
                "O comprimento do código do produto deve ser menor ou igual a 20 caracteres",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
    }

    private fun salvarApenasCodigo(codigo: String) {
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

        val produtoDAO = ProdutoDAO(this)
        val produto = Produto(-1, codigo, "", "", dataAtual)

        if (produtoDAO.salvarCodigo(produto)) {
            exibirMensagem("Salvo com sucesso")
            limparCampos()
        }
    }

    private fun salvarCodigoCodigoAuxiliar(codigo: String, codigoAuxiliar: String) {
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

        val produtoDAO = ProdutoDAO(this)
        val produto = Produto(-1, codigo, codigoAuxiliar, "", dataAtual)

        if (produtoDAO.salvarCodigoCodigoAuxiliar(produto)) {
            exibirMensagem("Salvo com sucesso")
            limparCampos()
        }

    }

    private fun salvarCodigoDescricao(codigo: String, descricao: String) {
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

        val produtoDAO = ProdutoDAO(this)
        val produto = Produto(-1, codigo, "", descricao, dataAtual)

        if (produtoDAO.salvarCodigoDescricao(produto)) {
            exibirMensagem("Salvo com sucesso")
            limparCampos()
        }

    }

    private fun limparCampos() {
        binding.edtCodigoProduto.setText("")
        binding.edtCodigoAuxiliarProduto.setText("")
        binding.edtDescricaoProduto.setText("")
        binding.edtCodigoProduto.requestFocus()
    }

    private fun inicializarToolbar(titulo: String, subtitulo: String) {
        val toolbar = binding.includeToolbarCadastroProdutos.tbPrinciapl
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = titulo
            subtitle = subtitulo
            // setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun verificarConfiguracao() {

        val configuracaoDAO = ConfiguracaoDAO.getInstance(this)
        val configuracoes = configuracaoDAO.verificarConfiguracao()

        configuracoes?.let {
            if (it.importarCadastro) {

                binding.txtCodigoAuxiliarProdutoLabel.visibility =
                    if (it.codigoAuxiliar) View.VISIBLE else View.GONE

                codAuxiliarObrigatorio = it.codigoAuxiliar
                descricaoObrigatorio = it.descricao

                binding.edtCodigoAuxiliarProduto.visibility =
                    if (it.codigoAuxiliar) View.VISIBLE else View.GONE

                binding.txtDescricaoProdutoLabel.visibility =
                    if (it.descricao) View.VISIBLE else View.GONE
                binding.edtDescricaoProduto.visibility =
                    if (it.descricao) View.VISIBLE else View.GONE
            }
        }
    }
}