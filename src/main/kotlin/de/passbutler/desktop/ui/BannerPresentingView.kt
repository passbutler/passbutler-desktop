package de.passbutler.desktop.ui

import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import javafx.scene.layout.Pane

interface BannerPresentingView {
    var bannerView: JFXSnackbar?

    fun Pane.setupBannerView() {
        bannerView = jfxSnackbar(this)
    }

    fun showInformation(message: String) {
        bannerView?.enqueue(JFXSnackbar.SnackbarEvent(JFXSnackbarLayout(message)))
    }

    fun showError(message: String) {
        showInformation(message)
    }
}