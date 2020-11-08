package de.passbutler.desktop

import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.ThemeManager
import de.passbutler.desktop.ui.ThemeType
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
                isSelected = (ThemeManager.themeType == ThemeType.DARK)

                onLeftClick {
                    ThemeManager.themeType = when (ThemeManager.themeType) {
                        ThemeType.LIGHT -> ThemeType.DARK
                        ThemeType.DARK -> ThemeType.LIGHT
                    }
                }
            }
        }
    }
}
