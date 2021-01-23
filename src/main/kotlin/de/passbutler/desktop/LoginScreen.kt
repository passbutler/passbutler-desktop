package de.passbutler.desktop

import de.passbutler.common.base.BuildType
import de.passbutler.common.base.MutableBindable
import de.passbutler.common.database.RequestUnauthorizedException
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.base.BuildInformationProvider
import de.passbutler.desktop.base.DebugConstants
import de.passbutler.desktop.base.isHttpsUrl
import de.passbutler.desktop.base.isNetworkUrl
import de.passbutler.desktop.ui.BaseFragment
import de.passbutler.desktop.ui.FormFieldValidatorRule
import de.passbutler.desktop.ui.FormValidating
import de.passbutler.desktop.ui.LONGPRESS_DURATION
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.bind
import de.passbutler.desktop.ui.bindChecked
import de.passbutler.desktop.ui.bindInputOptional
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.showFadeInOutAnimation
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelHeadline1
import de.passbutler.desktop.ui.validateWithRules
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.text.TextAlignment
import tornadofx.FX.Companion.messages
import tornadofx.Fieldset
import tornadofx.ValidationContext
import tornadofx.action
import tornadofx.addClass
import tornadofx.checkbox
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.hbox
import tornadofx.imageview
import tornadofx.longpress
import tornadofx.onLeftClick
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.pane
import tornadofx.passwordfield
import tornadofx.px
import tornadofx.stackpane
import tornadofx.textfield
import tornadofx.useMaxWidth
import tornadofx.vbox
import tornadofx.whenDocked

class LoginScreen : BaseFragment(messages["login_title"]), FormValidating, RequestSending {

    override val root = stackpane()

    override val validationContext = ValidationContext()

    private val viewModel by injectWithPrivateScope<LoginViewModel>()

    private val serverUrl = MutableBindable<String?>(null)
    private val username = MutableBindable<String?>(null)
    private val masterPassword = MutableBindable<String?>(null)
    private val isLocalLogin = MutableBindable(false)

    init {
        with(root) {
            setupRootView()
        }
    }

    private fun Node.setupRootView() {
        stackpane {
            pane {
                addClass(Theme.abstractBackgroundStyle)
            }

            pane {
                addClass(Theme.abstractBackgroundOverlayStyle)
            }

            onLeftClick {
                requestFocus()
            }

            hbox(alignment = Pos.CENTER) {
                vbox(alignment = Pos.CENTER) {
                    setupCardViewContent()
                }
            }
        }
    }

    private fun Node.setupCardViewContent() {
        form {
            addClass(Theme.cardViewBackgroundStyle)

            alignment = Pos.CENTER
            paddingAll = marginM.value
            prefWidth = 320.px.value

            imageview(Image("/drawables/logo_elevated.png", 120.px.value, 0.0, true, true)) {
                setupDebugPresetsButton()
            }

            textLabelHeadline1(messages["login_headline"]) {
                paddingTop = marginM.value
                textAlignment = TextAlignment.CENTER
            }

            textLabelBody1(messages["login_description"]) {
                paddingTop = marginS.value
                textAlignment = TextAlignment.CENTER
            }

            fieldset(labelPosition = Orientation.VERTICAL) {
                paddingTop = marginM.value
                spacing = marginS.value

                setupServerUrlField()
                setupUsernameUrlField()
                setupMasterPasswordUrlField()
                setupLocalLoginCheckbox()
            }

            vbox {
                paddingTop = marginM.value
                setupLoginButton()
            }
        }
    }

    private fun ImageView.setupDebugPresetsButton() {
        if (BuildInformationProvider.buildType == BuildType.Debug) {
            longpress(LONGPRESS_DURATION) {
                serverUrl.value = DebugConstants.TEST_SERVERURL
                username.value = DebugConstants.TEST_USERNAME
                masterPassword.value = DebugConstants.TEST_PASSWORD
                isLocalLogin.value = false
            }
        }
    }

    private fun Fieldset.setupServerUrlField() {
        field(messages["login_serverurl_hint"], orientation = Orientation.VERTICAL) {
            bind(this@LoginScreen, isLocalLogin) { isLocalLogin ->
                val shouldShow = !isLocalLogin
                showFadeInOutAnimation(shouldShow)
            }

            textfield {
                bindInputOptional(this@LoginScreen, serverUrl)

                validateWithRules(this) {
                    listOfNotNull(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["form_serverurl_validation_error_empty"]),
                        FormFieldValidatorRule({ !isNetworkUrl(it) }, messages["form_serverurl_validation_error_invalid"]),
                        FormFieldValidatorRule({ !isHttpsUrl(it) }, messages["form_serverurl_validation_error_invalid_scheme"]).takeIf { BuildInformationProvider.buildType == BuildType.Release }
                    ).takeIf { !isLocalLogin.value }
                }
            }
        }
    }

    private fun Fieldset.setupUsernameUrlField() {
        field(messages["login_username_hint"], orientation = Orientation.VERTICAL) {
            textfield {
                bindInputOptional(this@LoginScreen, username)

                validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["login_username_validation_error_empty"])
                    )
                }

                whenDocked {
                    requestFocus()
                }
            }
        }
    }

    private fun Fieldset.setupMasterPasswordUrlField() {
        field(messages["login_master_password_hint"], orientation = Orientation.VERTICAL) {
            passwordfield {
                bindInputOptional(this@LoginScreen, masterPassword)

                validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["form_master_password_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Node.setupLocalLoginCheckbox() {
        checkbox(messages["login_local_login_label"]) {
            bindChecked(this@LoginScreen, isLocalLogin)
        }
    }

    private fun Node.setupLoginButton() {
        jfxButtonRaised(messages["login_button_text"]) {
            useMaxWidth = true
            isDefaultButton = true

            action {
                loginClicked()
            }
        }
    }

    private fun loginClicked() {
        validationContext.validate()

        val isLocalLoginValue = isLocalLogin.value
        val serverUrlValue = serverUrl.value?.takeIf { !isLocalLoginValue }
        val usernameValue = username.value
        val masterPasswordValue = masterPassword.value

        if (validationContext.isValid && usernameValue != null && masterPasswordValue != null) {
            loginUser(serverUrlValue, usernameValue, masterPasswordValue)
        }
    }

    private fun loginUser(serverUrl: String?, username: String, masterPassword: String) {
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
}