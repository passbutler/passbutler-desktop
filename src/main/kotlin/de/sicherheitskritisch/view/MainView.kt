package de.sicherheitskritisch.view

import de.passbutler.common.crypto.RandomGenerator
import de.sicherheitskritisch.app.Styles
import kotlinx.coroutines.runBlocking
import tornadofx.*

class MainView : View("Pass Butler") {
    override val root = hbox {

        val d = runBlocking {
            RandomGenerator.generateRandomBytes(1).first().toString()
        }

        label(title + d) {
            addClass(Styles.heading)
        }
    }
}