package de.passbutler.desktop.ui

import de.passbutler.common.base.Bindable
import de.passbutler.common.base.MutableBindable
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextInputControl
import tornadofx.action


/**
 * General binders
 */

fun <T> Node.bind(baseUIComponent: BaseUIComponent, bindable: Bindable<T>, block: (T) -> Unit) {
    bindable.addLifecycleObserver(baseUIComponent, true) { newValue ->
        block(newValue)
    }
}

/**
 * Visibility binders
 */

fun <T> Node.bindVisibility(baseUIComponent: BaseUIComponent, bindable: Bindable<T>, block: (T) -> Boolean) {
    bindable.addLifecycleObserver(baseUIComponent, true) { newValue ->
        val newVisibleValue = block(newValue)
        isVisible = newVisibleValue
        isManaged = newVisibleValue
    }
}

fun Node.bindVisibility(baseUIComponent: BaseUIComponent, bindable: Bindable<Boolean>) {
    bindable.addLifecycleObserver(baseUIComponent, true) { newValue ->
        isVisible = newValue
        isManaged = newValue
    }
}

/**
 * Enabled binders
 */

fun Node.bindEnabled(baseUIComponent: BaseUIComponent, bindable: Bindable<Boolean>) {
    bindable.addLifecycleObserver(baseUIComponent, true) {
        isEnabled = it
    }
}

/**
 * Text binders
 */

fun Label.bindTextAndVisibility(baseUIComponent: BaseUIComponent, bindable: Bindable<String?>) {
    bindable.addLifecycleObserver(baseUIComponent, true) { newValue ->
        if (newValue != null) {
            text = newValue
            isVisible = true
            isManaged = true
        } else {
            text = ""
            isVisible = false
            isManaged = false
        }
    }
}

fun <T> Label.bindTextAndVisibility(baseUIComponent: BaseUIComponent, bindable: Bindable<T>, transform: (T) -> String?) {
    bindable.addLifecycleObserver(baseUIComponent, true) { newValue ->
        val newTransformedValue = transform(newValue)

        if (newTransformedValue != null) {
            text = newTransformedValue
            isVisible = true
            isManaged = true
        } else {
            text = ""
            isVisible = false
            isManaged = false
        }
    }
}

/**
 * Input binders
 */

fun TextInputControl.bindInput(baseUIComponent: BaseUIComponent, bindable: MutableBindable<String>) {
    bindable.addLifecycleObserver(baseUIComponent, true) { newValue ->
        // Update bindable via text property and not vice versa to avoid the cursor position is lost
        if (text != newValue) {
            text = newValue
            end()
        }
    }

    textProperty().addListener { _, _, newValue ->
        bindable.value = newValue
    }
}

fun TextInputControl.bindInputOptional(baseUIComponent: BaseUIComponent, bindable: MutableBindable<String?>) {
    bindable.addLifecycleObserver(baseUIComponent, true) { newValue ->
        // Update bindable via text property and not vice versa to avoid the cursor position is lost
        if (text != newValue) {
            text = newValue ?: ""
            end()
        }
    }

    textProperty().addListener { _, _, newValue ->
        bindable.value = newValue
    }
}

fun CheckBox.bindChecked(baseUIComponent: BaseUIComponent, bindable: MutableBindable<Boolean>) {
    bindable.addLifecycleObserver(baseUIComponent, true) { newValue ->
        if (isSelected != newValue) {
            isSelected = newValue
        }
    }

    action {
        bindable.value = !bindable.value
    }
}
