package com.farias.inventario.Modelos

import com.google.gson.annotations.SerializedName

data class ModeloDataHora(
    @SerializedName("abbreviation") val abreviacao: String,
    @SerializedName("client_ip") val ipDoCliente: String,
    @SerializedName("datetime") val dataHora: String,
    @SerializedName("day_of_week") val diaDaSemana: Int,
    @SerializedName("day_of_year") val diaDoAno: Int,
    @SerializedName("dst") val horarioDeVerao: Boolean,
    @SerializedName("dst_from") val inicioHorarioDeVerao: Any,
    @SerializedName("dst_offset") val offsetHorarioDeVerao: Int,
    @SerializedName("dst_until") val fimHorarioDeVerao: Any,
    @SerializedName("raw_offset") val offsetBruto: Int,
    @SerializedName("timezone") val fusoHorario: String,
    @SerializedName("unixtime") val tempoUnix: Int,
    @SerializedName("utc_datetime") val dataHoraUTC: String,
    @SerializedName("utc_offset") val offsetUTC: String,
    @SerializedName("week_number") val numeroDaSemana: Int
)
