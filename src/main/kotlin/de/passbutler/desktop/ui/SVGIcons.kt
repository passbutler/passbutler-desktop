package de.passbutler.desktop.ui

import javafx.event.EventTarget
import javafx.scene.layout.Pane
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.px
import tornadofx.style

/**
 * Custom implementation similar to `SVGIcon` but with theme based tinting via style.
 */
class SmallSVGIcon(svgPath: String) : Pane() {
    init {
        addClass(Theme.iconStyle)

        style {
            shape = svgPath

            minWidth = 18.px
            minHeight = 18.px
            maxWidth = 18.px
            maxHeight = 18.px
        }
    }
}

fun EventTarget.smallSVGIcon(svgPath: String, op: SmallSVGIcon.() -> Unit = {}) = SmallSVGIcon(svgPath).attachTo(this, op)
