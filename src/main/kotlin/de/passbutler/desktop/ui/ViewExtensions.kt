package de.passbutler.desktop.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXSlider
import com.jfoenix.controls.JFXSpinner
import com.jfoenix.controls.JFXToggleButton
import de.passbutler.common.ui.FADE_TRANSITION_DURATION
import javafx.animation.Animation
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.ToggleButton
import javafx.scene.effect.DropShadow
import javafx.scene.effect.Effect
import javafx.scene.input.MouseButton
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import tornadofx.FX
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.css
import tornadofx.fade
import tornadofx.label
import tornadofx.pane
import tornadofx.px
import tornadofx.scrollpane
import tornadofx.stackpane

typealias JavaTimeDuration = java.time.Duration
typealias JavaFxDuration = javafx.util.Duration

/**
 * General
 */

var Node.isEnabled: Boolean
    get() = !isDisable
    set(value) {
        isDisable = !value
    }

fun Node.showFadeInOutAnimation(shouldShow: Boolean, finishedCallback: (() -> Unit)? = null) {
    val animationProperty = "de.passbutler.desktop.ui.showFadeInOutAnimation"

    // Cancel previous animation if any is running to avoid produce out-of-sync view state
    (properties[animationProperty] as? Animation)?.stop()

    if (shouldShow) {
        isVisible = true
        opacity = 0.0
        properties[animationProperty] = fade(FADE_TRANSITION_DURATION.toJavaFxDuration(), 1.0).apply {
            setOnFinished {
                finishedCallback?.invoke()
            }
        }
    } else {
        opacity = 1.0
        properties[animationProperty] = fade(FADE_TRANSITION_DURATION.toJavaFxDuration(), 0.0).apply {
            setOnFinished {
                isVisible = false
                finishedCallback?.invoke()
            }
        }
    }
}

fun JavaTimeDuration.toJavaFxDuration(): JavaFxDuration {
    return JavaFxDuration(toMillis().toDouble())
}

/**
 * Checkbox
 */

fun EventTarget.jfxCheckBox(text: String = "", op: JFXCheckBox.() -> Unit = {}) = JFXCheckBox(text).attachTo(this, op)

/**
 * Buttons
 */

fun EventTarget.jfxButton(text: String = "", op: JFXButton.() -> Unit = {}) = JFXButton().attachTo(this) {
    textProperty().addListener { _, _, newValue ->
        this.text = newValue.uppercase(FX.locale)
    }

    // Initially trigger text listener above
    this.text = text

    op.invoke(this)
}

fun EventTarget.jfxFloatingActionButton(text: String = "", op: JFXButton.() -> Unit = {}) = jfxButton(text) {
    addClass(Theme.buttonFloatingActionStyle)

    shape = Circle(1.px.value)

    op.invoke(this)
}

fun EventTarget.jfxToggleButton(text: String? = null, op: JFXToggleButton.() -> Unit = {}) = JFXToggleButton().attachTo(this) {
    this.text = text
    op.invoke(this)
}

fun ToggleButton.toggle() {
    isSelected = !isSelected
}

/**
 * Slider
 */

fun EventTarget.jfxSlider(min: Double, max: Double, value: Double, op: JFXSlider.() -> Unit = {}) = JFXSlider(min, max, value).attachTo(this, op)

fun EventTarget.jfxSlider(range: IntRange, value: Int, op: JFXSlider.() -> Unit = {}) = jfxSlider(range.first.toDouble(), range.last.toDouble(), value.toDouble(), op)

/**
 * Spinner
 */

fun EventTarget.jfxSpinner(op: JFXSpinner.() -> Unit = {}) = JFXSpinner().attachTo(this, op)

/**
 * Text labels
 */

