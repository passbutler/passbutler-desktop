package de.passbutler.desktop.ui

import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.layout.Pane
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.px
import tornadofx.style

/**
 * Custom implementation similar to `SVGIcon` but with theme based tinting via style.
 */
class SmallSVGIcon(drawable: Drawable) : Pane() {
    init {
        addClass(Theme.iconStyle)

        style {
            shape = drawable.svgPath

            alignment = Pos.CENTER

            minWidth = drawable.desiredWidthPixels.px
            maxWidth = drawable.desiredWidthPixels.px

            minHeight = drawable.desiredHeightPixels.px
            maxHeight = drawable.desiredHeightPixels.px
        }
    }
}

fun EventTarget.smallSVGIcon(drawable: Drawable, op: SmallSVGIcon.() -> Unit = {}) = SmallSVGIcon(drawable).attachTo(this, op)
