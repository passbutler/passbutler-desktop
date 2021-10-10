package de.passbutler.desktop.ui

import tornadofx.Component

fun Component.openBrowser(url: String) {
    hostServices.showDocument(url)
}