fun EventTarget.textLabelBase(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = label(text, graphic) {
    isWrapText = true
    op.invoke(this)
}

fun EventTarget.textLabelBase(observable: ObservableValue<String>, op: Label.() -> Unit = {}) = label(observable) {
    isWrapText = true
    op.invoke(this)
}

fun EventTarget.textLabelHeadlineOrder1(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = textLabelBase(text, graphic) {
    addClass(Theme.textHeadline5Style)
    op.invoke(this)
}

fun EventTarget.textLabelHeadlineOrder1(observable: ObservableValue<String>, op: Label.() -> Unit = {}) = textLabelBase(observable) {
    addClass(Theme.textHeadline5Style)
    op.invoke(this)
}

fun EventTarget.textLabelHeadlineOrder2(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = textLabelBase(text, graphic) {
    addClass(Theme.textHeadline6Style)
    op.invoke(this)
}

fun EventTarget.textLabelHeadlineOrder2(observable: ObservableValue<String>, op: Label.() -> Unit = {}) = textLabelBase(observable) {
    addClass(Theme.textHeadline6Style)
    op.invoke(this)
}

fun EventTarget.textLabelSubtitleOrder1(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = textLabelBase(text, graphic) {
    addClass(Theme.textSubtitle1Style)
    op.invoke(this)
}

fun EventTarget.textLabelSubtitleOrder1(observable: ObservableValue<String>, op: Label.() -> Unit = {}) = textLabelBase(observable) {
    addClass(Theme.textSubtitle1Style)
    op.invoke(this)
}

fun EventTarget.textLabelSubtitleOrder2(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = textLabelBase(text, graphic) {
    addClass(Theme.textSubtitle2Style)
    op.invoke(this)
}

fun EventTarget.textLabelSubtitleOrder2(observable: ObservableValue<String>, op: Label.() -> Unit = {}) = textLabelBase(observable) {
    addClass(Theme.textSubtitle2Style)
    op.invoke(this)
}

fun EventTarget.textLabelBodyOrder1(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = textLabelBase(text, graphic) {
    addClass(Theme.textBody1Style)
    op.invoke(this)
}

fun EventTarget.textLabelBodyOrder1(observable: ObservableValue<String>, op: Label.() -> Unit = {}) = textLabelBase(observable) {
    addClass(Theme.textBody1Style)
    op.invoke(this)
}

fun EventTarget.textLabelBodyOrder2(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = textLabelBase(text, graphic) {
    addClass(Theme.textBody2Style)
    op.invoke(this)
}

fun EventTarget.textLabelBodyOrder2(observable: ObservableValue<String>, op: Label.() -> Unit = {}) = textLabelBase(observable) {
    addClass(Theme.textBody2Style)
    op.invoke(this)
}

fun EventTarget.textLabelCaption(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = textLabelBase(text, graphic) {
    addClass(Theme.textCaptionStyle)
    op.invoke(this)
}

fun EventTarget.textLabelCaption(observable: ObservableValue<String>, op: Label.() -> Unit = {}) = textLabelBase(observable) {
    addClass(Theme.textCaptionStyle)
    op.invoke(this)
}

/**
 * Shadows
 */

fun Node.paneWithDropShadow(dropShadowEffect: Effect, op: Pane.() -> Unit) {
    stackpane {
        pane {
            addClass(Theme.backgroundStyle)
            effect = dropShadowEffect
        }

        op(this)
    }
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

/**
 * Input events
 */

fun Node.onLeftClickIgnoringCount(action: () -> Unit) {
    setOnMouseClicked {
        if (it.button === MouseButton.PRIMARY) {
            action()
        }
    }
}

fun Node.onMouseBackClick(clickCount: Int = 1, action: () -> Unit) {
    setOnMouseClicked {
        if (it.clickCount == clickCount && it.button === MouseButton.BACK) {
            action()
        }
    }
}

/**
 * ScrollPane
 */

fun EventTarget.scrollPane(scrollSpeed: ScrollSpeed = ScrollSpeed.MEDIUM, op: ScrollPane.() -> Unit = {}): ScrollPane = scrollpane(fitToWidth = true, fitToHeight = false) {
    addClass(Theme.scrollPaneBorderlessStyle)

    op.invoke(this)

    // Call after content initialisation to be sure the content is not null
    setScrollSpeed(scrollSpeed)
}

private fun ScrollPane.setScrollSpeed(scrollSpeed: ScrollSpeed) {
    val scrollSpeedFactor = when (scrollSpeed) {
        ScrollSpeed.SLOW -> 0.001
        ScrollSpeed.MEDIUM -> 0.0025
        ScrollSpeed.FAST -> 0.01
    }

    content.setOnScroll { scrollEvent ->
        val deltaY = scrollEvent.deltaY * scrollSpeedFactor
        vvalue -= deltaY
    }
}

enum class ScrollSpeed {
    SLOW,
    MEDIUM,
    FAST
}
