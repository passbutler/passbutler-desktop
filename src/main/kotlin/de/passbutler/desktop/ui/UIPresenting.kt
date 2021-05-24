package de.passbutler.desktop.ui

import de.passbutler.common.ui.BannerPresenting
import de.passbutler.common.ui.ProgressPresenting
import de.passbutler.common.ui.TransitionType
import javafx.scene.Node
import tornadofx.UIComponent
import kotlin.reflect.KClass

typealias Dialog = Node

interface ScreenPresenting : ScreenPresentingExtensions {
    fun <T : UIComponent> showScreen(screenClass: KClass<T>, parameters: Map<*, Any?>? = null, userTriggered: Boolean = true, transitionType: TransitionType = TransitionType.SLIDE)
    fun <T : UIComponent> isScreenShown(screenClass: KClass<T>): Boolean
    fun shownScreen(): UIComponent
}

interface DialogPresenting {
    fun showDialog(dialog: Dialog)
    fun dismissDialog()
}

interface UIPresenting : ScreenPresenting, ProgressPresenting, BannerPresenting, DialogPresenting
