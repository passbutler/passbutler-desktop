package de.passbutler.desktop

import de.passbutler.common.DecryptMasterEncryptionKeyFailedException
import de.passbutler.common.UpdateUserFailedException
import de.passbutler.common.base.MutableBindable
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.FormFieldValidatorRule
import de.passbutler.desktop.ui.FormValidating
import de.passbutler.desktop.ui.NavigationMenuFragment
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.bindInputOptional
import de.passbutler.desktop.ui.bindVisibility
import de.passbutler.desktop.ui.createCancelButton
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxButton
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.showScreenUnanimated
import de.passbutler.desktop.ui.textLabelBodyOrder1
import de.passbutler.desktop.ui.textLabelHeadlineOrder1
import de.passbutler.desktop.ui.unmaskablePasswordField
import javafx.geometry.Orientation
import javafx.scene.Node
import tornadofx.FX.Companion.messages
import tornadofx.Fieldset
import tornadofx.ValidationContext
import tornadofx.action
import tornadofx.addClass
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.hbox
import tornadofx.paddingTop

class ChangeMasterPasswordScreen : NavigationMenuFragment(messages["change_master_password_title"], navigationMenuItems = createDefaultNavigationMenu()), FormValidating, RequestSending {

    private val validationContext = ValidationContext()

    private val viewModel by injectWithPrivateScope<ChangeMasterPasswordViewModel>()

    private val oldMasterPassword = MutableBindable<String?>(null)
    private val newMasterPassword = MutableBindable<String?>(null)

    init {
        setupRootView()

        shortcut("ESC") {
            showPreviousScreen()
        }
    }

    private fun showPreviousScreen() {
        showScreenUnanimated(SettingsScreen::class)
    }

    override fun Node.setupMainContent() {
        form {
            textLabelHeadlineOrder1(messages["change_master_password_headline"])

            textLabelBodyOrder1(messages["change_master_password_description"]) {
                paddingTop = marginS.value
            }

            textLabelBodyOrder1(messages["change_master_password_disable_biometric_hint"]) {
                paddingTop = marginS.value

                viewModel.loggedInUserViewModel?.biometricUnlockEnabled?.let { biometricUnlockEnabledBindable ->
                    bindVisibility(this@ChangeMasterPasswordScreen, biometricUnlockEnabledBindable)
                }
            }

            fieldset(labelPosition = Orientation.VERTICAL) {
                paddingTop = marginM.value
                spacing = marginS.value

                setupOldPasswordField()
                setupNewPasswordField()
                setupNewPasswordConfirmField()
            }

            hbox {
                paddingTop = marginM.value
                spacing = marginM.value

                setupChangeButton()

                createCancelButton {
                    showPreviousScreen()
                }
            }
        }
    }

    private fun Fieldset.setupOldPasswordField() {
        field(messages["change_master_password_old_master_password_hint"], orientation = Orientation.VERTICAL) {
            unmaskablePasswordField {
                bindInputOptional(this@ChangeMasterPasswordScreen, oldMasterPassword)

                validationContext.validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["change_master_password_old_master_password_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Fieldset.setupNewPasswordField() {
        field(messages["change_master_password_new_master_password_hint"], orientation = Orientation.VERTICAL) {
            unmaskablePasswordField {
                bindInputOptional(this@ChangeMasterPasswordScreen, newMasterPassword)

                validationContext.validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["change_master_password_new_master_password_validation_error_empty"]),
                        FormFieldValidatorRule({ it == oldMasterPassword.value }, messages["change_master_password_new_master_password_validation_error_equal"])
                    )
                }
            }
        }
    }

    private fun Fieldset.setupNewPasswordConfirmField() {
        field(messages["change_master_password_new_master_password_confirm_hint"], orientation = Orientation.VERTICAL) {
            unmaskablePasswordField {
                validationContext.validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it != newMasterPassword.value }, messages["change_master_password_new_master_password_confirm_validation_error_different"])
                    )
                }
            }
        }
    }

    private fun Node.setupChangeButton() {
        jfxButton(messages["change_master_password_button_text"]) {
            addClass(Theme.buttonPrimaryStyle)

            isDefaultButton = true

            action {
                changeMasterPasswordClicked()
            }
        }
    }

    private fun changeMasterPasswordClicked() {
        validationContext.validate()

        val oldMasterPasswordValue = oldMasterPassword.value
        val newMasterPasswordValue = newMasterPassword.value

        if (validationContext.isValid && oldMasterPasswordValue != null && newMasterPasswordValue != null) {
            changeMasterPassword(oldMasterPasswordValue, newMasterPasswordValue)
        }
    }

    private fun changeMasterPassword(oldMasterPassword: String, newMasterPassword: String) {
        launchRequestSending(
            handleSuccess = {
                showInformation(messages["change_master_password_successful_message"])
                showPreviousScreen()
            },
            handleFailure = {
                val errorStringResourceId = when (it) {
                    is DecryptMasterEncryptionKeyFailedException -> "change_master_password_failed_wrong_master_password_title"
                    is UpdateUserFailedException -> "change_master_password_failed_update_user_failed_title"
                    else -> "change_master_password_failed_general_title"
                }

                showError(messages[errorStringResourceId])
            }
        ) {
            viewModel.changeMasterPassword(oldMasterPassword, newMasterPassword)
        }
    }
}
