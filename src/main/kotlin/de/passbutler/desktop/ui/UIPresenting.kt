package de.passbutler.desktop.ui

import de.passbutler.common.ui.BannerPresenting
import de.passbutler.common.ui.ProgressPresenting
import de.passbutler.common.ui.TransitionType
import tornadofx.UIComponent
import kotlin.reflect.KClass

interface UIPresenting : ProgressPresenting, BannerPresenting {
    fun <T : UIComponent> showScreen(screenClass: KClass<T>, userTriggered: Boolean = true, transitionType: TransitionType = TransitionType.SLIDE)
    fun <T : UIComponent> isScreenShown(screenClass: KClass<T>): Boolean
}

fun <T : UIComponent> UIPresenting.showScreenUnanimated(screenClass: KClass<T>) {
    showScreen(screenClass, transitionType = TransitionType.NONE)
}

fun <T : UIComponent> UIPresenting.showScreenFaded(screenClass: KClass<T>) {
    showScreen(screenClass, transitionType = TransitionType.FADE)
}

fun <T : UIComponent> UIPresenting.showScreenModally(screenClass: KClass<T>) {
    showScreen(screenClass, transitionType = TransitionType.MODAL)
}