package de.passbutler.desktop.ui

import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.css
import tornadofx.cssclass
import tornadofx.importStylesheet
import tornadofx.loadFont
import tornadofx.multi
import tornadofx.px
import tornadofx.removeStylesheet
import java.net.URI
import kotlin.reflect.KClass

interface ThemeColors {
    val colorBackground: Color

    val colorSurface: Color

    val colorPrimary: Color
    val colorPrimaryDark: Color

    val colorSecondary: Color

    val colorAccent: Color

    val colorOnPrimary: Color
    val colorOnSecondary: Color

    val colorBackgroundImageTint: Color

    val textColorPrimary: Color
    val textColorSecondary: Color
}

abstract class Theme : Stylesheet(), ThemeColors {

    private val fontMedium = loadFontFamily("/fonts/roboto/Roboto-Medium.ttf")
    private val fontRegular = loadFontFamily("/fonts/roboto/Roboto-Regular.ttf")
    private val fontLight = loadFontFamily("/fonts/roboto/Roboto-Light.ttf")
    private val fontBold = loadFontFamily("/fonts/roboto/Roboto-Bold.ttf")

    private lateinit var colorBackgroundTransparent: Color
    private lateinit var colorSurfaceTransparent: Color

    protected fun applyStyles() {
        colorBackgroundTransparent = Color.web(colorBackground.css, 0.65)
        colorSurfaceTransparent = Color.web(colorSurface.css, 0.65)

        root {
            backgroundColor = multi(colorBackground)
            baseColor = colorBackground
            accentColor = colorAccent
            focusColor = colorAccent
            faintFocusColor = transparent

            fontFamily = fontRegular
            fontWeight = FontWeight.NORMAL

            fontSize = BASE_FONT_SIZE.px
        }

        /**
         * Menu
         */

        menuBar {
            backgroundColor = multi(colorBackground)
        }

        contextMenu {
            backgroundColor = multi(colorBackground)
        }

        /**
         * Text styles
         */

        label {
            textFill = textColorPrimary
            fontSize = textSizeMedium
        }

        textHeadline1Style {
            fontFamily = fontLight
            textFill = textColorSecondary
            fontWeight = FontWeight.LIGHT
            fontSize = textSizeLarge
        }

        textBody1Style {
            fontSize = textSizeMedium
        }

        textBody2Style {
            fontSize = textSizeSmall
        }

        /**
         * Input styles
         */

        textField {
            textFill = textColorPrimary
            fontSize = textSizeMedium

            minHeight = 36.px
        }

        checkBox {
            textFill = textColorPrimary
            fontSize = textSizeMedium
        }

        button {
            backgroundColor = multi(colorPrimary)
            textFill = colorOnPrimary

            fontFamily = fontMedium

            minHeight = 36.px
            padding = box(marginS, marginM)
            fontSize = textSizeMedium
        }

        /**
         * Background styles
         */

        abstractBackgroundStyle {
            backgroundImage += URI("/drawables/background.jpg")
            backgroundSize += BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, false, true)
            backgroundRepeat += Pair(BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT)
        }

        abstractBackgroundOverlayStyle {
            backgroundColor = multi(colorBackgroundImageTint)
        }

        cardViewBackgroundStyle {
            backgroundColor = multi(colorSurfaceTransparent)
            backgroundRadius = multi(box(6.px))
        }

        scrimBackgroundStyle {
            backgroundColor = multi(colorBackgroundTransparent)
        }

        /**
         * Image
         */

        imageTint {
            fill = textColorPrimary
        }

        /**
         * Navigation drawer
         */

        navigationView {
            spacing = marginS
            prefWidth = 200.px
            padding = box(marginM)
        }
    }

    companion object {
        val abstractBackgroundStyle by cssclass()
        val abstractBackgroundOverlayStyle by cssclass()
        val cardViewBackgroundStyle by cssclass()
        val scrimBackgroundStyle by cssclass()

        val textHeadline1Style by cssclass()
        val textBody1Style by cssclass()
        val textBody2Style by cssclass()

        val imageTint by cssclass()
        val navigationView by cssclass()
    }
}

private fun loadFontFamily(fontPath: String): String {
    // Do not set proper font size because we only care about the family name
    return loadFont(fontPath, 0)!!.family
}

class LightTheme : Theme(), ThemeColors by LightTheme.Companion {

    init {
        applyStyles()
    }

    companion object : ThemeColors {
        override val colorBackground = whiteMedium
        override val colorSurface = whiteMedium
        override val colorPrimary = wineRed
        override val colorPrimaryDark = wineRedDark
        override val colorSecondary = pointRed
        override val colorAccent = pointRed
        override val colorOnPrimary = white
        override val colorOnSecondary = white
        override val colorBackgroundImageTint = Color.web("#000000", 0.0)
        override val textColorPrimary = black
        override val textColorSecondary = blackTransparent
    }
}

class DarkTheme : Theme(), ThemeColors by DarkTheme.Companion {

    init {
        applyStyles()
    }

    companion object : ThemeColors {
        override val colorBackground = greyMedium
        override val colorSurface = greyMedium
        override val colorPrimary = wineRedLight
        override val colorPrimaryDark = wineRed
        override val colorSecondary = pointRed
        override val colorAccent = pointRed
        override val colorOnPrimary = white
        override val colorOnSecondary = white
        override val colorBackgroundImageTint = Color.web("#000000", 0.3)
        override val textColorPrimary = white
        override val textColorSecondary = whiteTransparent
    }
}

object ThemeManager {

    var themeType: ThemeType = ThemeType.LIGHT
        set(value) {
            field = value

            when (value) {
                ThemeType.LIGHT -> {
                    removeStylesheet(DarkTheme::class)
                    importStylesheet(LightTheme::class)
                }
                ThemeType.DARK -> {
                    removeStylesheet(LightTheme::class)
                    importStylesheet(DarkTheme::class)
                }
            }
        }
}

enum class ThemeType(val kotlinClass: KClass<out Theme>) {
    LIGHT(LightTheme::class),
    DARK(DarkTheme::class)
}
