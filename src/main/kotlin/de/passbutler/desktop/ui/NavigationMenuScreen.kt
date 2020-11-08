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
import tornadofx.get
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

            // TODO: Extract to dedicated style
            style {
                spacing = marginS
                prefWidth = 200.px
                paddingAll = marginM.value
            }

            createNavigationItem(messages["drawer_menu_item_overview"], Drawables.ICON_HOME, OverviewScreen::class)
            createNavigationItem(messages["drawer_menu_item_settings"], Drawables.ICON_SETTINGS, SettingsScreen::class)
            createNavigationItem(messages["drawer_menu_item_about"], Drawables.ICON_INFO, AboutScreen::class)
        }
    }

    private fun Pane.createNavigationItem(title: String, icon: Drawable, screenClass: KClass<out UIComponent>) {
        hbox {
            svgpath(icon.svgPath) {
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

