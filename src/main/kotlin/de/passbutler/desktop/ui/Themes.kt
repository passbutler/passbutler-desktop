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

abstract class BaseTheme : Stylesheet() {
    abstract val colorBackground: Color
    abstract val colorBackgroundInverted: Color

    abstract val colorSurface: Color

    abstract val colorPrimary: Color
    abstract val colorPrimaryDark: Color

    abstract val colorSecondary: Color

    abstract val colorAccent: Color

    abstract val colorOnPrimary: Color
    abstract val colorOnSecondary: Color

    abstract val colorBackgroundImageTint: Color

    abstract val textColor: Color
    abstract val textColorInverted: Color

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
            backgroundColor = multi(colorBackgroundInverted)

            label {
                textFill = textColorInverted
            }
        }

        contextMenu {
            backgroundColor = multi(colorBackgroundInverted)
        }

        /**
         * Text styles
         */

        label {
            textFill = textColor
            fontSize = textSizeMedium
        }

        textHeadline1Style {
            fontFamily = fontLight
            textFill = Color.web("#6C5F5D") // TODO: Create theme color
            fontWeight = FontWeight.LIGHT
            fontSize = textSizeLarge
        }

        textBody1Style {
            fontWeight = FontWeight.NORMAL
        }

        /**
         * Input styles
         */

        textField {
            textFill = textColor
            fontSize = textSizeMedium

            minHeight = 36.px
        }

        checkBox {
            textFill = textColor
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

        abstractBackgroundImageStyle {
            backgroundColor = multi(colorBackgroundImageTint)
        }

        cardViewBackgroundStyle {
            backgroundColor = multi(colorSurfaceTransparent)
            backgroundRadius = multi(box(6.px))
        }

        scrimBackgroundStyle {
            backgroundColor = multi(colorBackgroundTransparent)
        }
    }

    companion object {
        val abstractBackgroundStyle by cssclass()
        val abstractBackgroundImageStyle by cssclass()
        val cardViewBackgroundStyle by cssclass()
        val scrimBackgroundStyle by cssclass()

        val textHeadline1Style by cssclass()
        val textBody1Style by cssclass()
    }
}

private fun loadFontFamily(fontPath: String): String {
    // Do not set proper font size because we only care about the family name
    return loadFont(fontPath, 0)!!.family
}

class LightTheme : BaseTheme() {
    override val colorBackground = whiteMedium
    override val colorBackgroundInverted = greyMedium
    override val colorSurface = whiteMedium
    override val colorPrimary = wineRed
    override val colorPrimaryDark = wineRedDark
    override val colorSecondary = pointRed
    override val colorAccent = pointRed
    override val colorOnPrimary = white
    override val colorOnSecondary = white
    override val colorBackgroundImageTint = Color.web("#000000", 0.0)
    override val textColor = black
    override val textColorInverted = white

    init {
        applyStyles()
    }
}

class DarkTheme : BaseTheme() {
    override val colorBackground = greyMedium
    override val colorBackgroundInverted = whiteMedium
    override val colorSurface = greyMedium
    override val colorPrimary = wineRedLight
    override val colorPrimaryDark = wineRed
    override val colorSecondary = pointRed
    override val colorAccent = pointRed
    override val colorOnPrimary = white
    override val colorOnSecondary = white
    override val colorBackgroundImageTint = Color.web("#000000", 0.3)
    override val textColor = white
    override val textColorInverted = black

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