package de.passbutler.desktop

import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
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

    override fun Node.setupMainContent() {
        vbox {
            paddingAll = marginM.value

            textLabelHeadline1(messages["settings_title"])

            vbox {
                paddingTop = marginM.value
                spacing = marginS.value

                setupCategories()
            }
        }
    }

    private fun Node.setupCategories() {
        setupCategoryItem(messages["settings_category_general_title"]) {
            setupDarkThemeItem()
        }

        setupCategoryItem(messages["settings_category_security_title"]) {
            setupHidePasswordsItem()
        }
    }

    private fun Node.setupDarkThemeItem() {
        setupSettingItem(messages["settings_dark_theme_setting_title"], messages["settings_dark_theme_setting_summary"]) {
            jfxToggleButton {
                paddingAll = 0
                isSelected = (ThemeManager.themeType == ThemeType.DARK)

                onLeftClickIgnoringCount {
                    val oldSelectedValue = !isSelected

                    launchRequestSending(
                        handleFailure = {
                            // Reset to old value if operation failed
                            isSelected = oldSelectedValue

                            showError(messages["settings_save_setting_failed_title"])
                        }
                    ) {
                        viewModel.saveThemeType()
                    }
                }
            }
        }
    }

    private fun Node.setupHidePasswordsItem() {
        setupSettingItem(messages["settings_hide_passwords_setting_title"], messages["settings_hide_passwords_setting_summary"]) {
            jfxToggleButton {
                paddingAll = 0
                isSelected = viewModel.hidePasswordsEnabledSetting

                onLeftClickIgnoringCount {
                    viewModel.hidePasswordsEnabledSetting = !viewModel.hidePasswordsEnabledSetting
                }
            }
        }
    }

    private fun Node.setupCategoryItem(title: String, categoryItemsSetup: Node.() -> Unit) {
        textLabelHeadline2(title)

        vbox {
            spacing = marginS.value
            categoryItemsSetup()
        }
    }

    private fun Node.setupSettingItem(title: String, summary: String, settingItemSetup: Node.() -> Unit) {
        hbox {
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

            settingItemSetup()
        }
    }
}
