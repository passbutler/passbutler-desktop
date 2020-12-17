package de.passbutler.desktop

import de.passbutler.common.base.BuildType
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
import tornadofx.Field
import tornadofx.Fieldset
import tornadofx.ValidationContext
import tornadofx.action
import tornadofx.addClass
import tornadofx.box
import tornadofx.checkbox
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.hbox
import tornadofx.imageview
import tornadofx.longpress
import tornadofx.onLeftClick
import tornadofx.paddingTop
import tornadofx.pane
import tornadofx.passwordfield
import tornadofx.px
import tornadofx.stackpane
import tornadofx.style
import tornadofx.textfield
import tornadofx.useMaxWidth
import tornadofx.vbox
import tornadofx.whenDocked

class LoginScreen : BaseFragment(messages["login_title"]), FormValidating, RequestSending {

    override val root = stackpane()

    override val validationContext = ValidationContext()

    private val viewModel by injectWithPrivateScope<LoginViewModel>()

    private var serverUrlField: Field? = null

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

            style {
                padding = box(marginM)
            }

            alignment = Pos.CENTER
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
                paddingTop = marginS.value
                spacing = marginS.value

                serverUrlField = createServerUrlField()
                setupUsernameUrlField()
                setupPasswordUrlField()
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
                viewModel.serverUrlProperty.set(DebugConstants.TEST_SERVERURL)
                viewModel.usernameProperty.set(DebugConstants.TEST_USERNAME)
                viewModel.passwordProperty.set(DebugConstants.TEST_PASSWORD)
                viewModel.isLocalLoginProperty.set(false)
            }
        }
    }

    private fun Fieldset.createServerUrlField(): Field {
        return field(messages["login_serverurl_hint"], orientation = Orientation.VERTICAL) {
            isVisible = !viewModel.isLocalLoginProperty.value

            textfield(viewModel.serverUrlProperty) {
                validateWithRules(this) {
                    listOfNotNull(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["form_serverurl_validation_error_empty"]),
                        FormFieldValidatorRule({ !isNetworkUrl(it) }, messages["form_serverurl_validation_error_invalid"]),
                        FormFieldValidatorRule({ !isHttpsUrl(it) }, messages["form_serverurl_validation_error_invalid_scheme"]).takeIf { BuildInformationProvider.buildType == BuildType.Release }
                    ).takeIf { !viewModel.isLocalLoginProperty.value }
                }
            }
        }
    }

    private fun Fieldset.setupUsernameUrlField() {
        field(messages["login_username_hint"], orientation = Orientation.VERTICAL) {
            textfield(viewModel.usernameProperty) {
                whenDocked {
                    requestFocus()
                }

                validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["login_username_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Fieldset.setupPasswordUrlField() {
        field(messages["login_master_password_hint"], orientation = Orientation.VERTICAL) {
            passwordfield(viewModel.passwordProperty) {
                validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["form_master_password_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Node.setupLocalLoginCheckbox() {
        checkbox(messages["login_local_login_label"], viewModel.isLocalLoginProperty) {
            action {
                val shouldShow = !viewModel.isLocalLoginProperty.value
                serverUrlField?.showFadeInOutAnimation(shouldShow)
            }
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

        if (validationContext.isValid) {
            val isLocalLogin = viewModel.isLocalLoginProperty.value

            val serverUrl = viewModel.serverUrlProperty.value?.takeIf { !isLocalLogin }
            val username = viewModel.usernameProperty.value
            val masterPassword = viewModel.passwordProperty.value
            loginUser(serverUrl, username, masterPassword)
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