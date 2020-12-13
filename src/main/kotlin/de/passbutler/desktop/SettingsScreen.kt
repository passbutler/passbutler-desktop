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
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxToggleButton
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.marginXS
import de.passbutler.desktop.ui.onLeftClickIgnoringCount
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelHeadline1
import de.passbutler.desktop.ui.textLabelHeadline2
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ToggleButton
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import tornadofx.FX.Companion.messages
import tornadofx.get
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.region
import tornadofx.style
import tornadofx.vbox

class SettingsScreen : NavigationMenuScreen(messages["settings_title"], navigationMenuItems = createDefaultNavigationMenu()), RequestSending {

    private val viewModel by injectWithPrivateScope<SettingsViewModel>()

    init {
        setupRootView()
    }

    override fun Node.createMainContent() {
        vbox {
            paddingAll = marginM.value

            textLabelHeadline1(messages["settings_title"])

            setupGeneralCategory()
            setupSecurityCategory()
        }
    }

    private fun Node.setupGeneralCategory() {
        textLabelHeadline2(messages["settings_category_general_title"]) {
            paddingTop = marginM.value
        }

        setupDarkThemeItem()
    }

    private fun Node.setupDarkThemeItem() {
        setupSettingItem(messages["settings_dark_theme_setting_title"], messages["settings_dark_theme_setting_summary"]) {
            jfxToggleButton {
                isSelected = (ThemeManager.themeType == ThemeType.DARK)

                onLeftClickIgnoringCount {
                    saveThemeType()
                }
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

    private fun Node.setupSecurityCategory() {
        textLabelHeadline2(messages["settings_category_security_title"]) {
            paddingTop = marginM.value
        }

        setupHidePasswordsItem()
    }

    private fun Node.setupHidePasswordsItem() {
        setupSettingItem(messages["settings_hide_passwords_setting_title"], messages["settings_hide_passwords_setting_summary"]) {
            jfxToggleButton {
                isSelected = viewModel.hidePasswordsEnabledSetting

                onLeftClickIgnoringCount {
                    viewModel.hidePasswordsEnabledSetting = !viewModel.hidePasswordsEnabledSetting
                }
            }
        }
    }

    private fun Node.setupSettingItem(title: String, summary: String, settingNode: Node.() -> Unit) {
        hbox {
            paddingTop = marginS.value

            vbox {
                alignment = Pos.CENTER_LEFT

                textLabelBody1(title) {
                    style {
                        fontWeight = FontWeight.BOLD
                    }
                }

                textLabelBody1(summary) {
                    paddingTop = marginXS.value
                }
            }

            region {
                hgrow = Priority.ALWAYS
            }

            settingNode()
        }
    }
}
