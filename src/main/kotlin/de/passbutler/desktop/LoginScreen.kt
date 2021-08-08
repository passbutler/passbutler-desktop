package de.passbutler.desktop

import de.passbutler.common.base.BuildType
import de.passbutler.common.base.MutableBindable
import de.passbutler.common.database.RequestUnauthorizedException
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.base.BuildInformationProvider
import de.passbutler.desktop.base.DebugConstants
import de.passbutler.desktop.base.UrlExtensions
import de.passbutler.desktop.ui.BaseFragment
import de.passbutler.desktop.ui.FormFieldValidatorRule
import de.passbutler.desktop.ui.FormValidating
import de.passbutler.desktop.ui.LONGPRESS_DURATION
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.bindInputOptional
import de.passbutler.desktop.ui.createTransparentSectionedLayout
import de.passbutler.desktop.ui.createHeaderView
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxButton
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.showScreenFaded
import de.passbutler.desktop.ui.textLabelBase
import de.passbutler.desktop.ui.textLabelBodyOrder1
import de.passbutler.desktop.ui.unmaskablePasswordField
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import tornadofx.FX.Companion.messages
import tornadofx.Fieldset
import tornadofx.ValidationContext
import tornadofx.action
import tornadofx.addClass
import tornadofx.box
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.longpress
import tornadofx.paddingTop
import tornadofx.px
import tornadofx.stackpane
import tornadofx.style
import tornadofx.textfield
import tornadofx.useMaxWidth
import tornadofx.vbox

class LoginScreen : BaseFragment(messages["login_title"]), FormValidating, RequestSending {

    override val root = stackpane()

    private val validationContext = ValidationContext()

    private val viewModel by injectWithPrivateScope<LoginViewModel>()

    private val serverUrl = MutableBindable<String?>(null)
    private val username = MutableBindable<String?>(null)
    private val masterPassword = MutableBindable<String?>(null)

    init {
        with(root) {
            setupRootView()
        }

        shortcut("ESC") {
            showPreviousScreen()
        }
    }

    private fun showPreviousScreen() {
        showScreenFaded(IntroductionScreen::class)
    }

    private fun Node.setupRootView() {
        createTransparentSectionedLayout(
            topSetup = {
                setupHeader()
            },
            centerSetup = {
                setupContent()
            },
            bottomSetup = {
                setupFooter()
            }
        )
    }

    private fun Node.setupHeader() {
        createHeaderView {
            setupDebugPresetsButton()
        }
    }

    private fun Node.setupDebugPresetsButton() {
        if (BuildInformationProvider.buildType == BuildType.Debug) {
            longpress(LONGPRESS_DURATION) {
                serverUrl.value = DebugConstants.TEST_SERVERURL
                username.value = DebugConstants.TEST_USERNAME
                masterPassword.value = DebugConstants.TEST_PASSWORD
            }
        }
    }

    private fun Node.setupContent() {
        vbox(alignment = Pos.CENTER_LEFT) {
            textLabelBase(messages["login_headline"]) {
                addClass(Theme.textHeadline4Style)
            }

            textLabelBodyOrder1(messages["login_description"]) {
                paddingTop = marginS.value
            }

            form {
                // Reset the default padding from theme style
                style {
                    padding = box(0.px)
                }

                fieldset(labelPosition = Orientation.VERTICAL) {
                    paddingTop = marginM.value
                    spacing = marginS.value

                    setupServerUrlField()
                    setupUsernameField()
                    setupMasterPasswordField()
                }

                vbox {
                    paddingTop = marginM.value
                    setupLoginButton()
                }
            }
        }
    }

    private fun Fieldset.setupServerUrlField() {
        field(messages["general_serverurl_hint"], orientation = Orientation.VERTICAL) {
            textfield {
                bindInputOptional(this@LoginScreen, serverUrl)

                validationContext.validateWithRules(this) {
                    listOfNotNull(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["form_serverurl_validation_error_empty"]),
                        FormFieldValidatorRule({ !UrlExtensions.isNetworkUrl(it) }, messages["form_serverurl_validation_error_invalid"]),
                        FormFieldValidatorRule({ !UrlExtensions.isHttpsUrl(it) }, messages["form_serverurl_validation_error_invalid_scheme"]).takeIf { BuildInformationProvider.buildType == BuildType.Release }
                    )
                }
            }
        }
    }

    private fun Fieldset.setupUsernameField() {
        field(messages["general_username_hint"], orientation = Orientation.VERTICAL) {
            textfield {
                bindInputOptional(this@LoginScreen, username)

                validationContext.validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["form_username_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Fieldset.setupMasterPasswordField() {
        field(messages["general_master_password_hint"], orientation = Orientation.VERTICAL) {
            unmaskablePasswordField {
                bindInputOptional(this@LoginScreen, masterPassword)

                validationContext.validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["form_master_password_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Node.setupLoginButton() {
        jfxButton(messages["login_button_text"]) {
            addClass(Theme.buttonPrimaryStyle)

            useMaxWidth = true
            isDefaultButton = true

            action {
                loginClicked()
            }
        }
    }

    private fun loginClicked() {
        validationContext.validate()

        val serverUrlValue = serverUrl.value
        val usernameValue = username.value
        val masterPasswordValue = masterPassword.value

        if (validationContext.isValid && serverUrlValue != null && usernameValue != null && masterPasswordValue != null) {
            loginUser(serverUrlValue, usernameValue, masterPasswordValue)
        }
    }

    private fun loginUser(serverUrl: String, username: String, masterPassword: String) {
        launchRequestSending(
            handleFailure = {
                val errorStringResourceId = when (it) {
                    is RequestUnauthorizedException -> "login_failed_unauthorized_title"
                    else -> "login_failed_general_title"
                }

                showError(messages[errorStringResourceId])
            },
            isCancellable = false
        ) {
            viewModel.loginUser(serverUrl, username, masterPassword)
        }
    }

    private fun Node.setupFooter() {
        vbox(alignment = Pos.CENTER_LEFT) {
            jfxButton(messages["general_back"]) {
                addClass(Theme.buttonTextOnSurfaceStyle)

                action {
                    showPreviousScreen()
                }
            }
        }
    }
}
