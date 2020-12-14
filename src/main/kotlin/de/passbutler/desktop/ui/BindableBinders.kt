package de.passbutler.desktop.ui

import de.passbutler.common.base.MutableBindable
import javafx.scene.control.TextInputControl

fun TextInputControl.bindInput(bindable: MutableBindable<String>) {
    text = bindable.value

    textProperty().addListener { _, _, newValue ->
        bindable.value = newValue
    }
}
