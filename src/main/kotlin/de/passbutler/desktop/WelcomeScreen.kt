package de.passbutler.desktop

import de.passbutler.desktop.ui.BaseFragment
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelHeadline
import javafx.geometry.Pos
import javafx.scene.Node
import tornadofx.FX.Companion.messages
import tornadofx.action
import tornadofx.addClass
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
            // TODO
        }
    }

    private fun Node.createOpenVaultCardView() {
        createCardView(messages["welcome_open_vault_headline"], messages["welcome_open_vault_description"], messages["welcome_open_vault_button_text"]) {
            // TODO
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
