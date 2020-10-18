package de.passbutler.desktop

import de.passbutler.desktop.ui.*
import javafx.geometry.Orientation
import javafx.geometry.Pos
import tornadofx.*

class LoginScreen : View("Login") {

    private var light = true

    override val root = hbox(alignment = Pos.CENTER) {
        addClass(BaseTheme.abstractBackgroundStyle)

        onLeftClick {
            requestFocus()
        }

        group {
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

                    field(messages["login_username_hint"]) {
                        textfield {
                            whenDocked {
                                requestFocus()
                            }
                        }
                    }
                    field(messages["login_master_password_hint"]) {
                        passwordfield {
                        }
                    }
                    field {
                        checkbox(messages["login_local_login_label"])
                    }
                }

                jfxButtonRaised(messages["login_button_text"]) {
                    isDefaultButton = true

                    action {
                        println("Login pressed light = $light")

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
    }
}