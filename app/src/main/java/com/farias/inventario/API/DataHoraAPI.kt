package com.farias.inventario.API

import com.farias.inventario.Modelos.ModeloDataHora
import retrofit2.Response
import retrofit2.http.GET

interface DataHoraAPI {
    @GET("api/timezone/Etc/UTC")
   suspend fun recuperarData(): Response<ModeloDataHora>?
}