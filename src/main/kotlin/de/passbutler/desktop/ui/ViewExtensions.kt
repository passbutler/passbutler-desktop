package de.passbutler.desktop.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSpinner
import com.jfoenix.controls.JFXToggleButton
import de.passbutler.common.ui.FADE_TRANSITION_DURATION
import javafx.animation.Animation
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.effect.DropShadow
import javafx.scene.effect.Effect
import javafx.scene.input.MouseButton
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.css
import tornadofx.fade
import tornadofx.label
import tornadofx.px

typealias JavaTimeDuration = java.time.Duration
typealias JavaFxDuration = javafx.util.Duration

fun EventTarget.jfxButtonBase(text: String = "", graphic: Node? = null, op: JFXButton.() -> Unit = {}) = JFXButton(text.toUpperCase()).attachTo(this, op) {
    if (graphic != null) it.graphic = graphic
}

fun EventTarget.jfxButtonRaised(text: String = "", graphic: Node? = null, op: JFXButton.() -> Unit = {}) = jfxButtonBase(text, graphic) {
    minWidth = 150.px.value
    buttonType = JFXButton.ButtonType.RAISED
    op.invoke(this)
}

fun EventTarget.jfxFloatingActionButtonRaised(text: String = "", graphic: Node? = null, op: JFXButton.() -> Unit = {}) = jfxButtonBase(text, graphic) {
    addClass(Theme.floatingActionButtonStyle)

    buttonType = JFXButton.ButtonType.RAISED
    shape = Circle(1.px.value)

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

fun EventTarget.textLabelWrapped(observable: ObservableValue<String>, op: Label.() -> Unit = {}) = label(observable) {
    isWrapText = true
    op.invoke(this)
}

fun EventTarget.textLabelHeadline1(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = textLabelWrapped(text, graphic) {
    addClass(Theme.textHeadline1Style)
    op.invoke(this)
}

fun EventTarget.textLabelHeadline1(observable: ObservableValue<String>, op: Label.() -> Unit = {}) = textLabelWrapped(observable) {
    addClass(Theme.textHeadline1Style)
    op.invoke(this)
}

fun EventTarget.textLabelBody1(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = textLabelWrapped(text, graphic) {
    addClass(Theme.textBody1Style)
    op.invoke(this)
}

fun EventTarget.textLabelBody1(observable: ObservableValue<String>, op: Label.() -> Unit = {}) = textLabelWrapped(observable) {
    addClass(Theme.textBody1Style)
    op.invoke(this)
}

fun EventTarget.textLabelBody2(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = textLabelWrapped(text, graphic) {
    addClass(Theme.textBody2Style)
    op.invoke(this)
}

private const val FADE_IN_OUT_ANIMATION_PROPERTY = "de.passbutler.desktop.ui.showFadeInOutAnimation"

fun Node.showFadeInOutAnimation(shouldShow: Boolean) {
    // Cancel previous animation if any is running to avoid produce out-of-sync view state
    (properties[FADE_IN_OUT_ANIMATION_PROPERTY] as? Animation)?.stop()

    if (shouldShow) {
        isVisible = true
        opacity = 0.0
        properties[FADE_IN_OUT_ANIMATION_PROPERTY] = fade(FADE_TRANSITION_DURATION.toJavaFxDuration(), 1.0).apply {
            onFinished = null
        }
    } else {
        opacity = 1.0
        properties[FADE_IN_OUT_ANIMATION_PROPERTY] = fade(FADE_TRANSITION_DURATION.toJavaFxDuration(), 0.0).apply {
            setOnFinished {
                isVisible = false
            }
        }
    }
}

fun JavaTimeDuration.toJavaFxDuration(): JavaFxDuration {
    return JavaFxDuration(toMillis().toDouble())
}

fun bottomDropShadow(): Effect {
    return dropShadow().apply {
        offsetY = 1.px.value
    }
}

fun endDropShadow(): Effect {
    return dropShadow().apply {
        offsetX = 1.px.value
    }
}

private fun dropShadow(): DropShadow {
    return DropShadow().apply {
        radius = 2.px.value
        offsetY = 1.px.value

        // No theming here because not styleable
        color = Color.web(grey90.css, 0.5)
    }
}

fun Node.onLeftClickIgnoringCount(action: () -> Unit) {
    setOnMouseClicked {
        if (it.button === MouseButton.PRIMARY) {
            action()
        }
    }
}
