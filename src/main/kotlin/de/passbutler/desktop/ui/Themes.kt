package de.passbutler.desktop.ui

import de.passbutler.desktop.ui.ThemeConstants.OPACITY_DISABLED
import de.passbutler.desktop.ui.ThemeConstants.OPACITY_PRESSED
import de.passbutler.desktop.ui.ThemeConstants.RADIUS_MEDIUM
import de.passbutler.desktop.ui.ThemeConstants.RADIUS_SMALL
import de.passbutler.desktop.ui.ThemeConstants.TEXT_SIZE_MEDIUM
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

    private val colorSurfaceTransparent by lazy {
        Color.web(colorSurface.css, 0.65)
    }

    private val contextMenuMixin = mixin {
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

    private val inputColorsMixin = mixin {
        fontSize = TEXT_SIZE_MEDIUM
        textFill = textColorPrimary
    }

    private val inputDimensionsMixin = mixin {
        minHeight = 36.px
        minWidth = 156.px
    }

    private val inputDefaultsMixin = mixin {
        +inputColorsMixin
        +inputDimensionsMixin
    }

    protected fun applyStyles() {
        applyDefaultStyles()
        applyCustomStyles()
    }

    private fun applyDefaultStyles() {
        button {
            +inputDimensionsMixin

            backgroundColor = multi(colorPrimary)
            backgroundRadius = multi(box(RADIUS_SMALL))
            fontFamily = ThemeFonts.ROBOTO_MEDIUM
            fontSize = 14.sp
            padding = box(marginS, marginM)
            textFill = colorOnPrimary
        }

        checkBox {
            +inputColorsMixin
        }

        contextMenu {
            +contextMenuMixin
        }

        form {
            field {
                // Remove extra spacing (space in set only via `spacing` of `fieldset`)
                padding = box(0.px)
            }
        }

        hyperlink {
            borderWidth = multi(box(0.px))
            padding = box(0.px)
            underline = true
        }

        label {
            fontSize = TEXT_SIZE_MEDIUM
            textFill = textColorPrimary
        }

        labelContainer {
            fontSize = 12.sp
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

        listView {
            backgroundColor = multi(colorBackground)
            backgroundInsets = multi(box(0.px))
            borderInsets = multi(box(0.px))
            padding = box(0.px)
        }

        menuBar {
            backgroundColor = multi(colorBackground)
        }

        root {
            accentColor = colorAccent
            backgroundColor = multi(colorBackground)
            baseColor = colorBackground
            faintFocusColor = transparent
            focusColor = colorAccent
            fontFamily = ThemeFonts.ROBOTO_REGULAR
            fontSize = FONT_SIZE_BASE.px
            fontWeight = FontWeight.NORMAL
        }

        scrollBar {
            // Remove default border
            backgroundInsets = multi(box(0.px))
        }

        textArea {
            +inputDefaultsMixin
        }

        textField {
            +inputDefaultsMixin

            padding = box(marginS)
        }
    }

    private fun applyCustomStyles() {
        backgroundAbstractStyle {
            backgroundImage += URI("/drawables/background.jpg")
            backgroundRepeat += Pair(BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT)
            backgroundSize += BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, false, true)
        }

        backgroundOverlayStyle {
            backgroundColor = multi(colorBackgroundImageTint)
        }

        backgroundPressableStyle {
            and(pressed) {
                opacity = OPACITY_PRESSED
            }
        }

        backgroundScrimStyle {
            backgroundColor = multi(scrimBackground)
        }

        backgroundStyle {
            backgroundColor = multi(colorBackground)
        }

        buttonFloatingActionStyle {
            backgroundColor = multi(colorSecondary)
            maxHeight = 45.px
            maxWidth = 45.px
            minHeight = 45.px
            minWidth = 45.px
        }

        buttonSecondaryStyle {
            backgroundColor = multi(transparent)
            borderColor = multi(box(colorPrimary))
            borderRadius = multi(box(RADIUS_SMALL))
            borderWidth = multi(box(1.px))
            textFill = colorPrimary
        }

        cardBackgroundStyle {
            backgroundColor = multi(colorSurfaceTransparent)
            backgroundRadius = multi(box(RADIUS_MEDIUM))
        }

        cardEmphasizedStyle {
            backgroundColor = multi(colorBackgroundEmphasized)
            borderColor = multi(box(colorDivider))
            borderRadius = multi(box(RADIUS_SMALL))
            borderWidth = multi(box(1.px))
        }

        iconStyle {
            backgroundColor = multi(textColorPrimary)

            and(disabled) {
                opacity = OPACITY_DISABLED
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

        listViewStaticBackgroundStyle {
            listCell {
                backgroundColor = multi(colorBackground)
            }
        }

        listViewVerticalDividerStyle {
            borderColor = multi(box(colorDivider))
            borderWidth = multi(box(1.px, 0.px))
        }

        navigationViewItemStyle {
            alignment = Pos.CENTER_LEFT

            and(pressed) {
                opacity = OPACITY_PRESSED
            }
        }

        navigationViewStyle {
            padding = box(marginM)
            prefWidth = 200.px
            spacing = marginS
        }

        // Do not apply to default style `scrollPane` to avoid affecting `TextArea`
        scrollPaneBorderlessStyle {
            // Remove default border
            backgroundInsets = multi(box(0.px))
            padding = box(0.px)

            // Apply background color to scroll pane, see https://community.oracle.com/thread/3538169 or "modena.css"
            viewport {
                backgroundColor = multi(colorBackground)
            }
        }

        snackbarLayoutStyle {
            backgroundColor = multi(colorOnSurface)
            backgroundRadius = multi(box(RADIUS_SMALL))
            fontSize = 14.sp
            opacity = 0.8
            padding = box(marginS, marginM)
            textFill = colorSurface
        }

        textBody1Style {
            fontFamily = ThemeFonts.ROBOTO_REGULAR
            fontSize = TEXT_SIZE_MEDIUM
            textFill = textColorPrimary
        }

        textBody2Style {
            fontFamily = ThemeFonts.ROBOTO_REGULAR
            fontSize = 14.sp
            textFill = textColorPrimary
        }

        textCaptionStyle {
            fontFamily = ThemeFonts.ROBOTO_REGULAR
            fontSize = 12.sp
            textFill = textColorSecondary
        }

        textFieldUnmaskablePasswordStyle {
            padding = box(marginS, marginL, marginS, marginS)
        }

        textHeadline1Style {
            fontFamily = ThemeFonts.ROBOTO_LIGHT
            fontSize = 96.sp
            textFill = textColorSecondary
        }

        textHeadline2Style {
            fontFamily = ThemeFonts.ROBOTO_LIGHT
            fontSize = 60.sp
            textFill = textColorSecondary
        }

        textHeadline3Style {
            fontFamily = ThemeFonts.ROBOTO_REGULAR
            fontSize = 48.sp
            textFill = textColorSecondary
        }

        textHeadline4Style {
            fontFamily = ThemeFonts.ROBOTO_REGULAR
            fontSize = 34.sp
            textFill = textColorSecondary
        }

        textHeadline5Style {
            fontFamily = ThemeFonts.ROBOTO_REGULAR
            fontSize = 24.sp
            textFill = textColorPrimary
        }

        textHeadline6Style {
            fontFamily = ThemeFonts.ROBOTO_MEDIUM
            fontSize = 20.sp
            textFill = textColorPrimary
        }

        textSubtitle1Style {
            fontFamily = ThemeFonts.ROBOTO_REGULAR
            fontSize = TEXT_SIZE_MEDIUM
            textFill = textColorPrimary
        }

        textSubtitle2Style {
            fontFamily = ThemeFonts.ROBOTO_MEDIUM
            fontSize = 14.sp
            textFill = textColorPrimary
        }

        toolbarStyle {
            alignment = Pos.CENTER_LEFT
            minHeight = 60.px
            padding = box(marginS, marginM)
        }
    }

    companion object {
        val backgroundAbstractStyle by cssclass()
        val backgroundOverlayStyle by cssclass()
        val backgroundPressableStyle by cssclass()
        val backgroundScrimStyle by cssclass()
        val backgroundStyle by cssclass()
        val buttonFloatingActionStyle by cssclass()
        val buttonSecondaryStyle by cssclass()
        val cardBackgroundStyle by cssclass()
        val cardEmphasizedStyle by cssclass()
        val iconStyle by cssclass()
        val listViewPressableCellStyle by cssclass()
        val listViewSelectableCellStyle by cssclass()
        val listViewStaticBackgroundStyle by cssclass()
        val listViewVerticalDividerStyle by cssclass()
        val navigationViewItemStyle by cssclass()
        val navigationViewStyle by cssclass()
        val scrollPaneBorderlessStyle by cssclass()
        val snackbarLayoutStyle by cssclass()
        val textBody1Style by cssclass()
        val textBody2Style by cssclass()
        val textCaptionStyle by cssclass()
        val textFieldUnmaskablePasswordStyle by cssclass()
        val textHeadline1Style by cssclass()
        val textHeadline2Style by cssclass()
        val textHeadline3Style by cssclass()
        val textHeadline4Style by cssclass()
        val textHeadline5Style by cssclass()
        val textHeadline6Style by cssclass()
        val textSubtitle1Style by cssclass()
        val textSubtitle2Style by cssclass()
        val toolbarStyle by cssclass()
    }
}

object ThemeFonts {
    val ROBOTO_MEDIUM = loadFontFamily("/fonts/roboto/Roboto-Medium.ttf")
    val ROBOTO_REGULAR = loadFontFamily("/fonts/roboto/Roboto-Regular.ttf")
    val ROBOTO_LIGHT = loadFontFamily("/fonts/roboto/Roboto-Light.ttf")
    val ROBOTO_BOLD = loadFontFamily("/fonts/roboto/Roboto-Bold.ttf")

    private fun loadFontFamily(fontPath: String): String {
        // Do not set proper font size because we only care about the family name
        return loadFont(fontPath, 0)!!.family
    }
}

object ThemeConstants {
    val TEXT_SIZE_MEDIUM = 16.sp

    val RADIUS_SMALL = 3.px
    val RADIUS_MEDIUM = 6.px

    // Value took from "modena.css"
    const val OPACITY_DISABLED = 0.4

    const val OPACITY_PRESSED = 0.8
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
