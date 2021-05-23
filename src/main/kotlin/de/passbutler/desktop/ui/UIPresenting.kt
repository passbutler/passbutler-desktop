package de.passbutler.desktop.ui

import de.passbutler.common.ui.BannerPresenting
import de.passbutler.common.ui.ProgressPresenting
import de.passbutler.common.ui.TransitionType
import tornadofx.UIComponent
import kotlin.reflect.KClass

interface ScreenPresenting : ScreenPresentingExtensions {
    fun <T : UIComponent> showScreen(screenClass: KClass<T>, parameters: Map<*, Any?>? = null, userTriggered: Boolean = true, transitionType: TransitionType = TransitionType.SLIDE)
    fun <T : UIComponent> isScreenShown(screenClass: KClass<T>): Boolean
}

interface UIPresenting : ScreenPresenting, ProgressPresenting, BannerPresenting
