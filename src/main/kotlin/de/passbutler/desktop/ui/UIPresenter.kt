package de.passbutler.desktop.ui

import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import de.passbutler.common.ui.DebouncedUIPresenting
import de.passbutler.common.ui.FADE_TRANSITION_DURATION
import de.passbutler.common.ui.SLIDE_TRANSITION_DURATION
import de.passbutler.common.ui.TransitionType
import de.passbutler.desktop.RootScreen
import javafx.util.Duration
import org.tinylog.kotlin.Logger
import tornadofx.*
import java.time.Instant
import kotlin.reflect.KClass

class UIPresenter(
    private val rootScreen: RootScreen
) : UIPresenting, DebouncedUIPresenting {

    override var lastViewTransactionTime: Instant? = null

    override fun <T : UIComponent> showScreen(screenClass: KClass<T>, debounce: Boolean, transitionType: TransitionType) {
        val debouncedViewTransactionEnsured = ensureDebouncedViewTransaction().takeIf { debounce } ?: true

        if (debouncedViewTransactionEnsured) {
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
        } else {
            Logger.warn("The view transaction was ignored because a recent transaction was already done!")
        }
    }

    override fun showProgress() {
        rootScreen.progressView?.showFadeInOutAnimation(true)
    }

    override fun hideProgress() {
        rootScreen.progressView?.showFadeInOutAnimation(false)
    }

    override fun showInformation(message: String) {
        rootScreen.bannerView?.enqueue(JFXSnackbar.SnackbarEvent(JFXSnackbarLayout(message)))
    }

    override fun showError(message: String) {
        // Same as information at the moment
        showInformation(message)
    }
}

private fun TransitionType.createViewTransition(): ViewTransition? {
    return when (this) {
        TransitionType.MODAL -> {
            // Not supported at the moment
            null
        }
        TransitionType.SLIDE -> ViewTransition.Slide(SLIDE_TRANSITION_DURATION.toJavaFxDuration())
        TransitionType.FADE -> ViewTransition.Fade(FADE_TRANSITION_DURATION.toJavaFxDuration())
        TransitionType.NONE -> null
    }
}