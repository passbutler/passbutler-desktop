package de.passbutler.desktop.ui

import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.PasswordField
import javafx.scene.control.skin.TextFieldSkin
import javafx.scene.layout.StackPane
import tornadofx.ChangeListener
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.hiddenWhen
import tornadofx.onLeftClick
import tornadofx.paddingRight
import tornadofx.stackpane
import tornadofx.visibleWhen

class UnmaskablePasswordField(initialMaskPassword: Boolean, op: PasswordField.() -> Unit) : StackPane() {

    val wrappedPasswordField: PasswordField

    private val maskPasswordProperty = SimpleBooleanProperty(initialMaskPassword)

    init {
        wrappedPasswordField = createUnmaskablePasswordField()

        maskPasswordProperty.addListener(ChangeListener<Boolean> { _, _, _ ->
            // Trigger "change" to re-apply the masking
            wrappedPasswordField.text = wrappedPasswordField.text
        })

        wrappedPasswordField.attachTo(this, op)

        stackpane {
            alignment = Pos.CENTER_RIGHT
            paddingRight = marginS.value

            // Do not consume clicks meant for the password field
            isPickOnBounds = false

            setupShownIcon()
            setupHiddenIcon()
        }
    }

    private fun createUnmaskablePasswordField(): PasswordField {
        return PasswordField().apply {
            addClass(Theme.textFieldUnmaskablePasswordStyle)

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
    }

    private fun Node.setupShownIcon() {
        vectorDrawableIcon(Drawables.ICON_VISIBILITY) {
            visibleWhen(maskPasswordProperty)

            onLeftClick {
                maskPasswordProperty.value = false
            }
        }
    }

    private fun Node.setupHiddenIcon() {
        vectorDrawableIcon(Drawables.ICON_VISIBILITY_OFF) {
            hiddenWhen(maskPasswordProperty)

            onLeftClick {
                maskPasswordProperty.value = true
            }
        }
    }
}

fun EventTarget.unmaskablePasswordField(initialMaskPassword: Boolean = true, op: PasswordField.() -> Unit = {}) = UnmaskablePasswordField(initialMaskPassword, op).attachTo(this)
