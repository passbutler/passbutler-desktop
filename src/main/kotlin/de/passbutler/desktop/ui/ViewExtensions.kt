package de.passbutler.desktop.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSpinner
import de.passbutler.common.ui.FADE_TRANSITION_DURATION
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import tornadofx.attachTo
import tornadofx.fade
import tornadofx.label
import tornadofx.useMaxWidth

typealias JavaTimeDuration = java.time.Duration
typealias JavaFxDuration = javafx.util.Duration

fun EventTarget.jfxButtonBase(text: String = "", graphic: Node? = null, op: JFXButton.() -> Unit = {}) = JFXButton(text.toUpperCase()).attachTo(this, {
    useMaxWidth = true
    op.invoke(this)
}) {
    if (graphic != null) it.graphic = graphic
}

fun EventTarget.jfxButtonRaised(text: String = "", graphic: Node? = null, op: JFXButton.() -> Unit = {}) = jfxButtonBase(text, graphic) {
    buttonType = JFXButton.ButtonType.RAISED
    op.invoke(this)
}

fun EventTarget.jfxSpinner(op: JFXSpinner.() -> Unit = {}) = JFXSpinner().attachTo(this, op)
fun EventTarget.jfxSnackbar(snackbarContainer: Pane, op: JFXSnackbar.() -> Unit = {}) = JFXSnackbar(snackbarContainer).attachTo(this, op)

fun EventTarget.textLabelWrapped(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = label(text, graphic) {
    isWrapText = true
    op.invoke(this)
}

fun Node.showFadeInOutAnimation(shouldShow: Boolean) {
    // TODO: Cancel previous animation if any is running to avoid produce out-of-sync view state
    if (shouldShow) {
        isVisible = true
        opacity = 0.0
        fade(FADE_TRANSITION_DURATION.toJavaFxDuration(), 1.0).onFinished = null
    } else {
        opacity = 1.0
        fade(FADE_TRANSITION_DURATION.toJavaFxDuration(), 0.0).setOnFinished {
            isVisible = false
        }
    }
}

fun JavaTimeDuration.toJavaFxDuration(): JavaFxDuration {
    return JavaFxDuration(toMillis().toDouble())
}