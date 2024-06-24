package com.farias.inventario.Activities

import GerenciadorDeFaturamento
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.ProductDetails
import com.farias.inventario.Utilidades.exibirMensagem
import com.farias.inventario.databinding.ActivityTelaAssinaturasBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaAssinaturas : AppCompatActivity() {

    private lateinit var gerenciadorDeFaturamento: GerenciadorDeFaturamento
    private var listaProductDetails: List<ProductDetails> = emptyList()

    private val binding by lazy {
        ActivityTelaAssinaturasBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        gerenciadorDeFaturamento = GerenciadorDeFaturamento.getInstance(this)

        iniciarEventosClick()
    }

    private fun consultarProdutos() {
        CoroutineScope(Dispatchers.IO).launch {
            val produtos = gerenciadorDeFaturamento.consultarProdutosDisponiveisSuspend()
            withContext(Dispatchers.Main) {
                listaProductDetails = produtos
                if (listaProductDetails.isEmpty()) {
                    Log.e("TelaAssinaturas", "Nenhum produto disponível encontrado.")
                    exibirMensagem("Nenhum produto disponível encontrado.")
                } else {
                    Log.i("TelaAssinaturas", "Produtos consultados com sucesso.")
                }
            }
        }
    }

    private fun iniciarEventosClick() {
        binding.btnMostarOpcoesAssinaturas.setOnClickListener {
            consultarProdutos()
            binding.scroolView.visibility = View.VISIBLE
        }

        listOf(
            Pair(binding.btnComprar1Semana, "assinatura_1_semana"),
            Pair(binding.btnComprar1Mes, "assinatura_1_mes"),
            Pair(binding.btnComprar3Meses, "assinatura_3_meses"),
            Pair(binding.btnComprar6Meses, "assinatura_6_meses"),
            Pair(binding.btnComprar1Ano, "assinatura_1_ano")
        ).forEach { (button, productId) ->
            button.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    iniciarFluxoDeCompra(productId)
                }
            }
        }
    }

    private suspend fun iniciarFluxoDeCompra(productId: String) {
        try {
            val produtoDesejado = listaProductDetails.find { it.productId == productId }
            produtoDesejado?.subscriptionOfferDetails?.firstOrNull()?.let { oferta ->
                Log.i(
                    "TelaAssinaturas", "Produto ID: ${produtoDesejado.productId}, " +
                            "Nome: ${produtoDesejado.title}, " +
                            "Preço: ${oferta.pricingPhases.pricingPhaseList.first().formattedPrice}"
                )
                gerenciadorDeFaturamento.iniciarFluxoDeCompra(produtoDesejado, oferta, this@TelaAssinaturas)
            } ?: run {
                Log.e("TelaAssinaturas", "Produto ou oferta desejada não encontrada.")
                exibirMensagem("Produto ou oferta desejada não encontrada.")
            }
        } catch (e: Exception) {
            Log.e("TelaAssinaturas", "Erro ao iniciar fluxo de compra: ${e.message}", e)
            exibirMensagem("Erro ao iniciar fluxo de compra: ${e.message}")
        }
    }
}
