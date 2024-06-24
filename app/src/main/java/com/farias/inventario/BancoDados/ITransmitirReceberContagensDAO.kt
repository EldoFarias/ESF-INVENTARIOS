package com.farias.inventario.BancoDados

import android.content.Context

interface ITransmitirReceberContagensDAO {

    fun receberContagens(): Boolean
    fun transmitirContagens(): Boolean

}