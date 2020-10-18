package de.passbutler.desktop.ui

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.StackPane
import tornadofx.addClass
import tornadofx.hbox

interface ProgressPresentingView {
    var progressView: Node?

    fun StackPane.setupProgressView() {
        progressView = hbox(alignment = Pos.CENTER) {
            addClass(BaseTheme.scrimForegroundStyle)

            jfxSpinner()
            isVisible = false
        }
    }

    fun showProgress() {
        progressView?.isVisible = true
    }

    fun hideProgress() {
        progressView?.isVisible = false
    }
}