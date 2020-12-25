package de.passbutler.desktop.ui

import javafx.geometry.Pos
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
    val colorBackgroundEmphasized: Color

    val colorSurface: Color

    val colorPrimary: Color
    val colorPrimaryDark: Color

    val colorSecondary: Color

    val colorAccent: Color

    val colorOnPrimary: Color
    val colorOnSecondary: Color
    val colorOnSurface: Color

    val scrimBackground: Color
    val colorBackgroundImageTint: Color

    val listItemBackgroundEven: Color
    val listItemBackgroundOdd: Color
    val listItemBackgroundSelected: Color

    val textColorPrimary: Color
    val textColorSecondary: Color
}

abstract class Theme : Stylesheet(), ThemeColors {

    private lateinit var colorSurfaceTransparent: Color

    protected fun applyStyles() {
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

        textHeadline2Style {
            fontFamily = fontLight
            textFill = textColorSecondary
            fontWeight = FontWeight.LIGHT
            fontSize = textSizeMedium
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

        textArea {
            textFill = textColorPrimary
            fontSize = textSizeMedium

            minHeight = 36.px
        }

        /**
         * Background styles
         */

        backgroundStyle {
            backgroundColor = multi(colorBackground)
        }

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
            backgroundColor = multi(scrimBackground)
        }

        pressedBackgroundStyle {
            and(pressed) {
                opacity = OPACITY_PRESSED
            }
        }

        /**
         * List view styles
         */

        listView {
            backgroundColor = multi(colorBackground)
            backgroundInsets = multi(box(0.px))
            borderInsets = multi(box(0.px))
            padding = box(0.px)
        }

        listCell {
            backgroundColor = multi(colorBackground)

            and(even) {
                backgroundColor = multi(listItemBackgroundEven)

                and(selected) {
                    backgroundColor = multi(listItemBackgroundSelected)
                }
            }

            and(odd) {
                backgroundColor = multi(listItemBackgroundOdd)

                and(selected) {
                    backgroundColor = multi(listItemBackgroundSelected)
                }
            }

            padding = box(0.px)
        }

        listViewPressableCellStyle {
            listCell {
                // Apply pressed state only for filled, not for empty list cells
                and(filled) {
                    and(pressed) {
                        opacity = OPACITY_PRESSED
                    }
                }
            }
        }

        listViewStaticBackgroundStyle {
            listCell {
                backgroundColor = multi(colorBackground)
            }
        }

        /**
         * Icons
         */

        smallIconStyle {
            backgroundColor = multi(textColorPrimary)

            and(disabled) {
                opacity = OPACITY_DISABLED
            }

            minWidth = 18.px
            minHeight = 18.px
            maxWidth = 18.px
            maxHeight = 18.px
        }

        /**
         * Navigation drawer
         */

        navigationViewStyle {
            spacing = marginS
            prefWidth = 200.px
            padding = box(marginM)
        }

        navigationViewItemStyle {
            alignment = Pos.CENTER_LEFT

            and(pressed) {
                opacity = OPACITY_PRESSED
            }
        }

        /**
         * Snackbar
         */

        snackbarLayoutStyle {
            backgroundColor = multi(colorOnSurface)
            backgroundRadius = multi(box(3.px))
            textFill = colorSurface
            padding = box(marginS, marginM)
            opacity = 0.8
        }

        /**
         * Buttons
         */

        button {
            minWidth = 150.px
        }

        floatingActionButtonStyle {
            backgroundColor = multi(colorAccent)
            minWidth = 45.px
            minHeight = 45.px
            maxWidth = 45.px
            maxHeight = 45.px
        }

        /**
         * Form
         */

        form {
            field {
                // Remove extra spacing (space in set only via `spacing` of `fieldset`)
                padding = box(0.0.px)
            }
        }

        /**
         * Scroll views
         */

        // Do not apply to default style `scrollPane` to avoid affecting `TextArea`
        scrollPaneBorderlessStyle {
            // Remove default border
            padding = box(0.px)
            backgroundInsets = multi(box(0.px))

            // Apply background color to scroll pane, see https://community.oracle.com/thread/3538169 or "modena.css"
            viewport {
                backgroundColor = multi(colorBackground)
            }
        }

        scrollBar {
            // Remove default border
            backgroundInsets = multi(box(0.px))
        }
    }

