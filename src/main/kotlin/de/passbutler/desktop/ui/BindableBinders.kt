package de.passbutler.desktop.ui

import de.passbutler.common.base.Bindable
import de.passbutler.common.base.MutableBindable
import javafx.scene.Node
import javafx.scene.control.TextInputControl

/**
 * Enabled binders
 */

fun Node.bindEnabled(baseUIComponent: BaseUIComponent, bindable: Bindable<Boolean>) {
    bindable.addLifecycleObserver(baseUIComponent, true) {
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
