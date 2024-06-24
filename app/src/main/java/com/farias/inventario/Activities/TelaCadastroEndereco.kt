package com.farias.inventario.Activities

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.farias.easycalls.adapters.EnderecoAdapter
import com.farias.inventario.BancoDados.EnderecoDAO
import com.farias.inventario.Modelos.Endereco
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaCadastroEnderecoBinding


class TelaCadastroEndereco : AppCompatActivity() {

    private val binding by lazy {
        ActivityTelaCadastroEnderecoBinding.inflate(layoutInflater)
    }

    private lateinit var enderecoAdapter: EnderecoAdapter
    private var listaEnderecos = emptyList<Endereco>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarToolbar()
        inicializarEventosClick()

        enderecoAdapter = EnderecoAdapter(
            { endereco -> confirmarExclusaoEndereco(endereco) }
            // {codEndereco -> telaContagem(codEndereco)} // Usando este método apenas para alguns testes ou seja, sua função não condiz com o que faz

        )
        binding.rvEnderecos.layoutManager = LinearLayoutManager(this)
        binding.rvEnderecos.addItemDecoration(
            DividerItemDecoration(
                this, LinearLayoutManager.VERTICAL
            )
        )
        binding.rvEnderecos.adapter = enderecoAdapter
        criarEventosClick()


    }

    private fun inicializarEventosClick() {
        binding.btnExcluirTudo.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("ATENÇÃO, EXCLUSÃO ENDEREÇOS")
            builder.setMessage(
                "Esta ação não poderá ser cancelada.\n" +
                        "Você tem certeza que deseja excluir todos os endereços e suas contagens?"
            )
            builder.setPositiveButton("Sim") { dialog, which ->
                exluirTodosEnderecos(1) // Este valor não está sendo usado
            }
            builder.setNegativeButton("Não") { dialog, which ->
                exibirMensagem("Ação cancelada.")
            }

            val dialog = builder.create()
            dialog.show()

        }
    }

    private fun exluirTodosEnderecos(valor: Int) {
        val enderecoDAO = EnderecoDAO.getInstance(this)
        if (enderecoDAO.deletarTudo(valor)) {

            exibirMensagem("Todos os Itens Foram excluidos")
            binding.editNome.requestFocus()
            atualizarListaEnderecos()
        } else {

        }
    }

    private fun confirmarExclusaoEndereco(endereco: String) {

        val alertBuilder = AlertDialog.Builder(this)

        alertBuilder.setTitle("Exclusão de endereço")
        alertBuilder.setMessage(
            "Deseja realmente excluir este endereço? \n" +
                    "Ao excluir o endereço, você irá excluir todos os itens relacionados a ele.\n" +
                    "Esta ação não poderá ser desfeita.\n"
        )
        alertBuilder.setPositiveButton("SIM") { _, _ ->
            val enderecoDAO = EnderecoDAO.getInstance(this)
            if (enderecoDAO.deletar(endereco)) {
                exibirMensagem("Endereço ${endereco} removido com sucesso.")
                binding.editNome.requestFocus()
                atualizarListaEnderecos()
            } else {

            }
        }
        alertBuilder.setNegativeButton("NÃO") { _, _ -> }
        alertBuilder.create().show()
    }

    override fun onStart() {
        super.onStart()
        atualizarListaEnderecos()

    }

    private fun atualizarListaEnderecos() {
        val enderecoDAO = EnderecoDAO.getInstance(this)
        listaEnderecos = enderecoDAO.listar()
        enderecoAdapter.adicionarEndereco(listaEnderecos)

    }

    private fun criarEventosClick() {
        binding.btnAddEndereco.setOnClickListener {
            val codigoInicialString = binding.editCodigoInicial.text.toString()
            val codigoFinalString = binding.editCodigoFinal.text.toString()
            val nomeEndereco = binding.editNome.text.toString()

            if (codigoInicialString.isNotEmpty() && codigoFinalString.isNotEmpty() && nomeEndereco.isNotEmpty()) {
                if (codigoInicialString.toInt() > codigoFinalString.toInt()) {
                    exibirMensagem("Código inicial precisa ser menor que código final")
                } else {

                    try {
                        val codigoInicial = codigoInicialString.toInt()
                        val codigoFinal = codigoFinalString.toInt()
                        val nomeLocalEndereco = binding.editNome.text.toString()
                        val status = "Aguardando contagens"
                        val data_abertura = "teste"
                        val data_fechado = "teste"

                        val enderecoDao = EnderecoDAO.getInstance(this)

                        for (codigoAtual in codigoInicial..codigoFinal) {
                            val codigoFormatado = codigoAtual.toString().padStart(4, '0')
                            val endereco = Endereco(
                                -1,
                                codigoFormatado,
                                nomeLocalEndereco,
                                status,
                                data_abertura,
                                data_fechado,
                                false,
                                false,
                                false
                            )

                            if (enderecoDao.salvar(endereco)) {

                            } else {
                                exibirMensagem("Erro ao salvar endereço para o código $codigoAtual")
                            }
                        }
                        exibirMensagem("Sucesso ao salvar")
                        limparCampos()
                    } catch (e: NumberFormatException) {
                        exibirMensagem("Digite apenas números nos campos de código")
                    }
                }
            }else{
                exibirMensagem("Preencha todos os campos")
            }
        }
    }


    private fun limparCampos() {

        atualizarListaEnderecos()
        binding.editNome.requestFocus()
        binding.editCodigoFinal.setText("")
        binding.editCodigoInicial.setText("")
        binding.editNome.setText("")
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbarEnderecos.tbPrinciapl
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Cadastro de endereços"
            subtitle = "Adicione os endereços do inventário"
            // setDisplayHomeAsUpEnabled(true)
        }
    }
}