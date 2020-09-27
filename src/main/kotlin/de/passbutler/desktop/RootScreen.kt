package de.passbutler.desktop

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*

class RootScreen : View() {

    val vm : RootViewModel by inject()

    override val root = form {
        fieldset {
            field("Data") {
                textfield(vm.data)
            }
        }
        button("Save") {
            action {
                vm.save()
            }
        }

        padding = Insets(10.0)
        vgrow = Priority.ALWAYS
        alignment = Pos.CENTER_LEFT
    }
}
