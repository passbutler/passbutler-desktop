package de.passbutler.desktop

import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.BaseFragment
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.showOpenVaultFileChooser
import de.passbutler.desktop.ui.showSaveVaultFileChooser
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelHeadline1
import javafx.geometry.Pos
import javafx.scene.Node
import tornadofx.FX.Companion.messages
import tornadofx.action
import tornadofx.addClass
import tornadofx.get
import tornadofx.hbox
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.pane
import tornadofx.px
import tornadofx.stackpane
import tornadofx.style
import tornadofx.vbox

class WelcomeScreen : BaseFragment(messages["welcome_title"]), RequestSending {

    override val root = stackpane()

    private val viewModel by injectRootViewModel()

    init {
        with(root) {
            setupRootView()
        }
    }

    private fun Node.setupRootView() {
        stackpane {
            pane {
                addClass(Theme.abstractBackgroundStyle)
            }

            pane {
                addClass(Theme.abstractBackgroundOverlayStyle)
            }

            hbox(alignment = Pos.CENTER) {
                vbox(alignment = Pos.CENTER) {
                    spacing = marginM.value

                    setupCreateVaultCardView()
                    setupOpenVaultCardView()
                }
            }
        }
    }

    private fun Node.setupCreateVaultCardView() {
        setupCardView(messages["welcome_create_vault_headline"], messages["welcome_create_vault_description"], messages["welcome_create_vault_button_text"]) {
            createVaultClicked()
        }
    }

    private fun createVaultClicked() {
        showSaveVaultFileChooser(messages["welcome_create_vault_headline"]) { choosenFile ->
            launchRequestSending(
                handleFailure = {
                    val errorStringResourceId = when (it) {
                        is VaultFileAlreadyExistsException -> "general_create_vault_failed_already_existing_title"
                        else -> "general_create_vault_failed_title"
                    }

                    showError(messages[errorStringResourceId])
                },
                isCancellable = false
            ) {
                viewModel.createVault(choosenFile)
            }
        }
    }

    private fun Node.setupOpenVaultCardView() {
        setupCardView(messages["welcome_open_vault_headline"], messages["welcome_open_vault_description"], messages["welcome_open_vault_button_text"]) {
            openVaultClicked()
        }
    }

    private fun openVaultClicked() {
        showOpenVaultFileChooser(messages["welcome_open_vault_headline"]) { choosenFile ->
            launchRequestSending(
                handleFailure = { showError(messages["general_open_vault_failed_title"]) },
                isCancellable = false
            ) {
                viewModel.openVault(choosenFile)
            }
        }
    }

    private fun Node.setupCardView(title: String, description: String, buttonTitle: String, buttonAction: () -> Unit) {
        vbox {
            addClass(Theme.cardViewBackgroundStyle)

            style {
                paddingAll = marginM.value
                prefWidth = 420.px
            }

            textLabelHeadline1(title)

            textLabelBody1(description) {
                paddingTop = marginS.value
            }

            vbox {
                paddingTop = marginM.value

                jfxButtonRaised(buttonTitle) {
                    action(buttonAction)
                }
            }
        }
    }
}
