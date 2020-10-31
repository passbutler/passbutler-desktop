package de.passbutler.desktop.ui

import de.passbutler.common.ui.BannerPresenting
import de.passbutler.common.ui.ProgressPresenting
import de.passbutler.common.ui.TransitionType
import tornadofx.UIComponent
import kotlin.reflect.KClass

interface UIPresenting : ProgressPresenting, BannerPresenting {
    fun <T : UIComponent> showScreen(screenClass: KClass<T>, debounce: Boolean = true, transitionType: TransitionType = TransitionType.SLIDE)
}