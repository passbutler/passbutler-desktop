package de.sicherheitskritisch.view

import de.sicherheitskritisch.app.Styles
import tornadofx.*

class MainView : View("Passbutler") {
    override val root = hbox {
        label(title) {
            addClass(Styles.heading)
        }
    }
}