package de.passbutler.desktop

import de.passbutler.desktop.ui.CoroutineScopedView
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import kotlinx.coroutines.launch
import org.tinylog.kotlin.Logger
import tornadofx.*

class RootScreen : CoroutineScopedView() {

    val viewModel: RootViewModel by inject()

    override fun onDock() {
        super.onDock()

        launch {
            viewModel.restoreLoggedInUser()
        }
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.onCleared()

        Logger.debug("RootScreen was undocked")
    }

    override val root = form {
        fieldset {
            field("Data") {
                // textfield(vm.data)
            }
        }
        button("Save") {
            action {
                //vm.save()
            }
        }

        padding = Insets(10.0)
        vgrow = Priority.ALWAYS
        alignment = Pos.CENTER_LEFT
    }
}
