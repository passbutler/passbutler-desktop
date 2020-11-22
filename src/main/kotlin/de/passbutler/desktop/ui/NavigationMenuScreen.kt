package de.passbutler.desktop.ui

import de.passbutler.desktop.AboutScreen
import de.passbutler.desktop.OverviewScreen
import de.passbutler.desktop.SettingsScreen
import javafx.scene.Node
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
import tornadofx.pane
import tornadofx.stackpane
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

    private fun Node.createNavigationMenu() {
        stackpane {
            pane {
                addClass(Theme.abstractBackgroundStyle)

                effect = endDropShadow()
            }

            pane {
                addClass(Theme.abstractBackgroundOverlayStyle)
            }

            vbox {
                // Enforce dark theme to navigation view because it has dark background
                addStylesheet(DarkTheme::class)

                addClass(Theme.navigationViewStyle)

                createNavigationItem(messages["drawer_menu_item_overview"], Drawables.ICON_HOME, OverviewScreen::class)
                createNavigationItem(messages["drawer_menu_item_settings"], Drawables.ICON_SETTINGS, SettingsScreen::class)
                createNavigationItem(messages["drawer_menu_item_about"], Drawables.ICON_INFO, AboutScreen::class)
            }
        }
    }

    protected fun Node.createNavigationItem(title: String, icon: Drawable, screenClass: KClass<out UIComponent>) {
        createNavigationItem(title, icon) {
            if (!isScreenShown(screenClass)) {
                showScreenUnanimated(screenClass)
            }
        }
    }

    private fun Node.createNavigationItem(title: String, icon: Drawable, clickedAction: () -> Unit) {
        hbox {
            smallSVGIcon(icon.svgPath)

            label(title) {
                paddingLeft = marginS.value
            }

            onLeftClick(action = clickedAction)
        }
    }

    abstract fun Node.createMainContent()
}
