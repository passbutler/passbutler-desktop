package de.passbutler.desktop.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSpinner
import com.jfoenix.controls.JFXToggleButton
import de.passbutler.common.ui.FADE_TRANSITION_DURATION
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.effect.DropShadow
import javafx.scene.effect.Effect
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.css
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
fun EventTarget.jfxToggleButton(text: String, op: JFXToggleButton.() -> Unit = {}) = JFXToggleButton().attachTo(this) {
    this.text = text
    op.invoke(this)
}

fun EventTarget.textLabelWrapped(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = label(text, graphic) {
    isWrapText = true
    op.invoke(this)
}

fun EventTarget.textLabelHeadline(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = textLabelWrapped(text, graphic) {
    addClass(Theme.textHeadline1Style)
    op.invoke(this)
}

fun EventTarget.textLabelBody1(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = textLabelWrapped(text, graphic) {
    addClass(Theme.textBody1Style)
    op.invoke(this)
}

fun EventTarget.textLabelBody2(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = textLabelWrapped(text, graphic) {
    addClass(Theme.textBody2Style)
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

fun dropShadow(): Effect {
    return DropShadow().apply {
        radius = 2.0
        offsetY = 1.0

        // TODO: Better use theme attribute
        color = Color.web(greyDark.css, 0.5)
    }
}