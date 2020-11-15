package de.passbutler.desktop

import javafx.beans.property.SimpleStringProperty
import tornadofx.ViewModel

class LockedScreenViewModel : ViewModel() {
    val passwordProperty = bind { SimpleStringProperty() }
}