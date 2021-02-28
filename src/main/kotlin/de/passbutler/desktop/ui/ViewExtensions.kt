package de.passbutler.desktop.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXToggleButton
import de.passbutler.common.ui.FADE_TRANSITION_DURATION
import javafx.animation.Animation
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.control.ScrollPane
import javafx.scene.control.ToggleButton
import javafx.scene.control.skin.TextFieldSkin
import javafx.scene.effect.DropShadow
import javafx.scene.effect.Effect
import javafx.scene.input.MouseButton
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import tornadofx.ChangeListener
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.css
import tornadofx.fade
import tornadofx.hiddenWhen
import tornadofx.label
import tornadofx.onLeftClick
import tornadofx.paddingRight
import tornadofx.pane
import tornadofx.px
import tornadofx.stackpane
import tornadofx.visibleWhen

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

fun Node.showFadeInOutAnimation(shouldShow: Boolean) {
    val animationProperty = "de.passbutler.desktop.ui.showFadeInOutAnimation"

    // Cancel previous animation if any is running to avoid produce out-of-sync view state
    (properties[animationProperty] as? Animation)?.stop()

    if (shouldShow) {
        isVisible = true
        opacity = 0.0
        properties[animationProperty] = fade(FADE_TRANSITION_DURATION.toJavaFxDuration(), 1.0).apply {
            onFinished = null
        }
    } else {
        opacity = 1.0
        properties[animationProperty] = fade(FADE_TRANSITION_DURATION.toJavaFxDuration(), 0.0).apply {
            setOnFinished {
                isVisible = false
            }
        }
    }
}

fun JavaTimeDuration.toJavaFxDuration(): JavaFxDuration {
    return JavaFxDuration(toMillis().toDouble())
}

fun Node.onLeftClickIgnoringCount(action: () -> Unit) {
    setOnMouseClicked {
        if (it.button === MouseButton.PRIMARY) {
            action()
        }
    }
}

/**
 * Input fields
 */

fun EventTarget.passwordFieldMaskable(initialMaskPassword: Boolean = true, op: PasswordField.() -> Unit = {}): StackPane {
    val maskPasswordProperty = SimpleBooleanProperty(initialMaskPassword)

    val passwordField = PasswordField().apply {
        skin = object : TextFieldSkin(this) {
            override fun maskText(originalText: String): String {
                return if (maskPasswordProperty.value) {
                    super.maskText(originalText)
                } else {
                    originalText
                }
            }
        }
    }

    maskPasswordProperty.addListener(ChangeListener<Boolean> { _, _, _ ->
        // Trigger "change" to re-apply the masking
        passwordField.text = passwordField.text
    })

    return stackpane {
        passwordField.attachTo(this, op)

        stackpane {
            alignment = Pos.CENTER_RIGHT
            paddingRight = marginS.value

            // Do not consume clicks of password field
            isPickOnBounds = false

            smallSVGIcon(Drawables.ICON_VISIBILITY) {
                visibleWhen(maskPasswordProperty)

                onLeftClick {
                    maskPasswordProperty.value = false
                }
            }

            smallSVGIcon(Drawables.ICON_VISIBILITY_OFF) {
                hiddenWhen(maskPasswordProperty)

                onLeftClick {
                    maskPasswordProperty.value = true
                }
            }
        }
    }
}

/**
 * Buttons
 */

fun EventTarget.jfxButtonBase(text: String = "", graphic: Node? = null, op: JFXButton.() -> Unit = {}) = JFXButton(text.toUpperCase()).attachTo(this, op) {
    if (graphic != null) it.graphic = graphic
}

fun EventTarget.jfxButtonRaised(text: String = "", graphic: Node? = null, op: JFXButton.() -> Unit = {}) = jfxButtonBase(text, graphic) {
    buttonType = JFXButton.ButtonType.RAISED
    op.invoke(this)
}

fun EventTarget.jfxFloatingActionButtonRaised(text: String = "", graphic: Node? = null, op: JFXButton.() -> Unit = {}) = jfxButtonBase(text, graphic) {
    addClass(Theme.floatingActionButtonStyle)

    buttonType = JFXButton.ButtonType.RAISED
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
 * Labels
 */

fun EventTarget.textLabelWrapped(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = label(text, graphic) {
    isWrapText = true
    op.invoke(this)
}

fun EventTarget.textLabelWrapped(observable: ObservableValue<String>, op: Label.() -> Unit = {}) = label(observable) {
    isWrapText = true
    op.invoke(this)
}

/**
 * Labels (headlines)
 */

fun EventTarget.textLabelHeadline1(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = textLabelWrapped(text, graphic) {
    addClass(Theme.textHeadline1Style)
    op.invoke(this)
}

fun EventTarget.textLabelHeadline1(observable: ObservableValue<String>, op: Label.() -> Unit = {}) = textLabelWrapped(observable) {
    addClass(Theme.textHeadline1Style)
    op.invoke(this)
}

fun EventTarget.textLabelHeadline2(text: String = "", graphic: Node? = null, op: Label.() -> Unit = {}) = textLabelWrapped(text, graphic) {
    addClass(Theme.textHeadline2Style)
    op.invoke(this)
}

/**
 * Labels (body texts)
 */

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

/**
 * Shadows
 */

fun Node.paneWithDropShadow(dropShadowEffect: Effect, op: Pane.() -> Unit) {
    stackpane {
        pane {
            addClass(Theme.backgroundStyle)
            effect = bottomDropShadow()
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

fun Node.onMouseBackClick(clickCount: Int = 1, action: () -> Unit) {
    setOnMouseClicked {
        if (it.clickCount == clickCount && it.button === MouseButton.BACK)
            action()
    }
}

/**
 * ScrollPane
 */

fun ScrollPane.setScrollSpeed(scrollSpeed: ScrollSpeed) {
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
