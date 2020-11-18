package de.passbutler.desktop

import de.passbutler.desktop.ui.BaseFragment
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.showOpenVaultFileChooser
import de.passbutler.desktop.ui.showSaveVaultFileChooser
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelHeadline
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.stage.FileChooser
import kotlinx.coroutines.runBlocking
import tornadofx.FX.Companion.messages
import tornadofx.FileChooserMode
import tornadofx.action
import tornadofx.addClass
import tornadofx.chooseFile
import tornadofx.get
import tornadofx.hbox
import tornadofx.paddingAll
import tornadofx.paddingBottom
import tornadofx.paddingTop
import tornadofx.pane
import tornadofx.px
import tornadofx.stackpane
import tornadofx.style
import tornadofx.vbox

class WelcomeScreen : BaseFragment(messages["welcome_title"]) {

    override val root = stackpane()

    private val viewModel by inject<RootViewModel>()

    init {
        with(root) {
            setupContentView()
        }
    }

    private fun Node.setupContentView() {
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

                    createCreateVaultCardView()
                    createOpenVaultCardView()
                }
            }
        }
    }

    private fun Node.createCreateVaultCardView() {
        createCardView(messages["welcome_create_vault_headline"], messages["welcome_create_vault_description"], messages["welcome_create_vault_button_text"]) {
            showSaveVaultFileChooser(messages["welcome_create_vault_headline"]) { choosenFile ->
                // TODO: Not blocking -> request sending
                runBlocking {
                    viewModel.createVault(choosenFile)
                }

                // TODO: should be managed by rootviewmodel
                showScreen(LoginScreen::class)
            }
        }
    }

    private fun Node.createOpenVaultCardView() {
        createCardView(messages["welcome_open_vault_headline"], messages["welcome_open_vault_description"], messages["welcome_open_vault_button_text"]) {
            showOpenVaultFileChooser(messages["welcome_open_vault_headline"]) { choosenFile ->
                // TODO: Not blocking -> request sending
                runBlocking {
                    viewModel.openVault(choosenFile)
                }
            }
        }
    }

    private fun Node.createCardView(title: String, description: String, buttonTitle: String, buttonAction: () -> Unit) {
        vbox {
            addClass(Theme.cardViewBackgroundStyle)

            style {
                paddingAll = marginM.value
                prefWidth = 420.px
            }

            textLabelHeadline(title)

            textLabelBody1(description) {
                paddingTop = marginS.value
                paddingBottom = marginM.value
            }

            jfxButtonRaised(buttonTitle) {
                action(buttonAction)
            }
        }
    }
}
