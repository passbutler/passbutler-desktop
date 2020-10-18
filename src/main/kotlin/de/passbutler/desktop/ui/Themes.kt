package de.passbutler.desktop.ui

import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import javafx.scene.paint.Color
import tornadofx.*
import java.net.URI
import kotlin.reflect.KClass

abstract class BaseTheme : Stylesheet() {
    abstract val colorBackground: Color
    abstract val colorSurface: Color

    abstract val colorPrimary: Color
    abstract val colorPrimaryDark: Color

    abstract val colorSecondary: Color

    abstract val colorAccent: Color

    abstract val colorOnPrimary: Color
    abstract val colorOnSecondary: Color

    abstract val colorBackgroundImageTint: Color

    abstract val textColor: Color

    private val fontMedium = loadFont("/fonts/roboto/Roboto-Medium.ttf", textSizeMedium.value)!!.family
    private val fontRegular = loadFont("/fonts/roboto/Roboto-Regular.ttf", textSizeMedium.value)!!.family
    private val fontLight = loadFont("/fonts/roboto/Roboto-Light.ttf", textSizeMedium.value)!!.family
    private val fontBold = loadFont("/fonts/roboto/Roboto-Bold.ttf", textSizeMedium.value)!!.family

    private lateinit var colorBackgroundTransparent: Color
    private lateinit var colorSurfaceTransparent: Color

    protected fun applyStyles() {
        colorBackgroundTransparent = Color.web(colorBackground.css, 0.65)
        colorSurfaceTransparent = Color.web(colorSurface.css, 0.65)

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
            minHeight = 25.dp
        }

        checkBox {
            textFill = textColor
        }

        button {
            backgroundColor += colorPrimary
            textFill = colorOnPrimary

            fontFamily = fontMedium

            minHeight = 25.dp
            padding = box(marginS, marginM)
        }

        abstractBackgroundStyle {
            backgroundImage += URI("/drawables/background.jpg")
            backgroundSize += BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, false, true)
            backgroundRepeat += Pair(BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT)
        }

        abstractBackgroundImageStyle {
            backgroundColor += colorBackgroundImageTint
        }

        cardViewBackgroundStyle {
            backgroundColor += colorSurfaceTransparent
            backgroundRadius = multi(box(4.dp))
        }

        scrimForegroundStyle {
            backgroundColor += colorBackgroundTransparent
        }
    }

    companion object {
        val abstractBackgroundStyle by cssclass()
        val abstractBackgroundImageStyle by cssclass()
        val cardViewBackgroundStyle by cssclass()
        val scrimForegroundStyle by cssclass()
    }
}

class LightTheme : BaseTheme() {
    override val colorBackground = whiteMedium
    override val colorSurface = whiteMedium
    override val colorPrimary = wineRed
    override val colorPrimaryDark = wineRedDark
    override val colorSecondary = pointRed
    override val colorAccent = pointRed
    override val colorOnPrimary = white
    override val colorOnSecondary = white
    override val colorBackgroundImageTint = Color.web("#000000", 0.0)
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
    override val colorOnPrimary = white
    override val colorOnSecondary = white
    override val colorBackgroundImageTint = Color.web("#000000", 0.3)
    override val textColor = white

    init {
        applyStyles()
    }
}

object ThemeManager {

    var theme: Theme = Theme.LIGHT
        private set

    fun changeTheme(newTheme: Theme) {
        theme = newTheme

        when (newTheme) {
            Theme.LIGHT -> {
                removeStylesheet(DarkTheme::class)
                importStylesheet(LightTheme::class)
            }
            Theme.DARK -> {
                removeStylesheet(LightTheme::class)
                importStylesheet(DarkTheme::class)
            }
        }
    }
}

enum class Theme(val kotlinClass: KClass<out BaseTheme>) {
    LIGHT(LightTheme::class),
    DARK(DarkTheme::class)
}