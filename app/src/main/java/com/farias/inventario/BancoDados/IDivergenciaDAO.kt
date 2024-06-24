package com.farias.inventario.BancoDados

import com.farias.inventario.Modelos.Divergencia
import com.farias.inventario.Modelos.DivergenciaPorContagem
import com.farias.inventario.Modelos.Endereco
import com.farias.inventario.Modelos.EnderecoReduzido

interface IDivergenciaDAO {

    fun compararDivergencia(): Boolean
    fun listarDivergencias(): List<Divergencia>
    fun deletarListaDivergencia(): Boolean
    fun gerarPDF(): List<Divergencia>
    fun gerarPDFPorContagem(contagem: Int): List<DivergenciaPorContagem>
    fun gerarPDFEnderecosNaoContados(): Pair<List<EnderecoReduzido>, Int>
    fun filtrarDivergencias(endereco: String): List<Divergencia>

}