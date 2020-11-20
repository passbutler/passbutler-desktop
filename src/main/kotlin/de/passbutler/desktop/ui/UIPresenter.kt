package de.passbutler.desktop.ui

import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import de.passbutler.common.ui.DebouncedUIPresenting
import de.passbutler.common.ui.FADE_TRANSITION_DURATION
import de.passbutler.common.ui.SLIDE_TRANSITION_DURATION
import de.passbutler.common.ui.TransitionType
import de.passbutler.desktop.RootScreen
import org.tinylog.kotlin.Logger
import tornadofx.UIComponent
import tornadofx.ViewTransition
import tornadofx.find
import tornadofx.getChildList
import tornadofx.replaceWith
import java.time.Instant
import kotlin.reflect.KClass

class UIPresenter(
    private val rootScreen: RootScreen
) : UIPresenting, DebouncedUIPresenting {

    override var lastViewTransactionTime: Instant? = null

    private var shownScreenClass: KClass<out UIComponent>? = null

    override fun <T : UIComponent> showScreen(screenClass: KClass<T>, userTriggered: Boolean, transitionType: TransitionType) {
        val debouncedViewTransactionEnsured = ensureDebouncedViewTransaction().takeIf { userTriggered } ?: true

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

                shownScreenClass = screenClass
            }
        } else {
            Logger.warn("The view transaction was ignored because a recent transaction was already done!")
        }
    }

    override fun <T : UIComponent> isScreenShown(screenClass: KClass<T>): Boolean {
        return shownScreenClass == screenClass
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