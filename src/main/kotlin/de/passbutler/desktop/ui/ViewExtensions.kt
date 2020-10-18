package de.passbutler.desktop.ui

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSpinner
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.layout.Pane
import tornadofx.Dimension
import tornadofx.attachTo
import tornadofx.useMaxWidth

val Number.dp: Dimension<Dimension.LinearUnits>
    get() = Dimension(this.toDouble(), Dimension.LinearUnits.pt)

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