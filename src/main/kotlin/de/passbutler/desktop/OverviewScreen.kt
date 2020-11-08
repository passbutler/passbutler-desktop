package de.passbutler.desktop

import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelHeadline
import javafx.geometry.Pos
import javafx.scene.Node
import tornadofx.FX.Companion.messages
import tornadofx.action
import tornadofx.button
import tornadofx.get
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.vbox

class OverviewScreen : NavigationMenuScreen(messages["overview_title"]), RequestSending {

    private val viewModel by injectWithPrivateScope<OverviewViewModel>()

    override fun Node.createMainContent() {
        vbox {
            paddingAll = marginM.value
            alignment = Pos.CENTER

            textLabelHeadline(messages["overview_empty_screen_title"])
            textLabelBody1(messages["overview_empty_screen_description"]) {
                paddingTop = marginS.value
            }

            // TODO: Remove
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

