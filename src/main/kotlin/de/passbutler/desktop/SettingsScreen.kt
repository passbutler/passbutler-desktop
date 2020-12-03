package de.passbutler.desktop

import de.passbutler.common.base.Failure
import de.passbutler.common.base.Success
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.PassButlerApplication.Configuration.Companion.applicationConfiguration
import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.ThemeManager
import de.passbutler.desktop.ui.ThemeType
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.jfxToggleButton
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.onLeftClickIgnoringCount
import javafx.scene.Node
import javafx.scene.control.ToggleButton
import tornadofx.FX.Companion.messages
import tornadofx.get
import tornadofx.paddingAll
import tornadofx.vbox

class SettingsScreen : NavigationMenuScreen(messages["settings_title"], navigationMenuItems = createDefaultNavigationMenu()), RequestSending {

    override fun Node.createMainContent() {
        vbox {
            paddingAll = marginM.value

            setupDarkThemeSetting()
        }
    }

    private fun setupDarkThemeSetting() {
        jfxToggleButton(messages["settings_dark_theme_setting_title"]) {
            isSelected = (ThemeManager.themeType == ThemeType.DARK)

            onLeftClickIgnoringCount {
                saveThemeType()
            }
        }
    }

    private fun ToggleButton.saveThemeType() {
        val oldSelectedValue = !isSelected

        launchRequestSending(
            handleSuccess = { newThemeType ->
                ThemeManager.themeType = newThemeType
            },
            handleFailure = {
                // Reset to old value if operation failed
                isSelected = oldSelectedValue

                showError(messages["settings_save_setting_failed_title"])
            }
        ) {
            val newThemeType = when (ThemeManager.themeType) {
                ThemeType.LIGHT -> ThemeType.DARK
                ThemeType.DARK -> ThemeType.LIGHT
            }

            val saveSettingResult = applicationConfiguration.writeValue {
                set(PassButlerApplication.Configuration.THEME_TYPE to newThemeType.name)
            }

            when (saveSettingResult) {
                is Success -> Success(newThemeType)
                is Failure -> Failure(saveSettingResult.throwable)
            }
        }
    }
}
