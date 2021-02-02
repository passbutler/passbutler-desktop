package de.passbutler.desktop

import de.passbutler.common.DecryptMasterEncryptionKeyFailedException
import de.passbutler.common.UpdateUserFailedException
import de.passbutler.common.base.MutableBindable
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.FormFieldValidatorRule
import de.passbutler.desktop.ui.FormValidating
import de.passbutler.desktop.ui.NavigationMenuFragment
import de.passbutler.desktop.ui.bindInputOptional
import de.passbutler.desktop.ui.bindVisibility
import de.passbutler.desktop.ui.createCancelButton
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.showScreenUnanimated
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelHeadline1
import de.passbutler.desktop.ui.validateWithRules
import javafx.geometry.Orientation
import javafx.scene.Node
import tornadofx.FX.Companion.messages
import tornadofx.Fieldset
import tornadofx.ValidationContext
import tornadofx.action
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.hbox
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.passwordfield

class ChangeMasterPasswordScreen : NavigationMenuFragment(messages["change_master_password_title"], navigationMenuItems = createDefaultNavigationMenu()), FormValidating, RequestSending {

    override val validationContext = ValidationContext()

    private val viewModel by injectWithPrivateScope<ChangeMasterPasswordViewModel>()

    private val oldMasterPassword = MutableBindable<String?>(null)
    private val newMasterPassword = MutableBindable<String?>(null)
    private val newMasterPasswordConfirm = MutableBindable<String?>(null)

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
            paddingAll = marginM.value

            textLabelHeadline1(messages["change_master_password_header"])

            textLabelBody1(messages["change_master_password_description"]) {
                paddingTop = marginS.value
            }

            textLabelBody1(messages["change_master_password_disable_biometric_hint"]) {
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
            passwordfield {
                bindInputOptional(this@ChangeMasterPasswordScreen, oldMasterPassword)

                validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["change_master_password_old_master_password_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Fieldset.setupNewPasswordField() {
        field(messages["change_master_password_new_master_password_hint"], orientation = Orientation.VERTICAL) {
            passwordfield {
                bindInputOptional(this@ChangeMasterPasswordScreen, newMasterPassword)

                validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["change_master_password_new_master_password_validation_error_empty"]),
                        FormFieldValidatorRule({ oldMasterPassword.value == it }, messages["change_master_password_new_master_password_validation_error_equal"])
                    )
                }
            }
        }
    }

    private fun Fieldset.setupNewPasswordConfirmField() {
        field(messages["change_master_password_new_master_password_confirm_hint"], orientation = Orientation.VERTICAL) {
            passwordfield {
                bindInputOptional(this@ChangeMasterPasswordScreen, newMasterPasswordConfirm)

                validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ newMasterPassword.value != it }, messages["change_master_password_new_master_password_confirm_validation_error_different"])
                    )
                }
            }
        }
    }

    private fun Node.setupChangeButton() {
        jfxButtonRaised(messages["change_master_password_button_text"]) {
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