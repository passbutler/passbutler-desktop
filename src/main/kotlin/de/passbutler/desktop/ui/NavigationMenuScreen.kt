package de.passbutler.desktop.ui

import de.passbutler.desktop.AboutScreen
import de.passbutler.desktop.OverviewScreen
import de.passbutler.desktop.SettingsScreen
import javafx.scene.Node
import javafx.scene.layout.Pane
import tornadofx.UIComponent
import tornadofx.addClass
import tornadofx.addStylesheet
import tornadofx.borderpane
import tornadofx.center
import tornadofx.get
import tornadofx.hbox
import tornadofx.label
import tornadofx.left
import tornadofx.onLeftClick
import tornadofx.paddingLeft
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
            // Enforce dark theme to navigation view because it has dark background
            addStylesheet(DarkTheme::class)

            addClass(Theme.navigationView)

            createNavigationItem(messages["drawer_menu_item_overview"], Drawables.ICON_HOME, OverviewScreen::class)
            createNavigationItem(messages["drawer_menu_item_settings"], Drawables.ICON_SETTINGS, SettingsScreen::class)
            createNavigationItem(messages["drawer_menu_item_about"], Drawables.ICON_INFO, AboutScreen::class)
        }
    }

    private fun Pane.createNavigationItem(title: String, icon: Drawable, screenClass: KClass<out UIComponent>) {
        hbox {
            svgpath(icon.svgPath) {
                addClass(Theme.imageTint)

                val originalWidth = prefWidth(-1.0)
                val originalHeight = prefHeight(originalWidth)

                scaleX = 18.0 / originalWidth
                scaleY = 18.0 / originalHeight
            }

            label(title) {
                paddingLeft = marginS.value
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
