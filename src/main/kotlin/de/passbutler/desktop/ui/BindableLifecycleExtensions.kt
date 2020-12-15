package de.passbutler.desktop.ui

import de.passbutler.common.base.Bindable
import de.passbutler.common.base.BindableObserver

// TODO: Use when possible
fun <T> Bindable<T>.addLifecycleObserver(baseUIComponent: BaseUIComponent, notifyOnRegister: Boolean, observer: BindableObserver<T>) {
    addObserver(baseUIComponent, notifyOnRegister, observer)

    baseUIComponent.addUndockedObserver {
        removeObserver(observer)
    }
}
