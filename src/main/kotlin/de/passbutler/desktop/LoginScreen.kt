package de.passbutler.desktop

import de.passbutler.desktop.ui.*
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.Button
import tornadofx.*

class LoginScreen : View("Login") {

    override val root = hbox(alignment = Pos.CENTER)

    private val viewModel: LoginViewModel by inject()

    private var serverUrlField: Field? = null

    private var light = true

    init {
        with(root) {
            setupForegroundContainer()
        }
    }

    private fun Node.setupForegroundContainer(): Group {
        addClass(BaseTheme.abstractBackgroundStyle)

        onLeftClick {
            requestFocus()
        }

        return group {
            setupForm()
        }
    }

    private fun Node.setupForm() {
        form {
            addClass(BaseTheme.cardViewBackgroundStyle)

            style {
                paddingAll = marginM.value
                minWidth = 180.pt
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
                viewModel.validate()

                if (viewModel.valid.value) {
                    // TODO: Start request sending with viewModel.loginUser()
                }

                if (light) {
                    ThemeManager.changeTheme(Theme.DARK)
                } else {
                    ThemeManager.changeTheme(Theme.LIGHT)
                }

                light = !light
            }
        }
    }
}