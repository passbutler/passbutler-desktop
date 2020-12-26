package de.passbutler.desktop

import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.ThemeManager
import de.passbutler.desktop.ui.ThemeType
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxToggleButton
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.marginXS
import de.passbutler.desktop.ui.onLeftClickIgnoringCount
import de.passbutler.desktop.ui.showScreenUnanimated
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelHeadline1
import de.passbutler.desktop.ui.textLabelHeadline2
import de.passbutler.desktop.ui.toggle
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ToggleButton
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import tornadofx.FX.Companion.messages
import tornadofx.action
import tornadofx.addClass
import tornadofx.get
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.insets
import tornadofx.onLeftClick
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.px
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
            setupChangeMasterPasswordItem()
        }
    }

    private fun Node.setupDarkThemeItem() {
        val initialSwitchEnabled = (ThemeManager.themeType == ThemeType.DARK)

        setupSwitchSettingItem(messages["settings_dark_theme_setting_title"], messages["settings_dark_theme_setting_summary"], initialSwitchEnabled) {
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

    private fun Node.setupHidePasswordsItem() {
        setupSwitchSettingItem(messages["settings_hide_passwords_setting_title"], messages["settings_hide_passwords_setting_summary"], viewModel.hidePasswordsEnabledSetting) {
            viewModel.hidePasswordsEnabledSetting = !viewModel.hidePasswordsEnabledSetting
        }
    }

    private fun Node.setupChangeMasterPasswordItem() {
        setupTextSettingItem(messages["settings_change_master_password_setting_title"], messages["settings_change_master_password_setting_summary"]) {
            showScreenUnanimated(ChangeMasterPasswordScreen::class)
        }
    }

    private fun Node.setupCategoryItem(title: String, categoryItemsSetup: Node.() -> Unit) {
        textLabelHeadline2(title)

        vbox {
            spacing = marginS.value
            categoryItemsSetup()
        }
    }

    private fun Node.setupSwitchSettingItem(title: String, summary: String, initialSwitchEnabled: Boolean, switchEnabledChanged: ToggleButton.() -> Unit) {
        hbox {
            addClass(Theme.pressableBackgroundStyle)

            alignment = Pos.CENTER_LEFT

            vbox {
                alignment = Pos.CENTER_LEFT
                padding = insets(0.px.value, marginS.value)

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

            val toggleButton = jfxToggleButton {
                paddingAll = 0
                isSelected = initialSwitchEnabled

                action {
                    switchEnabledChanged.invoke(this)
                }
            }

            onLeftClickIgnoringCount {
                toggleButton.toggle()
                switchEnabledChanged.invoke(toggleButton)
            }
        }
    }

    private fun Node.setupTextSettingItem(title: String, summary: String, settingItemAction: () -> Unit) {
        vbox {
            addClass(Theme.pressableBackgroundStyle)

            alignment = Pos.CENTER_LEFT
            padding = insets(0.px.value, marginS.value)

            textLabelBody1(title) {
                style {
                    fontWeight = FontWeight.BOLD
                }
            }

            textLabelBody1(summary) {
                paddingTop = marginXS.value
            }

            onLeftClick {
                settingItemAction.invoke()
            }
        }
    }
}
