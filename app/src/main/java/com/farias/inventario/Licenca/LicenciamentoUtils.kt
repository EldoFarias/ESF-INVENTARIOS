package com.farias.inventario.Licenca

object LicenciamentoUtils {

    fun getDiasDeLicenca(productId: String?): Int {
        // Verifica se o productId não é nulo
        productId ?: return 0
        // Procura o produto na enumeração ProdutoLicenciamento
        val produto = ProdutoLicenciamento.entries.find { it.productId == productId } ?: return 0
        // Retorna o número de dias associado ao produto encontrado
        return produto.dias
    }
}
