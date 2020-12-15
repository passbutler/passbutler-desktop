package de.passbutler.desktop.ui

import de.passbutler.common.base.Bindable
import de.passbutler.common.base.MutableBindable
import javafx.scene.Node
import javafx.scene.control.TextInputControl

/**
 * Enabled binders
 */

fun Node.bindEnabled(bindable: Bindable<Boolean>) {
    isEnabled = bindable.value

    // TODO: Leaking
    bindable.addObserver(null, false) {
        isEnabled = it
    }
}

/**
 * Input binders
 */

fun TextInputControl.bindInput(bindable: MutableBindable<String>) {
    text = bindable.value

    textProperty().addListener { _, _, newValue ->
        bindable.value = newValue
    }
}
