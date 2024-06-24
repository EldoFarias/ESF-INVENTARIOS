package com.farias.inventario.Utilidades

import android.graphics.Color
import com.farias.inventario.R
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment

class ClasseConteudoRelatorios(text: String, fontSize: Float) : Cell() {

    init {

        val red = 220
        val green = 220
        val blue = 220
        val color = DeviceRgb(red, green, blue)

        add(Paragraph(text).setFontSize(fontSize)).setBackgroundColor(color).setTextAlignment(TextAlignment.CENTER)
    }
}
