package de.passbutler.desktop.ui

import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import de.passbutler.desktop.RootScreen
import javafx.util.Duration
import tornadofx.*
import java.time.temporal.ChronoUnit
import kotlin.reflect.KClass

typealias JavaDuration = java.time.Duration

class UIPresenter(
    private val rootScreen: RootScreen
) : UIPresenting {

    private var lastViewTransactionTimestamp: Long = 0

    override fun <T : UIComponent> showScreen(screenClass: KClass<T>, debounce: Boolean, transitionType: TransitionType) {
        val noRecentTransactionWasDone = checkNoRecentViewTransactionWasDone().takeIf { debounce } ?: true

        if (noRecentTransactionWasDone) {
            rootScreen.contentContainer?.getChildList()?.apply {
                val screenInstance = find(screenClass)
                rootScreen.title = screenInstance.title

                if (screenInstance is BaseUIComponent) {
                    screenInstance.transitionType = transitionType
                    screenInstance.uiPresentingDelegate = this@UIPresenter
                }

                val existingScreen = lastOrNull()

                if (existingScreen != null) {
                    existingScreen.replaceWith(screenInstance.root, transitionType.createViewTransition())
                } else {
                    add(screenInstance.root)
                }
            }
        }
    }

    private fun checkNoRecentViewTransactionWasDone(): Boolean {
        val currentTimestamp = System.currentTimeMillis()
        val lastViewTransactionTimestampDelta = currentTimestamp - lastViewTransactionTimestamp
        val noRecentViewTransactionWasDone = lastViewTransactionTimestampDelta > VIEW_TRANSACTION_DEBOUNCE_TIME.toMillis()

        // If no recent show fragment transaction was done, set current timestamp
        if (noRecentViewTransactionWasDone) {
            lastViewTransactionTimestamp = currentTimestamp
        }

        return noRecentViewTransactionWasDone
    }

    override fun showProgress() {
        rootScreen.progressView?.apply {
            isVisible = true
            opacity = 0.0
            fade(Duration(350.0), 1.0).onFinished = null
        }
    }

    override fun hideProgress() {
        rootScreen.progressView?.apply {
            opacity = 1.0
            fade(Duration(350.0), 0).setOnFinished {
                isVisible = false
            }
        }
    }

    override fun showInformation(message: String) {
        rootScreen.bannerView?.enqueue(JFXSnackbar.SnackbarEvent(JFXSnackbarLayout(message)))
    }

    override fun showError(message: String) {
        // Same as information at the moment
        showInformation(message)
    }

    companion object {
        private val VIEW_TRANSACTION_DEBOUNCE_TIME = JavaDuration.of(450, ChronoUnit.MILLIS)
    }
}

fun TransitionType.createViewTransition(): ViewTransition? {
    return when (this) {
        TransitionType.SLIDE -> ViewTransition.Slide(Duration(500.0))
        TransitionType.FADE -> ViewTransition.Fade(Duration(500.0))
        TransitionType.NONE -> null
    }
}