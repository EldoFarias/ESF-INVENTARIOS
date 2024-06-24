package com.farias.inventario.Activities

import DivergenciaDAO
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.farias.inventario.Adapters.DivergenciaAdapter
import com.farias.inventario.Modelos.Divergencia
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaAnalisarDivergenciaBinding

class TelaAnalisarDivergencia : AppCompatActivity() {

    private lateinit var divergenciaAdapter: DivergenciaAdapter
    private var listaDivergencia = emptyList<Divergencia>()

    private val binding by lazy {
        ActivityTelaAnalisarDivergenciaBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarEventosClick()
        inicializarToolbar()
        divergenciaAdapter = DivergenciaAdapter()

        binding.rvItensDivergencia.layoutManager = LinearLayoutManager(this)
        binding.rvItensDivergencia.adapter = divergenciaAdapter

    }

    private fun inicializarEventosClick() {

        binding.btnFiltarDivergencia.setOnClickListener {
            val endereco = binding.editFiltroDivergencia.text.toString().trim()
            if (endereco == ""){
                exibirMensagem("Informe um endereço")
            }else{
                val divergenciaDAO = DivergenciaDAO.getInstance(this)

                val enderecoFormatado = String.format("%04d", endereco.toInt()) // Sempre ter 4 caracteres

                listaDivergencia = divergenciaDAO.filtrarDivergencias(enderecoFormatado)
                divergenciaAdapter.atualizarLista(listaDivergencia)
            }
        }

        binding.btnLimparDivergencias.setOnClickListener {
            binding.editFiltroDivergencia.setText("")
            atualizarListaDivergencia()

        }
    }

    override fun onStart() {
        super.onStart()
        atualizarListaDivergencia()
    }

    private fun atualizarListaDivergencia(){

        val divergenciaDAO = DivergenciaDAO.getInstance(this)
        listaDivergencia = divergenciaDAO.listarDivergencias()
        divergenciaAdapter.atualizarLista(listaDivergencia)

    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbarAnalisarDivergencias.tbPrinciapl
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Análise de contagens"
            subtitle = "Itens lidos por contagem"
            // setDisplayHomeAsUpEnabled(true)
        }
    }

}