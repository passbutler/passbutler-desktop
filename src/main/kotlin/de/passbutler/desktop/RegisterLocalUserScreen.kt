package de.passbutler.desktop

import de.passbutler.common.DecryptMasterEncryptionKeyFailedException
import de.passbutler.common.base.BuildType
import de.passbutler.common.base.MutableBindable
import de.passbutler.common.database.RequestConflictedException
import de.passbutler.common.database.RequestForbiddenException
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.base.BuildInformationProvider
import de.passbutler.desktop.base.DebugConstants
import de.passbutler.desktop.base.isHttpsUrl
import de.passbutler.desktop.base.isNetworkUrl
import de.passbutler.desktop.ui.FormFieldValidatorRule
import de.passbutler.desktop.ui.FormValidating
import de.passbutler.desktop.ui.LONGPRESS_DURATION
import de.passbutler.desktop.ui.NavigationMenuFragment
import de.passbutler.desktop.ui.bindInputOptional
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
import tornadofx.longpress
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.passwordfield
import tornadofx.textfield

class RegisterLocalUserScreen : NavigationMenuFragment(messages["register_local_user_title"], navigationMenuItems = createDefaultNavigationMenu()), FormValidating, RequestSending {

    override val validationContext = ValidationContext()

    private val viewModel by injectWithPrivateScope<RegisterLocalUserViewModel>()

    private val serverUrl = MutableBindable<String?>(null)
    private val invitationCode = MutableBindable<String?>(null)
    private val masterPassword = MutableBindable<String?>(null)

    init {
        setupRootView()

        shortcut("ESC") {
            showPreviousScreen()
        }
    }

    private fun showPreviousScreen() {
        showScreenUnanimated(OverviewScreen::class)
    }

    override fun Node.setupMainContent() {
        form {
            paddingAll = marginM.value

            textLabelHeadline1(messages["register_local_user_header"]) {
                setupDebugPresetsButton()
            }

            textLabelBody1(messages["register_local_user_description"]) {
                paddingTop = marginS.value
            }

            fieldset(labelPosition = Orientation.VERTICAL) {
                paddingTop = marginM.value
                spacing = marginS.value

                setupServerUrlField()
                setupInvitationCodeField()
                setupMasterPasswordField()
            }

            hbox {
                paddingTop = marginM.value
                spacing = marginM.value

                setupRegisterButton()

                createCancelButton {
                    showPreviousScreen()
                }
            }
        }
    }

    private fun Node.setupDebugPresetsButton() {
        if (BuildInformationProvider.buildType == BuildType.Debug) {
            longpress(LONGPRESS_DURATION) {
                serverUrl.value = DebugConstants.TEST_SERVERURL
                invitationCode.value = DebugConstants.TEST_INVITATION_CODE
                masterPassword.value = DebugConstants.TEST_PASSWORD
            }
        }
    }

    private fun Fieldset.setupServerUrlField() {
        field(messages["register_local_user_serverurl_hint"], orientation = Orientation.VERTICAL) {
            textfield {
                bindInputOptional(this@RegisterLocalUserScreen, serverUrl)

                validateWithRules(this) {
                    listOfNotNull(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["form_serverurl_validation_error_empty"]),
                        FormFieldValidatorRule({ !isNetworkUrl(it) }, messages["form_serverurl_validation_error_invalid"]),
                        FormFieldValidatorRule({ !isHttpsUrl(it) }, messages["form_serverurl_validation_error_invalid_scheme"]).takeIf { BuildInformationProvider.buildType == BuildType.Release }
                    )
                }
            }
        }
    }

    private fun Fieldset.setupInvitationCodeField() {
        field(messages["register_local_user_invitation_code_hint"], orientation = Orientation.VERTICAL) {
            textfield {
                bindInputOptional(this@RegisterLocalUserScreen, invitationCode)

                validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["register_local_user_invitation_code_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Fieldset.setupMasterPasswordField() {
        field(messages["register_local_user_master_password_hint"], orientation = Orientation.VERTICAL) {
            passwordfield {
                bindInputOptional(this@RegisterLocalUserScreen, masterPassword)

                validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["form_master_password_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Node.setupRegisterButton() {
        jfxButtonRaised(messages["register_local_user_button_text"]) {
            isDefaultButton = true

            action {
                registerClicked()
            }
        }
    }

    private fun registerClicked() {
        validationContext.validate()

        val serverUrlValue = serverUrl.value
        val invitationCodeValue = invitationCode.value
        val masterPasswordValue = masterPassword.value

        if (validationContext.isValid && serverUrlValue != null && invitationCodeValue != null && masterPasswordValue != null) {
            registerLocalUser(serverUrlValue, invitationCodeValue, masterPasswordValue)
        }
    }

    private fun registerLocalUser(serverUrl: String, invitationCode: String, masterPassword: String) {
        launchRequestSending(
            handleSuccess = {
                showInformation(messages["register_local_user_successful_message"])
                showPreviousScreen()
            },
            handleFailure = {
                val errorStringResourceId = when (it) {
                    is DecryptMasterEncryptionKeyFailedException -> "register_local_user_failed_wrong_master_password_title"
                    is RequestForbiddenException -> "register_local_user_failed_forbidden_title"
                    is RequestConflictedException -> "register_local_user_failed_username_existing_title"
                    else -> "register_local_user_failed_general_title"
                }

                showError(messages[errorStringResourceId])
            }
        ) {
            viewModel.registerLocalUser(serverUrl, invitationCode, masterPassword)
        }
    }
}
