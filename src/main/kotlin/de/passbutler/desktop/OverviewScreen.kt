package de.passbutler.desktop

import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.BaseFragment
import de.passbutler.desktop.ui.injectWithPrivateScope
import tornadofx.FX.Companion.messages
import tornadofx.action
import tornadofx.button
import tornadofx.get
import tornadofx.stackpane

class OverviewScreen : BaseFragment(messages["app_name"]), RequestSending {

    override val root = stackpane()

    private val viewModel by injectWithPrivateScope<OverviewViewModel>()

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