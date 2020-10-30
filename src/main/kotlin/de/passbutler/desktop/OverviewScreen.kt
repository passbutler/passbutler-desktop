package de.passbutler.desktop

import de.passbutler.desktop.base.RequestSending
import de.passbutler.desktop.base.launchRequestSending
import de.passbutler.desktop.ui.BaseFragment
import tornadofx.FX.Companion.messages
import tornadofx.action
import tornadofx.button
import tornadofx.get
import tornadofx.stackpane

class OverviewScreen : BaseFragment(messages["app_name"]), RequestSending {

    override val root = stackpane()

    private val viewModel: OverviewViewModel by inject()

    init {
        with(root) {
            button("logout") {
                action {
                    logoutUser()
                }
            }
        }
    }

    private fun logoutUser() {
        launchRequestSending(
            handleFailure = { showError(messages["overview_logout_failed_title"]) },
            isCancellable = false
        ) {
            viewModel.logoutUser()
        }
    }
}