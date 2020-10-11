package de.passbutler.desktop

import com.jfoenix.controls.JFXButton
import de.passbutler.desktop.ui.DarkTheme
import de.passbutler.desktop.ui.LightTheme
import org.tinylog.kotlin.Logger
import tornadofx.*

class LoginScreen : View("Login") {

    private var light = true


    override val root = form {
        importStylesheet(LightTheme::class)

        fieldset {
            field("Username") {
                textfield("") {
//                    required()
                    textProperty().addListener { obs, old, new ->
                        println("You typed: " + new)
                    }

                    whenDocked { requestFocus() }
                }
            }
            field("Password") {
                passwordfield("") {
//                    required()
                }
            }
            field("Remember me") {
                checkbox()
            }
        }

        this += JFXButton("Anmelden".toUpperCase()).apply {
            isDefaultButton = true
            buttonType = JFXButton.ButtonType.RAISED

            action {
                Logger.debug("Login pressed")
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
//        button("Login") {
//            isDefaultButton = true
//
//            action {
//                Logger.debug("Login pressed")
//                importStylesheet(LightTheme::class)
//
//            }
//        }
    }
}
