package de.passbutler.desktop

import de.passbutler.common.base.BuildType
import de.passbutler.common.database.RequestUnauthorizedException
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.base.BuildInformationProvider
import de.passbutler.desktop.base.isHttpsUrl
import de.passbutler.desktop.base.isNetworkUrl
import de.passbutler.desktop.ui.BaseFragment
import de.passbutler.desktop.ui.BaseTheme
import de.passbutler.desktop.ui.FormFieldValidatorRule
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.showFadeInOutAnimation
import de.passbutler.desktop.ui.textLabelWrapped
import de.passbutler.desktop.ui.validatorWithRules
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.text.TextAlignment
import tornadofx.FX.Companion.messages
import tornadofx.Field
import tornadofx.Fieldset
import tornadofx.Form
import tornadofx.action
import tornadofx.addClass
import tornadofx.checkbox
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.group
import tornadofx.imageview
import tornadofx.onLeftClick
import tornadofx.paddingAll
import tornadofx.paddingBottom
import tornadofx.paddingTop
import tornadofx.pane
import tornadofx.passwordfield
import tornadofx.px
import tornadofx.stackpane
import tornadofx.style
import tornadofx.textfield
import tornadofx.whenDocked

class LoginScreen : BaseFragment(messages["login_title"]), RequestSending {

    override val root = stackpane()

    private val viewModel by injectWithPrivateScope<LoginViewModel>()

    private var serverUrlField: Field? = null

    init {
        with(root) {
            setupContentView()
        }
    }

    private fun Node.setupContentView() {
        stackpane {
            pane {
                addClass(BaseTheme.abstractBackgroundStyle)
            }

            pane {
                addClass(BaseTheme.abstractBackgroundImageStyle)
            }

            onLeftClick {
                requestFocus()
            }

            setupCardViewContent()
        }
    }

    private fun Node.setupCardViewContent() {
        group {
            form {
                addClass(BaseTheme.cardViewBackgroundStyle)

                style {
                    alignment = Pos.CENTER
                    paddingAll = marginM.value
                    prefWidth = 320.px
                }

                imageview(Image("/drawables/logo_elevated.png", 120.px.value, 0.0, true, true))

                textLabelWrapped(messages["login_headline"]) {
                    addClass(BaseTheme.textHeadline1Style)

                    paddingTop = marginM.value
                    textAlignment = TextAlignment.CENTER
                }

                textLabelWrapped(messages["login_description"]) {
                    addClass(BaseTheme.textBody1Style)

                    paddingTop = marginS.value
                    textAlignment = TextAlignment.CENTER
                }

                fieldset(labelPosition = Orientation.VERTICAL) {
                    paddingTop = marginS.value
                    paddingBottom = marginM.value

                    spacing = marginS.value

                    serverUrlField = createServerUrlField()

                    createUsernameUrlField()
                    createPasswordUrlField()
                    createLocalLoginCheckbox()
                }

                createLoginButton()
            }
        }
    }

    private fun Fieldset.createServerUrlField(): Field {
        return field(messages["login_serverurl_hint"]) {
            isVisible = !viewModel.isLocalLoginProperty.value

            textfield(viewModel.serverUrlProperty) {
                validatorWithRules {
                    listOfNotNull(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["form_serverurl_validation_error_empty"]),
                        FormFieldValidatorRule({ !isNetworkUrl(it) }, messages["form_serverurl_validation_error_invalid"]),
                        FormFieldValidatorRule({ !isHttpsUrl(it) }, messages["form_serverurl_validation_error_invalid_scheme"]).takeIf { BuildInformationProvider.buildType == BuildType.Release }
                    ).takeIf { !viewModel.isLocalLoginProperty.value }
                }
            }
        }
    }

    private fun Fieldset.createUsernameUrlField(): Field {
        return field(messages["login_username_hint"]) {
            textfield(viewModel.usernameProperty) {
                whenDocked {
                    requestFocus()
                }

                validatorWithRules {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["login_username_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Fieldset.createPasswordUrlField(): Field {
        return field(messages["login_master_password_hint"]) {
            passwordfield(viewModel.passwordProperty) {
                validatorWithRules {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["form_master_password_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Fieldset.createLocalLoginCheckbox(): Field {
        return field {
            checkbox(messages["login_local_login_label"], viewModel.isLocalLoginProperty) {
                action {
                    val shouldShow = !viewModel.isLocalLoginProperty.value
                    serverUrlField?.showFadeInOutAnimation(shouldShow)
                }
            }
        }
    }

    private fun Form.createLoginButton(): Button {
        return jfxButtonRaised(messages["login_button_text"]) {
            isDefaultButton = true

            action {
                loginClicked()
            }
        }
    }

    private fun loginClicked() {
        viewModel.validate()

        if (viewModel.valid.value) {
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