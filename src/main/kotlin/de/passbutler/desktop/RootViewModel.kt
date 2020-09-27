package de.passbutler.desktop

import javafx.beans.property.SimpleStringProperty
import tornadofx.ViewModel

class RootViewModel : ViewModel() {

    val data = SimpleStringProperty()

    fun save() {
        val dataToSave = data.value

        // execute save here
        println("Saving $dataToSave")
    }
}