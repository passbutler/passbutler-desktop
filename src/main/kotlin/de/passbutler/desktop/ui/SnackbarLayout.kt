package de.passbutler.desktop.ui

import javafx.scene.control.Label
import tornadofx.addClass

class SnackbarLayout(message: String) : Label(message) {
    init {
        addClass(Theme.snackbarLayoutStyle)
    }
}
