package de.passbutler.desktop.ui

import de.passbutler.common.base.Bindable
import de.passbutler.common.base.MutableBindable
import javafx.beans.value.ChangeListener
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextInputControl

/**
 * General binders
 */

fun <T> bind(baseUIComponent: BaseUIComponent, bindable: Bindable<T>, block: (T) -> Unit) {
    bindable.addLifecycleObserver(baseUIComponent, true) { newValue ->
        block(newValue)
    }
}

fun <T1, T2> bind(baseUIComponent: BaseUIComponent, bindable1: Bindable<T1>, bindable2: Bindable<T2>, block: (T1, T2) -> Unit) {
    bindable1.addLifecycleObserver(baseUIComponent, false) {
        block(bindable1.value, bindable2.value)
    }

    bindable2.addLifecycleObserver(baseUIComponent, true) {
        block(bindable1.value, bindable2.value)
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

fun <T1, T2> Node.bindVisibility(baseUIComponent: BaseUIComponent, bindable1: Bindable<T1>, bindable2: Bindable<T2>, block: (T1, T2) -> Boolean) {
    bindable1.addLifecycleObserver(baseUIComponent, true) { newValue ->
        val newVisibleValue = block(newValue, bindable2.value)
        isVisible = newVisibleValue
        isManaged = newVisibleValue
    }

    bindable2.addLifecycleObserver(baseUIComponent, true) { newValue ->
        val newVisibleValue = block(bindable1.value, newValue)
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

fun Node.bindEnabled(baseUIComponent: BaseUIComponent, bindable1: Bindable<Boolean>, bindable2: Bindable<Boolean>) {
    bindable1.addLifecycleObserver(baseUIComponent, false) {
        isEnabled = it && bindable2.value
    }

    bindable2.addLifecycleObserver(baseUIComponent, true) {
        isEnabled = it && bindable1.value
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
    val changeListener = ChangeListener<String> { _, _, newValue ->
        bindable.value = newValue
    }

    textProperty().addListener(changeListener)

    bindable.addLifecycleObserver(baseUIComponent, true) { newValue ->
        // Update bindable via text property and not vice versa to avoid the cursor position is lost
        if (text != newValue) {
            textProperty().removeListener(changeListener)

            text = newValue
            end()

            textProperty().addListener(changeListener)
        }
    }
}

fun TextInputControl.bindInputOptional(baseUIComponent: BaseUIComponent, bindable: MutableBindable<String?>) {
    val changeListener = ChangeListener<String> { _, _, newValue ->
        bindable.value = newValue
    }

    textProperty().addListener(changeListener)

    bindable.addLifecycleObserver(baseUIComponent, true) { newValue ->
        // Update bindable via text property and not vice versa to avoid the cursor position is lost
        if (text != newValue) {
            textProperty().removeListener(changeListener)

            text = newValue ?: ""
            end()

            textProperty().addListener(changeListener)
        }
    }
}

fun CheckBox.bindChecked(baseUIComponent: BaseUIComponent, bindable: MutableBindable<Boolean>) {
    val changeListener = ChangeListener<Boolean> { _, _, newValue ->
        bindable.value = newValue
    }

    selectedProperty().addListener(changeListener)

    bindable.addLifecycleObserver(baseUIComponent, true) { newValue ->
        if (isSelected != newValue) {
            selectedProperty().removeListener(changeListener)
            isSelected = newValue
            selectedProperty().addListener(changeListener)
        }
    }
}
