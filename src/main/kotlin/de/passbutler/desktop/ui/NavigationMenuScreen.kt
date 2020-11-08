package de.passbutler.desktop.ui

import de.passbutler.desktop.AboutScreen
import de.passbutler.desktop.OverviewScreen
import de.passbutler.desktop.SettingsScreen
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.text.TextAlignment
import tornadofx.UIComponent
import tornadofx.addClass
import tornadofx.borderpane
import tornadofx.center
import tornadofx.hbox
import tornadofx.label
import tornadofx.left
import tornadofx.onLeftClick
import tornadofx.paddingAll
import tornadofx.paddingLeft
import tornadofx.px
import tornadofx.style
import tornadofx.svgpath
import tornadofx.vbox
import kotlin.reflect.KClass

abstract class NavigationMenuScreen(title: String? = null, icon: Node? = null) : BaseFragment(title, icon) {

    final override val root = borderpane()

    init {
        with(root) {
            left {
                createNavigationMenu()
            }

            center {
                createMainContent()
            }
        }
    }

    private fun Pane.createNavigationMenu() {
        vbox {
            addClass(BaseTheme.abstractBackgroundStyle)

            spacing = marginS.value

            // TODO: Extract to dedicated style
            style {
                prefWidth = 200.px
                paddingAll = marginM.value
            }

            // TODO: Localize
            createNavigationItem("My Vault", "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z", OverviewScreen::class)
            createNavigationItem("Settings", "M19.14,12.94c0.04-0.3,0.06-0.61,0.06-0.94c0-0.32-0.02-0.64-0.07-0.94l2.03-1.58c0.18-0.14,0.23-0.41,0.12-0.61 l-1.92-3.32c-0.12-0.22-0.37-0.29-0.59-0.22l-2.39,0.96c-0.5-0.38-1.03-0.7-1.62-0.94L14.4,2.81c-0.04-0.24-0.24-0.41-0.48-0.41 h-3.84c-0.24,0-0.43,0.17-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29L5.24,5.33c-0.22-0.08-0.47,0-0.59,0.22L2.74,8.87 C2.62,9.08,2.66,9.34,2.86,9.48l2.03,1.58C4.84,11.36,4.8,11.69,4.8,12s0.02,0.64,0.07,0.94l-2.03,1.58 c-0.18,0.14-0.23,0.41-0.12,0.61l1.92,3.32c0.12,0.22,0.37,0.29,0.59,0.22l2.39-0.96c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54 c0.05,0.24,0.24,0.41,0.48,0.41h3.84c0.24,0,0.44-0.17,0.47-0.41l0.36-2.54c0.59-0.24,1.13-0.56,1.62-0.94l2.39,0.96 c0.22,0.08,0.47,0,0.59-0.22l1.92-3.32c0.12-0.22,0.07-0.47-0.12-0.61L19.14,12.94z M12,15.6c-1.98,0-3.6-1.62-3.6-3.6 s1.62-3.6,3.6-3.6s3.6,1.62,3.6,3.6S13.98,15.6,12,15.6z", SettingsScreen::class)
            createNavigationItem("About", "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z", AboutScreen::class)
        }
    }

    // TODO: Use SVG files
    private fun Pane.createNavigationItem(title: String, icon: String, screenClass: KClass<out UIComponent>) {
        hbox {
            svgpath(icon) {
                fill = whiteMedium // TODO: Correct theming

                val originalWidth = prefWidth(-1.0)
                val originalHeight = prefHeight(originalWidth)

                scaleX = 18.0 / originalWidth
                scaleY = 18.0 / originalHeight
            }
            alignment = Pos.CENTER_LEFT

            label(title) {
                style {
                    textAlignment = TextAlignment.CENTER
                    alignment = Pos.BASELINE_CENTER
                    paddingLeft = marginS.value
                    textFill = whiteMedium // TODO
                }
            }

            onLeftClick {
                if (!isScreenShown(screenClass)) {
                    showScreenUnanimated(screenClass)
                }
            }
        }
    }

    abstract fun Pane.createMainContent()
}

