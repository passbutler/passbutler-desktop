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
import tornadofx.mixin
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

    val colorDivider: Color

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

        val contextMenuMixin = mixin {
            backgroundColor = multi(colorBackground)

            label {
                textFill = textColorPrimary
            }

            focused {
                label {
                    textFill = colorOnPrimary
                }
            }
        }

        contextMenu {
            +contextMenuMixin
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

        hyperlink {
            borderWidth = multi(box(0.px))
            padding = box(0.px)
            underline = true
        }

        /**
         * Input styles
         */

        val inputDimensionsMixin = mixin {
            minHeight = 36.px
            minWidth = 156.px
        }

        val inputDefaultsMixin = mixin {
            +inputDimensionsMixin

            textFill = textColorPrimary
            fontSize = textSizeMedium
        }

        textField {
            +inputDefaultsMixin

            padding = box(marginS)
        }

        unmaskablePasswordFieldStyle {
            padding = box(marginS, marginL, marginS, marginS)
        }

        checkBox {
            textFill = textColorPrimary
            fontSize = textSizeMedium
        }

        textArea {
            +inputDefaultsMixin
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

        pressableBackgroundStyle {
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
            }

            and(odd) {
                backgroundColor = multi(listItemBackgroundOdd)
            }

            padding = box(0.px)
        }

        listViewVerticalDividerStyle {
            borderColor = multi(box(colorDivider))
            borderWidth = multi(box(1.px, 0.px))
        }

        listViewSelectableCellStyle {
            listCell {
                and(even, odd) {
                    and(selected) {
                        backgroundColor = multi(listItemBackgroundSelected)

                        label {
                            textFill = colorOnPrimary
                        }

                        // Reapply default label coloring for nested context menu
                        contextMenu {
                            +contextMenuMixin
                        }

                        iconStyle {
                            backgroundColor = multi(colorOnPrimary)
                        }
                    }
                }
            }
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

        iconStyle {
            backgroundColor = multi(textColorPrimary)

            and(disabled) {
                opacity = OPACITY_DISABLED
            }
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
            +inputDimensionsMixin

            backgroundColor = multi(colorPrimary)
            fontFamily = fontMedium
            fontSize = textSizeMedium
            padding = box(marginS, marginM)
            textFill = colorOnPrimary
        }

        floatingActionButtonStyle {
            backgroundColor = multi(colorSecondary)
            minWidth = 45.px
            minHeight = 45.px
            maxWidth = 45.px
            maxHeight = 45.px
        }

        secondaryButtonStyle {
            backgroundColor = multi(transparent)
            borderColor = multi(box(colorPrimary))
            borderRadius = multi(box(3.px))
            borderWidth = multi(box(1.px))
            textFill = colorPrimary
        }

        /**
         * Form
         */

        form {
            field {
                // Remove extra spacing (space in set only via `spacing` of `fieldset`)
                padding = box(0.px)
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

        /**
         * Toolbar
         */

        toolbarStyle {
            alignment = Pos.CENTER_LEFT
            minHeight = 60.px
            padding = box(marginS, marginM)
        }

        /**
         * Emphasized Card
         */

        emphasizedCardStyle {
            backgroundColor = multi(colorBackgroundEmphasized)
            borderColor = multi(box(colorDivider))
            borderRadius = multi(box(3.px))
            borderWidth = multi(box(1.px))
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
        val pressableBackgroundStyle by cssclass()

        val textHeadline1Style by cssclass()
        val textHeadline2Style by cssclass()
        val textBody1Style by cssclass()
        val textBody2Style by cssclass()

        val iconStyle by cssclass()
        val navigationViewStyle by cssclass()
        val navigationViewItemStyle by cssclass()

        val snackbarLayoutStyle by cssclass()
        val floatingActionButtonStyle by cssclass()
        val secondaryButtonStyle by cssclass()

        val scrollPaneBorderlessStyle by cssclass()

        val listViewVerticalDividerStyle by cssclass()
        val listViewSelectableCellStyle by cssclass()
        val listViewPressableCellStyle by cssclass()
        val listViewStaticBackgroundStyle by cssclass()

        val toolbarStyle by cssclass()

        val emphasizedCardStyle by cssclass()

        val unmaskablePasswordFieldStyle by cssclass()
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
        override val colorBackground: Color = grey00
        override val colorBackgroundEmphasized: Color = grey04
        override val colorSurface: Color = grey00
        override val colorPrimary: Color = wineRed
        override val colorPrimaryDark: Color = wineRedDark
        override val colorSecondary: Color = pointRed
        override val colorAccent: Color = pointRed
        override val colorOnPrimary: Color = white
        override val colorOnSecondary: Color = white
        override val colorOnSurface: Color = grey80
        override val scrimBackground: Color = grey00Transparent
        override val colorBackgroundImageTint: Color = Color.web(black.css, 0.0)
        override val listItemBackgroundEven: Color = grey00
        override val listItemBackgroundOdd: Color = grey04
        override val listItemBackgroundSelected: Color = wineRedLight
        override val colorDivider: Color = grey10
        override val textColorPrimary: Color = black
        override val textColorSecondary: Color = blackTransparent
    }
}

class DarkTheme : Theme(), ThemeColors by Companion {

    init {
        applyStyles()
    }

    companion object : ThemeColors {
        override val colorBackground: Color = grey80
        override val colorBackgroundEmphasized: Color = grey84
        override val colorSurface: Color = grey80
        override val colorPrimary: Color = wineRedLight
        override val colorPrimaryDark: Color = wineRed
        override val colorSecondary: Color = pointRed
        override val colorAccent: Color = pointRed
        override val colorOnPrimary: Color = white
        override val colorOnSecondary: Color = white
        override val colorOnSurface: Color = grey00
        override val scrimBackground: Color = grey80Transparent
        override val colorBackgroundImageTint: Color = Color.web(black.css, 0.3)
        override val listItemBackgroundEven: Color = grey80
        override val listItemBackgroundOdd: Color = grey84
        override val listItemBackgroundSelected: Color = wineRedLight
        override val colorDivider: Color = grey70
        override val textColorPrimary: Color = white
        override val textColorSecondary: Color = whiteTransparent
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
                valueOf(name)
            } catch (exception: Exception) {
                null
            }
        }
    }
}
