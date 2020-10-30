package de.passbutler.desktop.ui

import tornadofx.UIComponent
import kotlin.reflect.KClass

interface ProgressPresenting {
    fun showProgress()
    fun hideProgress()
}

interface BannerPresenting {
    fun showInformation(message: String)
    fun showError(message: String)
}

interface UIPresenting : ProgressPresenting, BannerPresenting {
    fun <T : UIComponent> showScreen(screenClass: KClass<T>, debounce: Boolean = true, transitionType: TransitionType = TransitionType.SLIDE)
}

enum class TransitionType {
    SLIDE,
    FADE,
    NONE
}