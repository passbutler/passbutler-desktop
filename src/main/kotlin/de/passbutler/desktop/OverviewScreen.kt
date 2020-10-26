package de.passbutler.desktop

import de.passbutler.desktop.ui.CoroutineScopedFragment
import de.passbutler.desktop.ui.CoroutineScopedView
import tornadofx.*
import tornadofx.FX.Companion.messages

class OverviewScreen : CoroutineScopedFragment(messages["app_name"]) {

    override val root = stackpane()

    val viewModel: RootViewModel by inject()

    init {
        with(root) {
            button("logout") {
                action {
                    viewModel.rootScreenState.value = RootViewModel.RootScreenState.LoggedOut
                }
            }
        }
    }
}