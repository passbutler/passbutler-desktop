package de.passbutler.desktop

import de.passbutler.common.base.formattedDateTime
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.base.PathProvider
import de.passbutler.desktop.ui.Drawables
import de.passbutler.desktop.ui.NavigationMenuFragment
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.bindTextAndVisibility
import de.passbutler.desktop.ui.bindVisibility
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.createInformationView
import de.passbutler.desktop.ui.jfxButton
import de.passbutler.desktop.ui.marginL
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.showConfirmDialog
import de.passbutler.desktop.ui.textLabelBodyOrder1
import de.passbutler.desktop.ui.textLabelBodyOrder2
import de.passbutler.desktop.ui.textLabelHeadlineOrder1
import de.passbutler.desktop.ui.textLabelHeadlineOrder2
import de.passbutler.desktop.ui.vectorDrawableIcon
import javafx.scene.Node
import javafx.scene.layout.VBox
import tornadofx.FX
import tornadofx.FX.Companion.messages
import tornadofx.FileChooserMode
import tornadofx.action
import tornadofx.addClass
import tornadofx.chooseFile
import tornadofx.get
import tornadofx.hyperlink
import tornadofx.paddingAll
import tornadofx.paddingLeft
import tornadofx.paddingTop
import tornadofx.stackpane
import tornadofx.textflow
import tornadofx.vbox
import java.io.File
import java.time.Instant

class AboutScreen : NavigationMenuFragment(messages["about_title"], navigationMenuItems = createDefaultNavigationMenu()), RequestSending {

    private val viewModel by injectPremiumKeyViewModel()

    init {
        setupRootView()
    }

    override fun Node.setupMainContent() {
        vbox {
            paddingAll = marginM.value
            spacing = marginL.value

            setupAboutSection()
            setupPremiumKeySection()
        }
    }

    private fun Node.setupAboutSection() {
        vbox {
            textLabelHeadlineOrder1(messages["about_header"])

            textflow {
                paddingTop = marginS.value

                val versionName = BuildConfig.VERSION_NAME
                val formattedBuildTime = Instant.ofEpochMilli(BuildConfig.BUILD_TIMESTAMP).formattedDateTime(FX.locale)
                val gitShortHash = BuildConfig.BUILD_REVISION_HASH

                val formattedText = messages["about_subheader"].format(versionName, formattedBuildTime, gitShortHash)
                val formattedTextBeforeGitHash = formattedText.substringBefore(gitShortHash)
                val formattedTextAfterGitHash = formattedText.substringAfter(gitShortHash)

                textLabelBodyOrder1(formattedTextBeforeGitHash)

                hyperlink(gitShortHash) {
                    action {
                        hostServices.showDocument(GIT_PROJECT_URL.format(gitShortHash))
                    }
                }

                textLabelBodyOrder1(formattedTextAfterGitHash)
            }

            textLabelBodyOrder2(messages["about_passage_1"]) {
                paddingTop = marginS.value
            }
        }
    }

    private fun VBox.setupPremiumKeySection() {
        vbox {
            textLabelHeadlineOrder2(messages["premium_header"])

            stackpane {
                paddingTop = marginS.value

                setupNoPremiumKeyView()
                setupAvailablePremiumKeyView()
            }
        }
    }

    private fun Node.setupNoPremiumKeyView() {
        vbox {
            textLabelBodyOrder1(messages["premium_description_no_premium_key"])

            vbox {
                paddingTop = marginM.value

                jfxButton(messages["premium_register_premium_key_button_text"]) {
                    addClass(Theme.buttonPrimaryStyle)

                    action {
                        registerPremiumKeyClicked()
                    }
                }
            }

            bindVisibility(this@AboutScreen, viewModel.premiumKey) {
                it == null
            }
        }
    }

    private fun registerPremiumKeyClicked() {
        showOpenPremiumKeyFileChooser(messages["premium_register_premium_key_button_text"]) { chosenFile ->
            launchRequestSending(
                handleSuccess = { showInformation(messages["premium_register_premium_key_successful_message"]) },
                handleFailure = { showError(messages["premium_register_premium_key_failed_general_title"]) }
            ) {
                viewModel.registerPremiumKey(chosenFile)
            }
        }
    }

    private fun showOpenPremiumKeyFileChooser(title: String, chosenFileBlock: (File) -> Unit) {
        val homeDirectory = PathProvider.obtainDirectoryBlocking { homeDirectory }

        chooseFile(title, emptyArray(), initialDirectory = homeDirectory, mode = FileChooserMode.Single).firstOrNull()?.let {
            chosenFileBlock(it)
        }
    }

    private fun Node.setupAvailablePremiumKeyView() {
        vbox {
            spacing = marginM.value

            textLabelBodyOrder1(messages["premium_description_available_premium_key"])

            setupPremiumKeyCard()

            vbox {
                jfxButton(messages["premium_remove_premium_key_button_text"]) {
                    addClass(Theme.buttonPrimaryStyle)

                    action {
                        removePremiumKeyClicked()
                    }
                }
            }

            bindVisibility(this@AboutScreen, viewModel.premiumKey) {
                it != null
            }
        }
    }

    private fun Node.setupPremiumKeyCard() {
        vbox {
            paddingAll = marginM.value
            spacing = marginS.value

            addClass(Theme.cardEmphasizedStyle)

            val cardHeadline = textLabelHeadlineOrder2 {
                graphic = vectorDrawableIcon(Drawables.ICON_VERIFIED)
                graphicTextGap = marginM.value

                bindTextAndVisibility(this@AboutScreen, viewModel.premiumKey) { it?.name }
            }

            // Width of the graphic + spacing
            val cardHeadlineIconOffset = 18.0 + cardHeadline.graphicTextGap

            createInformationView(messages["premium_card_premium_key_email"]) {
                bindTextAndVisibility(this@AboutScreen, viewModel.premiumKey) { it?.email }
            }.apply {
                paddingLeft = cardHeadlineIconOffset
            }

            createInformationView(messages["premium_card_premium_key_id"]) {
                bindTextAndVisibility(this@AboutScreen, viewModel.premiumKey) { it?.id }
            }.apply {
                paddingLeft = cardHeadlineIconOffset
            }
        }
    }

    private fun removePremiumKeyClicked() {
        showConfirmDialog(
            title = messages["premium_remove_premium_key_confirmation_title"],
            message = messages["premium_remove_premium_key_confirmation_message"],
            positiveActionTitle = messages["general_remove"],
            positiveClickAction = {
                removePremiumKey()
            }
        )
    }

    private fun removePremiumKey() {
        launchRequestSending(
            handleSuccess = { showInformation(messages["premium_remove_premium_key_successful_message"]) },
            handleFailure = { showError(messages["premium_remove_premium_key_failed_general_title"]) }
        ) {
            viewModel.removePremiumKey()
        }
    }

    companion object {
        private const val GIT_PROJECT_URL = "https://git.sicherheitskritisch.de/passbutler/passbutler-desktop/commit/%s"
    }
}
