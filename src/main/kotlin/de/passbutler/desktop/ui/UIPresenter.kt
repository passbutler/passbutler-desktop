package de.passbutler.desktop.ui

import de.passbutler.common.ui.BannerPresenting
import de.passbutler.common.ui.DebouncedUIPresenting
import de.passbutler.common.ui.FADE_TRANSITION_DURATION
import de.passbutler.common.ui.ProgressPresenting
import de.passbutler.common.ui.SLIDE_TRANSITION_DURATION
import de.passbutler.common.ui.TransitionType
import de.passbutler.desktop.RootScreen
import javafx.scene.Node
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
) : UIPresenting, DebouncedUIPresenting,
    ProgressPresenting by ProgressPresenter(rootScreen.progressView),
    BannerPresenting by BannerPresenter(rootScreen.bannerView) {

    override var lastViewTransactionTime: Instant? = null

    private var shownScreenClass: KClass<out UIComponent>? = null

    override fun <T : UIComponent> showScreen(screenClass: KClass<T>, parameters: Map<*, Any?>?, userTriggered: Boolean, transitionType: TransitionType) {
        val debouncedViewTransactionEnsured = ensureDebouncedViewTransaction().takeIf { userTriggered } ?: true

        if (debouncedViewTransactionEnsured) {
            rootScreen.contentContainer.getChildList()?.apply {
                val screenInstance = find(screenClass, params = parameters)
                rootScreen.titleProperty.bind(screenInstance.titleProperty)

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

    override fun <T : UIComponent> isScreenShown(screenClass: KClass<T>): Boolean {
        return shownScreenClass == screenClass
    }
}

class ProgressPresenter(private val progressView: Node) : ProgressPresenting {
    override fun showProgress() {
        progressView.showFadeInOutAnimation(true)
    }

    override fun hideProgress() {
        progressView.showFadeInOutAnimation(false)
    }
}

class BannerPresenter(private val bannerView: BannerView) : BannerPresenting {
    override fun showInformation(message: String) {
        bannerView.show(message)
    }

    override fun showError(message: String) {
        // Same as information at the moment
        showInformation(message)
    }
}
