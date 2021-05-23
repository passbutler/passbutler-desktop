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
class VectorDrawableIcon(vectorDrawable: VectorDrawable) : Pane() {
    init {
        addClass(Theme.vectorDrawableIcon)

        style {
            shape = vectorDrawable.svgPath

            alignment = Pos.CENTER

            minWidth = vectorDrawable.desiredWidthPixels.px
            maxWidth = vectorDrawable.desiredWidthPixels.px

            minHeight = vectorDrawable.desiredHeightPixels.px
            maxHeight = vectorDrawable.desiredHeightPixels.px
        }
    }
}

fun EventTarget.vectorDrawableIcon(vectorDrawable: VectorDrawable, op: VectorDrawableIcon.() -> Unit = {}) = VectorDrawableIcon(vectorDrawable).attachTo(this, op)
