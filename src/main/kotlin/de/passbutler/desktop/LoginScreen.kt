package de.passbutler.desktop

import de.passbutler.desktop.ui.*
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import javafx.scene.paint.Color
import tornadofx.*
import java.net.URI

class LoginScreen : View("Login") {

    private var light = true

    override val root = hbox(alignment = Pos.CENTER) {
        style {
            backgroundImage += URI("/drawables/background.jpg")
            backgroundSize += BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, false, true)
            backgroundRepeat += Pair(BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT)
        }

        onLeftClick {
            requestFocus()
        }

        group {
            form {

                style {
                    // TODO: Use theme attribute
                    backgroundColor += Color.web(whiteMedium.css, 0.40)
                    backgroundRadius = multi(box(4.dp))
                    paddingAll = marginM.value
                    minWidth = 180.pt
                }

                text(messages["login_description"])

                fieldset(labelPosition = Orientation.VERTICAL) {
                    paddingTop = marginM.value
                    paddingBottom = marginM.value

                    spacing = marginS.value

                    field(messages["login_username_hint"]) {
                        textfield() {
                            whenDocked {
                                requestFocus()
                            }
                        }
                    }
                    field(messages["login_master_password_hint"]) {
                        passwordfield() {
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