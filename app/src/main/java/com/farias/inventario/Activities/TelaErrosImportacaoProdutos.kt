package com.farias.inventario.Activities

import ProdutoDAO
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.farias.inventario.Adapters.ProdutosErrosImportacaoAdapter
import com.farias.inventario.Modelos.ErroImportacaoProduto
import com.farias.inventario.Modelos.Produto
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaErrosImportacaoProdutosBinding

class TelaErrosImportacaoProdutos : AppCompatActivity() {

    private lateinit var adapterErrosProdutos: ProdutosErrosImportacaoAdapter
    private var listaProdutos = emptyList<ErroImportacaoProduto>()

    private val binding by lazy {
        ActivityTelaErrosImportacaoProdutosBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarEventosClick()
        inicializarToolbar()

        adapterErrosProdutos = ProdutosErrosImportacaoAdapter()
        binding.rvErrosImportacaoProdutos.layoutManager = LinearLayoutManager(this)
        binding.rvErrosImportacaoProdutos.adapter = adapterErrosProdutos
        atualizarListaProdutos()

    }

    private fun inicializarEventosClick() {
        binding.btnLimparListaErros.setOnClickListener {

            val produtoDAO = ProdutoDAO(this)
            if (produtoDAO.limparListaErros()) {
                exibirMensagem("Lista erros foi excluída")
                atualizarListaProdutos()
            } else {
                exibirMensagem("Erros ao excluir lista erros")
            }
        }
    }

    private fun atualizarListaProdutos() {
        val produtoDAO = ProdutoDAO(this)
        listaProdutos = produtoDAO.listarErrosImportacao()
        adapterErrosProdutos.atualizarLista(listaProdutos)

    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToobalErrosImportacaoProdutos.tbPrinciapl
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Lista erros importação"
            subtitle = "Produtos duplicados"
            // setDisplayHomeAsUpEnabled(true)
        }
    }
}