package de.passbutler.desktop.ui

import tornadofx.Dimension

const val FONT_SIZE_BASE = 14
private const val FONT_SIZE_SCALING_FACTOR = 0.82

val Number.sp: Dimension<Dimension.LinearUnits>
    get() {
        val baseFontSizePx = FONT_SIZE_BASE
        val fontSizeEm = (this.toDouble() / baseFontSizePx) * FONT_SIZE_SCALING_FACTOR

        return Dimension(fontSizeEm, Dimension.LinearUnits.em)
    }