    companion object {
        val fontMedium = loadFontFamily("/fonts/roboto/Roboto-Medium.ttf")
        val fontRegular = loadFontFamily("/fonts/roboto/Roboto-Regular.ttf")
        val fontLight = loadFontFamily("/fonts/roboto/Roboto-Light.ttf")
        val fontBold = loadFontFamily("/fonts/roboto/Roboto-Bold.ttf")

        // Value took from "modena.css"
        const val OPACITY_DISABLED = 0.4

        const val OPACITY_PRESSED = 0.8

        val backgroundStyle by cssclass()
        val abstractBackgroundStyle by cssclass()
        val abstractBackgroundOverlayStyle by cssclass()
        val cardViewBackgroundStyle by cssclass()
        val scrimBackgroundStyle by cssclass()
        val pressedBackgroundStyle by cssclass()

        val textHeadline1Style by cssclass()
        val textHeadline2Style by cssclass()
        val textBody1Style by cssclass()
        val textBody2Style by cssclass()

        val smallIconStyle by cssclass()
        val navigationViewStyle by cssclass()
        val navigationViewItemStyle by cssclass()

        val snackbarLayoutStyle by cssclass()
        val floatingActionButtonStyle by cssclass()

        val scrollPaneBorderlessStyle by cssclass()

        val listViewPressableCellStyle by cssclass()
        val listViewStaticBackgroundStyle by cssclass()
    }
}

private fun loadFontFamily(fontPath: String): String {
    // Do not set proper font size because we only care about the family name
    return loadFont(fontPath, 0)!!.family
}

class LightTheme : Theme(), ThemeColors by Companion {

    init {
        applyStyles()
    }

    companion object : ThemeColors {
        override val colorBackground = grey00
        override val colorBackgroundEmphasized = grey04
        override val colorSurface = grey00
        override val colorPrimary = wineRed
        override val colorPrimaryDark = wineRedDark
        override val colorSecondary = pointRed
        override val colorAccent = pointRed
        override val colorOnPrimary = white
        override val colorOnSecondary = white
        override val colorOnSurface = grey80
        override val scrimBackground = grey00Transparent
        override val colorBackgroundImageTint = Color.web(black.css, 0.0)
        override val listItemBackgroundEven = grey00
        override val listItemBackgroundOdd = grey04
        override val listItemBackgroundSelected = wineRedLight
        override val textColorPrimary = black
        override val textColorSecondary = blackTransparent
    }
}

class DarkTheme : Theme(), ThemeColors by Companion {

    init {
        applyStyles()
    }

    companion object : ThemeColors {
        override val colorBackground = grey80
        override val colorBackgroundEmphasized = grey84
        override val colorSurface = grey80
        override val colorPrimary = wineRedLight
        override val colorPrimaryDark = wineRed
        override val colorSecondary = pointRed
        override val colorAccent = pointRed
        override val colorOnPrimary = white
        override val colorOnSecondary = white
        override val colorOnSurface = grey00
        override val scrimBackground = grey80Transparent
        override val colorBackgroundImageTint = Color.web(black.css, 0.3)
        override val listItemBackgroundEven = grey80
        override val listItemBackgroundOdd = grey84
        override val listItemBackgroundSelected = wineRedLight
        override val textColorPrimary = white
        override val textColorSecondary = whiteTransparent
    }
}

object ThemeManager {
    var themeType: ThemeType = ThemeType.LIGHT
        set(value) {
            if (value != field) {
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
}

enum class ThemeType(val kotlinClass: KClass<out Theme>) {
    LIGHT(LightTheme::class),
    DARK(DarkTheme::class);

    companion object {
        fun valueOfOrNull(name: String): ThemeType? {
            return try {
                ThemeType.valueOf(name)
            } catch (exception: Exception) {
                null
            }
        }
    }
}
