package de.passbutler.desktop

import com.jfoenix.controls.JFXSnackbar
import de.passbutler.common.database.RequestUnauthorizedException
import de.passbutler.desktop.base.RequestSending
import de.passbutler.desktop.base.launchRequestSending
import de.passbutler.desktop.ui.*
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Button
import tornadofx.*
import tornadofx.FX.Companion.messages

class LoginScreen : CoroutineScopedView(messages["login_title"]), RequestSending {

    override val root = stackpane()
    override var progressView: Node? = null
    override var bannerView: JFXSnackbar? = null

    private val viewModel: LoginViewModel by inject()

    private var serverUrlField: Field? = null

    init {
        with(root) {
            setupContentView()
            setupBannerView()
            setupProgressView()
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
                    paddingAll = marginM.value
                    minWidth = 180.dp
                }

                label(messages["login_description"])

                fieldset(labelPosition = Orientation.VERTICAL) {
                    paddingTop = marginM.value
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
            textfield(viewModel.serverUrlProperty) {
                whenDocked {
                    requestFocus()
                }

                validator {
                    if (!viewModel.isLocalLoginProperty.value && it.isNullOrBlank()) {
                        error(viewModelBundle["required"])
                    } else {
                        null
                    }
                }
            }
        }
    }

    private fun Fieldset.createUsernameUrlField(): Field {
        return field(messages["login_username_hint"]) {
            textfield(viewModel.usernameProperty) {
                required()
            }
        }
    }

    private fun Fieldset.createPasswordUrlField(): Field {
        return field(messages["login_master_password_hint"]) {
            passwordfield(viewModel.passwordProperty) {
                required()
            }
        }
    }

    private fun Fieldset.createLocalLoginCheckbox(): Field {
        return field {
            checkbox(messages["login_local_login_label"], viewModel.isLocalLoginProperty) {
                action {
                    serverUrlField?.isVisible = !viewModel.isLocalLoginProperty.value
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