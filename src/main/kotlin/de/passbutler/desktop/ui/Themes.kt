package de.passbutler.desktop.ui

import javafx.scene.paint.Color
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.loadFont
import tornadofx.pt

abstract class BaseTheme : Stylesheet() {
    abstract val colorBackground: Color
    abstract val colorSurface: Color

    abstract val colorPrimary: Color
    abstract val colorPrimaryDark: Color

    abstract val colorSecondary: Color

    abstract val colorAccent: Color

    abstract val scrimBackground: Color

    abstract val colorOnPrimary: Color
    abstract val colorOnSecondary: Color

    abstract val textColor: Color

    protected val fontMedium = loadFont("/fonts/roboto/Roboto-Medium.ttf", textSizeMedium.value)!!.family
    protected val fontRegular = loadFont("/fonts/roboto/Roboto-Regular.ttf", textSizeMedium.value)!!.family
    protected val fontLight = loadFont("/fonts/roboto/Roboto-Light.ttf", textSizeMedium.value)!!.family
    protected val fontBold = loadFont("/fonts/roboto/Roboto-Bold.ttf", textSizeMedium.value)!!.family

    protected fun applyStyles() {
        root {
            backgroundColor += colorBackground
            baseColor = colorBackground
            accentColor = colorAccent
            focusColor = colorAccent
            faintFocusColor = transparent

            fontFamily = fontRegular
        }

        label {
            textFill = textColor
        }

        textField {
            textFill = textColor
            minHeight = 25.pt
        }

        checkBox {
            textFill = textColor
        }

        button {
            backgroundColor += colorPrimary
            textFill = colorOnPrimary

            fontFamily = fontMedium

            minHeight = 25.pt
            padding = box(marginS, marginM)
        }
    }
}

class LightTheme : BaseTheme() {
    override val colorBackground = whiteMedium
    override val colorSurface = whiteMedium
    override val colorPrimary = wineRed
    override val colorPrimaryDark = wineRedDark
    override val colorSecondary = pointRed
    override val colorAccent = pointRed
    override val scrimBackground = whiteMediumTransparent
    override val colorOnPrimary = white
    override val colorOnSecondary = white
    override val textColor = black

    init {
        applyStyles()
    }
}

class DarkTheme : BaseTheme() {
    override val colorBackground = greyMedium
    override val colorSurface = greyMedium
    override val colorPrimary = wineRedLight
    override val colorPrimaryDark = wineRed
    override val colorSecondary = pointRed
    override val colorAccent = pointRed
    override val scrimBackground = greyMediumTransparent
    override val colorOnPrimary = white
    override val colorOnSecondary = white
    override val textColor = white

    init {
        applyStyles()
    }
}