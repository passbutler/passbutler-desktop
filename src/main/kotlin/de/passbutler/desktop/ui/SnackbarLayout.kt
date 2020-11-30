package de.passbutler.desktop.ui

import javafx.scene.control.Label
import tornadofx.addClass
import tornadofx.addStylesheet
import tornadofx.insets

class SnackbarLayout(message: String) : Label(message) {
    init {
        // TODO: Use inverted style
        // Enforce dark theme to have always dark background
        addStylesheet(DarkTheme::class)

        addClass(Theme.backgroundStyle)

        padding = insets(marginM.value, marginS.value)
    }
}
