package com.farias.inventario.Utilidades

import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment

class ClasseTitulosRelatorios(text: String) : Paragraph(text) {
    init {
        val red = 0
        val green = 0
        val blue = 0
        val color = DeviceRgb(red, green, blue)

        this.setFontColor(color)
        this.setFontSize(16f)
        this.setBold()
        this.setTextAlignment(TextAlignment.CENTER)
    }
}