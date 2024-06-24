package com.farias.inventario.Activities

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.os.IResultReceiver._Parcel
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.farias.inventario.Adapters.ItensAdapter
import com.farias.inventario.BancoDados.ContagemDAO
import com.farias.inventario.BancoDados.EnderecoDAO
import com.farias.inventario.BancoDados.ItensDAO
import com.farias.inventario.Modelos.Itens
import com.farias.inventario.R
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaItensLidosBinding

class TelaItensLidos : AppCompatActivity() {

    private val binding by lazy {
        ActivityTelaItensLidosBinding.inflate(layoutInflater)
    }

    private lateinit var enderecoRecuperado: String
    private lateinit var numeroContagemRecuperado: String
    private  var tipoTela: Int = 1

    private lateinit var itensAdapter: ItensAdapter
    private var listaItens = emptyList<Itens>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        recuperarDadosContagem()
        inicializarToolbar()


        itensAdapter = ItensAdapter { id -> deletarItemContagem(id) }
        binding.rvItensContados.layoutManager = LinearLayoutManager(this)
        binding.rvItensContados.adapter = itensAdapter

        atualizarListaItens(enderecoRecuperado, numeroContagemRecuperado)
    }

    private fun recuperarDadosContagem() {

        val tipo = intent.getExtras()?.getInt("tipo")
        val endereco = intent.getExtras()?.getString("endereco")
        val numeroContagem = intent.getExtras()?.getString("numeroContagem")

        if (endereco != null && numeroContagem != null && tipo != null) {
            enderecoRecuperado = endereco
            numeroContagemRecuperado = numeroContagem
            tipoTela = tipo

        }
    }

    private fun atualizarListaItens(endereco: String, numeroContagem: String) {

        val itensDAO = ItensDAO.getInstance(this)

        if(tipoTela == 0){
            listaItens = itensDAO.listarItens(endereco, numeroContagem)
            val totalItensLidos = itensDAO.totalizarItensLidos(endereco, numeroContagem)
            val totalReferencias = itensDAO.totalizarReferencias(endereco, numeroContagem)
            binding.txtTotalItensLidos.setText("${totalItensLidos}")
            binding.txtTotalReferencias.setText("${totalReferencias}")
            if (listaItens.isEmpty()) {
                mostraMensagemListaVazia()
            } else {
                itensAdapter.atualizarLista(listaItens)
            }
        }else{
            listaItens = itensDAO.listarItensResumido(endereco, numeroContagem)
            val totalItensLidos = itensDAO.totalizarItensLidos(endereco, numeroContagem)
            val totalReferencias = itensDAO.totalizarReferencias(endereco, numeroContagem)
            binding.txtTotalItensLidos.setText("${totalItensLidos}")
            binding.txtTotalReferencias.setText("${totalReferencias}")
            if (listaItens.isEmpty()) {
                mostraMensagemListaVazia()
            } else {
                itensAdapter.atualizarLista(listaItens)
            }
        }
    }

    private fun mostraMensagemListaVazia() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("LISTAGEM DE ITENS.")
        builder.setMessage(
            "A lista de itens está vazia.\n" +
                    "Adicione itens na tela de contagem.\n" +
                    "Os itens serão exibidos nesta tela."
        )
        val dialog = builder.create()
        dialog.show()
    }

    private fun deletarItemContagem(id: Int) {

        if (tipoTela == 0){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("DELETAR ITEM")
            builder.setMessage(
                "Deseja realmente excluir este item?"
            )
            builder.setPositiveButton("SIM"){_, _, ->
                val contagemDAO = ContagemDAO.getInstance(this)
                if (contagemDAO.deletarItemContagem(id)) {
                    exibirMensagem("item removido da contagem")
                    atualizarListaItens(enderecoRecuperado, numeroContagemRecuperado)
                }
            }
            builder.setNegativeButton("NÃO"){_,_, ->

            }
            val dialog = builder.create()
            dialog.show()
        } else {
            exibirMensagem("Apague pela tela de ITENS LIDOS")
        }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeTelaItensContados.tbPrinciapl
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Endereço: ${enderecoRecuperado}"
            subtitle = "Contagem: ${numeroContagemRecuperado}"
            // setDisplayHomeAsUpEnabled(true)
        }
    }
}