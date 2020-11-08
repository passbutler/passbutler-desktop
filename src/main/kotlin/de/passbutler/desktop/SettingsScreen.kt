package de.passbutler.desktop

import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.ThemeManager
import de.passbutler.desktop.ui.jfxToggleButton
import de.passbutler.desktop.ui.marginM
import javafx.scene.layout.Pane
import tornadofx.FX.Companion.messages
import tornadofx.get
import tornadofx.onLeftClick
import tornadofx.paddingAll
import tornadofx.vbox

class SettingsScreen : NavigationMenuScreen(messages["settings_title"]) {

    override fun Pane.createMainContent() {
        vbox {
            paddingAll = marginM.value

            jfxToggleButton(messages["settings_dark_theme_setting_title"]) {
                isSelected = (ThemeManager.theme == Theme.DARK)

                onLeftClick {
                    val newTheme = when (ThemeManager.theme) {
                        Theme.LIGHT -> Theme.DARK
                        Theme.DARK -> Theme.LIGHT
                    }

                    ThemeManager.changeTheme(newTheme)
                }
            }
        }
    }
}
