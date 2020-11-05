package de.passbutler.desktop.ui

import tornadofx.Dimension

val textSizeSmall = 12.sp
val textSizeMedium = 16.sp
val textSizeLarge = 24.sp

val Number.sp: Dimension<Dimension.LinearUnits>
    get() {
        val baseFontSizePx = BASE_FONT_SIZE
        val fontSizeEm = (this.toDouble() / baseFontSizePx) * SCALING_FACTOR

        return Dimension(fontSizeEm, Dimension.LinearUnits.em)
    }

const val BASE_FONT_SIZE = 14
private const val SCALING_FACTOR = 0.82