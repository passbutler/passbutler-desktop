package de.passbutler.desktop.ui

import de.passbutler.common.ui.FADE_TRANSITION_DURATION
import de.passbutler.common.ui.SLIDE_TRANSITION_DURATION
import de.passbutler.common.ui.TransitionType
import tornadofx.UIComponent
import tornadofx.ViewTransition
import kotlin.reflect.KClass

interface ScreenPresentingExtensions {
    fun TransitionType.createViewTransition(): ViewTransition? {
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
}

fun <T : UIComponent> ScreenPresenting.showScreenUnanimated(screenClass: KClass<T>, parameters: Map<*, Any?>? = null, userTriggered: Boolean = true) {
    showScreen(screenClass, parameters = parameters, transitionType = TransitionType.NONE, userTriggered = userTriggered)
}

fun <T : UIComponent> ScreenPresenting.showScreenFaded(screenClass: KClass<T>, parameters: Map<*, Any?>? = null, userTriggered: Boolean = true) {
    showScreen(screenClass, parameters = parameters, transitionType = TransitionType.FADE, userTriggered = userTriggered)
}

fun <T : UIComponent> ScreenPresenting.showScreenModally(screenClass: KClass<T>, parameters: Map<*, Any?>? = null, userTriggered: Boolean = true) {
    showScreen(screenClass, parameters = parameters, transitionType = TransitionType.MODAL, userTriggered = userTriggered)
}
