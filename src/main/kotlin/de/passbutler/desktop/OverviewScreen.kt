package de.passbutler.desktop

import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.BaseTheme
import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.textLabelWrapped
import javafx.scene.layout.Pane
import tornadofx.FX.Companion.messages
import tornadofx.action
import tornadofx.addClass
import tornadofx.button
import tornadofx.get

class OverviewScreen : NavigationMenuScreen(messages["app_name"]), RequestSending {

    private val viewModel by injectWithPrivateScope<OverviewViewModel>()

    override fun Pane.createMainContent() {
        textLabelWrapped("Willkommen bei Pass Butler") {
            addClass(BaseTheme.textHeadline1Style)
        }

        button("logout") {
            action {
                logoutUser()
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

