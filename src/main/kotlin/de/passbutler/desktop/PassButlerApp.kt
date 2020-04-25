package de.passbutler.desktop

import de.passbutler.desktop.ui.Styles
import javafx.stage.Stage
import tornadofx.*

class PassButlerApp : App(LoginScreen::class, Styles::class) {
    val loginController: LoginController by inject()

    override fun start(stage: Stage) {
        super.start(stage)
        loginController.init()
    }
}

fun main(args: Array<String>) {
    launch<PassButlerApp>(args)
}