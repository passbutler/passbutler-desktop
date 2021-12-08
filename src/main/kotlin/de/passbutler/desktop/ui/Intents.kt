package de.passbutler.desktop.ui

import tornadofx.Component

fun Component.openBrowser(url: String) {
    hostServices.showDocument(url)
}

fun Component.openWriteEmail(email: String) {
    hostServices.showDocument("mailto:$email")
}
