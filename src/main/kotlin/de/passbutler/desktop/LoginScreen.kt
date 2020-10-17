package de.passbutler.desktop

import com.jfoenix.controls.JFXButton
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
        importStylesheet(LightTheme::class)

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
                    backgroundColor += Color.web(whiteMedium.css, 0.40)
                    backgroundRadius = multi(box(4.pt))
                    paddingAll = marginM.value
                }

                text("Melden Sie sich bei Pass Butler an")

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

                // TODO: Use extention
                this += JFXButton(messages["login_button_text"].toUpperCase()).apply {
                    useMaxWidth = true
                    isDefaultButton = true
                    buttonType = JFXButton.ButtonType.RAISED

                    action {
                        println("Login pressed light = $light")

                        if (light) {
                            removeStylesheet(LightTheme::class)
                            importStylesheet(DarkTheme::class)
                        } else {
                            removeStylesheet(DarkTheme::class)
                            importStylesheet(LightTheme::class)
                        }

                        light = !light
                    }
                }
            }
        }
    }
}