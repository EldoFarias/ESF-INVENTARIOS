import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GerenciadorDeFaturamento(contexto: Context) : PurchasesUpdatedListener {

    companion object {
        @Volatile
        private var instance: GerenciadorDeFaturamento? = null

        fun getInstance(context: Context): GerenciadorDeFaturamento {
            return instance ?: synchronized(this) {
                instance ?: GerenciadorDeFaturamento(context.applicationContext).also { instance = it }
            }
        }
    }

    private var clienteDeFaturamento: BillingClient = BillingClient.newBuilder(contexto)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        iniciarConexao()
    }

    private fun iniciarConexao() {
        clienteDeFaturamento.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(resultadoFaturamento: BillingResult) {
                if (resultadoFaturamento.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i("GerenciadorDeFaturamento", "Conexão de faturamento configurada com sucesso.")
                } else {
                    Log.e("GerenciadorDeFaturamento", "Erro ao configurar a conexão de faturamento: ${resultadoFaturamento.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("GerenciadorDeFaturamento", "Conexão de faturamento desconectada.")
                iniciarConexao()
            }
        })
    }

    suspend fun consultarProdutosDisponiveisSuspend(): List<ProductDetails> = withContext(Dispatchers.IO) {
        val listaDeProdutos = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("assinatura_1_semana")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("assinatura_1_mes")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("assinatura_3_meses")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("assinatura_6_meses")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("assinatura_1_ano")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listaDeProdutos)
            .build()

        val resultado = CompletableDeferred<List<ProductDetails>>()
        clienteDeFaturamento.queryProductDetailsAsync(params) { resultadoFaturamento, listaProductDetails ->
            if (resultadoFaturamento.responseCode == BillingClient.BillingResponseCode.OK && listaProductDetails != null) {
                resultado.complete(listaProductDetails)
            } else {
                resultado.complete(emptyList())
            }
        }

        return@withContext resultado.await()
    }

    fun verificarAssinaturasAtivas(callback: (Boolean) -> Unit) {
        clienteDeFaturamento.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { resultadoFaturamento, compras ->
            if (resultadoFaturamento.responseCode == BillingClient.BillingResponseCode.OK && compras != null) {
                val assinaturaAtiva = compras.any { compra ->
                    val isActive = compra.purchaseState == Purchase.PurchaseState.PURCHASED
                    Log.i("GerenciadorDeFaturamento", "Compra encontrada: ${compra.products}, Estado: ${compra.purchaseState}, Reconhecida: ${compra.isAcknowledged}")
                    if (isActive && !compra.isAcknowledged) {
                        processarCompra(compra)  // Reconheço a compra se ainda não foi
                    }
                    isActive
                }
                Log.i("GerenciadorDeFaturamento", "Assinaturas ativas verificadas: $assinaturaAtiva")
                callback(assinaturaAtiva)
            } else {
                Log.e("GerenciadorDeFaturamento", "Erro ao verificar assinaturas ativas: ${resultadoFaturamento.debugMessage}")
                callback(false)
            }
        }
    }

    override fun onPurchasesUpdated(resultadoFaturamento: BillingResult, compras: MutableList<Purchase>?) {
        if (resultadoFaturamento.responseCode == BillingClient.BillingResponseCode.OK && compras != null) {
            for (compra in compras) {
                processarCompra(compra)
            }
        } else if (resultadoFaturamento.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.w("GerenciadorDeFaturamento", "Compra cancelada pelo usuário.")
        } else {
            Log.e("GerenciadorDeFaturamento", "Erro na atualização de compras: ${resultadoFaturamento.debugMessage}")
        }
    }

    private fun processarCompra(compra: Purchase) {
        Log.i("GerenciadorDeFaturamento", "Processando compra: ${compra.products}")

        if (compra.purchaseState == Purchase.PurchaseState.PURCHASED && !compra.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(compra.purchaseToken)
                .build()

            clienteDeFaturamento.acknowledgePurchase(acknowledgePurchaseParams) { resultadoFaturamento ->
                if (resultadoFaturamento.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i("GerenciadorDeFaturamento", "Compra reconhecida com sucesso.")
                    // Conceder acesso ao conteúdo da assinatura
                } else {
                    Log.e("GerenciadorDeFaturamento", "Erro ao reconhecer a compra: ${resultadoFaturamento.debugMessage}")
                }
            }
        }
    }

    fun iniciarFluxoDeCompra(produtoDetalhes: ProductDetails, oferta: ProductDetails.SubscriptionOfferDetails, atividade: Activity) {
        val parametrosFluxo = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(produtoDetalhes)
                        .setOfferToken(oferta.offerToken)
                        .build()
                )
            )
            .build()
        val resultado = clienteDeFaturamento.launchBillingFlow(atividade, parametrosFluxo)
        Log.i("GerenciadorDeFaturamento", "Iniciando fluxo de compra: ${resultado.debugMessage}")
    }
}
