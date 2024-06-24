package com.farias.inventario.BancoDados

import android.content.Context
import android.net.Uri

interface IExportarTodasContagens {

    fun exportarLayoutInvMC(query: String, context: Context, uri: Uri): Boolean
    fun exportarCodigosQuantidades(query: String, delimitador: String, uri: Uri, context: Context): Boolean
    fun exportarCodigoDescricaoQuantidade (query: String, delimitador: String, uri: Uri, context: Context): Boolean


}