package de.passbutler.desktop.ui

import com.jfoenix.controls.JFXSnackbar
import javafx.event.EventTarget
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import tornadofx.addClass
import tornadofx.attachTo

interface BannerView {
    fun show(message: String)
}

class Snackbar(snackbarContainer: Pane) : JFXSnackbar(snackbarContainer), BannerView {
    override fun show(message: String) {
        enqueue(SnackbarEvent(Layout(message)))
    }

    class Layout(message: String) : Label(message) {
        init {
            addClass(Theme.snackbarLayoutStyle)
        }
    }
}

fun EventTarget.snackbarBannerView(snackbarContainer: Pane, op: JFXSnackbar.() -> Unit = {}) = Snackbar(snackbarContainer).attachTo(this, op)
