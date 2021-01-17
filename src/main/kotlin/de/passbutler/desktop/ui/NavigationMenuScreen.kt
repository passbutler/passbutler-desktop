package de.passbutler.desktop.ui

import de.passbutler.desktop.AboutScreen
import de.passbutler.desktop.ImportScreen
import de.passbutler.desktop.OverviewScreen
import de.passbutler.desktop.RecycleBinScreen
import de.passbutler.desktop.SettingsScreen
import javafx.scene.Node
import tornadofx.UIComponent
import tornadofx.addClass
import tornadofx.addStylesheet
import tornadofx.borderpane
import tornadofx.center
import tornadofx.get
import tornadofx.left
import tornadofx.onLeftClick
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

    protected fun setupRootView() {
        with(root) {
            center {
                setupMainContent()
            }

            // Draw afterwards to apply drop shadow over content area
            left {
                setupNavigationMenu()
            }

            // Obtain focus from any inputs if clicked on layout
            onLeftClick {
                requestFocus()
            }
        }
    }

    private fun Node.setupNavigationMenu() {
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
                    setupNavigationItem(messages[it.titleMessageKey], it.icon, it.screenClass)
                }
            }
        }
    }

    private fun Node.setupNavigationItem(title: String, icon: Drawable, screenClass: KClass<out UIComponent>) {
        setupNavigationItem(title, icon) {
            if (!isScreenShown(screenClass)) {
                showScreenUnanimated(screenClass)
            }
        }
    }

    private fun Node.setupNavigationItem(title: String, icon: Drawable, clickedAction: () -> Unit) {
        textLabelWrapped(title) {
            addClass(Theme.navigationViewItemStyle)

            graphic = smallSVGIcon(icon.svgPath)
            graphicTextGap = marginS.value

            onLeftClick(action = clickedAction)
        }
    }

    abstract fun Node.setupMainContent()
}

interface NavigationMenuUsing {
    val navigationMenuItems: List<NavigationItem>

    data class NavigationItem(val titleMessageKey: String, val icon: Drawable, val screenClass: KClass<out UIComponent>)
}

fun createDefaultNavigationMenu(): List<NavigationMenuUsing.NavigationItem> {
    return listOf(
        NavigationMenuUsing.NavigationItem("drawer_menu_item_overview", Drawables.ICON_HOME, OverviewScreen::class),
        NavigationMenuUsing.NavigationItem("drawer_menu_item_settings", Drawables.ICON_SETTINGS, SettingsScreen::class),
        NavigationMenuUsing.NavigationItem("drawer_menu_recycle_bin", Drawables.ICON_DELETE, RecycleBinScreen::class),
        NavigationMenuUsing.NavigationItem("drawer_menu_import", Drawables.ICON_MOVE_TO_INBOX, ImportScreen::class),
        NavigationMenuUsing.NavigationItem("drawer_menu_item_about", Drawables.ICON_INFO, AboutScreen::class),
    )
}
