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

abstract class NavigationMenuScreen(
    title: String? = null,
    icon: Node? = null,
    override val navigationMenuItems: List<NavigationMenuUsing.NavigationItem>
) : BaseFragment(title, icon), NavigationMenuUsing {

    final override val root = borderpane()

    init {
        with(root) {
            center {
                createMainContent()
            }

            // Draw afterwards to apply drop shadow over content area
            left {
                createNavigationMenu()
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

                navigationMenuItems.forEach {
                    createNavigationItem(messages[it.titleMessageKey], it.icon, it.screenClass)
                }
            }
        }
    }

    private fun Node.createNavigationItem(title: String, icon: Drawable, screenClass: KClass<out UIComponent>) {
        createNavigationItem(title, icon) {
            if (!isScreenShown(screenClass)) {
                showScreenUnanimated(screenClass)
            }
        }
    }

    private fun Node.createNavigationItem(title: String, icon: Drawable, clickedAction: () -> Unit) {
        hbox {
            addClass(Theme.navigationViewItemStyle)

            smallSVGIcon(icon.svgPath)

            label(title) {
                paddingLeft = marginS.value
            }

            onLeftClick(action = clickedAction)
        }
    }

    abstract fun Node.createMainContent()
}

interface NavigationMenuUsing {
    val navigationMenuItems: List<NavigationItem>

    data class NavigationItem(val titleMessageKey: String, val icon: Drawable, val screenClass: KClass<out UIComponent>)
}

fun createDefaultNavigationMenu(): List<NavigationMenuUsing.NavigationItem> {
    return listOf(
        NavigationMenuUsing.NavigationItem("drawer_menu_item_overview", Drawables.ICON_HOME, OverviewScreen::class),
        NavigationMenuUsing.NavigationItem("drawer_menu_item_settings", Drawables.ICON_SETTINGS, SettingsScreen::class),
        NavigationMenuUsing.NavigationItem("drawer_menu_item_about", Drawables.ICON_INFO, AboutScreen::class),
    )
}
