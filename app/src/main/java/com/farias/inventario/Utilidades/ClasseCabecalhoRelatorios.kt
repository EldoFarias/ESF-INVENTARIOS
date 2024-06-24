package com.farias.inventario.Utilidades

import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment

class ClasseCabecalhoRelatorios(text: String, fontSize: Float) : Cell() {

    init {

        val red = 211
        val green = 211
        val blue = 211
        val color = DeviceRgb(red, green, blue)

        add(Paragraph(text).setFontSize(fontSize))
            .setTextAlignment(TextAlignment.CENTER).setBold().setBackgroundColor(color)
    }
}
