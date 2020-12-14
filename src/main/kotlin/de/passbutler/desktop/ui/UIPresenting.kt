package de.passbutler.desktop.ui

import de.passbutler.common.ui.BannerPresenting
import de.passbutler.common.ui.ProgressPresenting
import de.passbutler.common.ui.TransitionType
import tornadofx.UIComponent
import kotlin.reflect.KClass

interface ScreenPresenting {
    fun <T : UIComponent> showScreen(screenClass: KClass<T>, parameters: Map<*, Any?>? = null, userTriggered: Boolean = true, transitionType: TransitionType = TransitionType.SLIDE)
    fun <T : UIComponent> isScreenShown(screenClass: KClass<T>): Boolean
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

interface UIPresenting : ScreenPresenting, ProgressPresenting, BannerPresenting
