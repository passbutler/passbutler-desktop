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
import de.passbutler.desktop.ui.openBrowser
import de.passbutler.desktop.ui.scrollPane
import de.passbutler.desktop.ui.showConfirmDialog
import de.passbutler.desktop.ui.textLabelBodyOrder1
import de.passbutler.desktop.ui.textLabelHeadlineOrder1
import de.passbutler.desktop.ui.textLabelHeadlineOrder2
import de.passbutler.desktop.ui.vectorDrawableIcon
import javafx.scene.Node
import javafx.scene.layout.VBox
import tornadofx.Component
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

class AboutLoggedInScreen : NavigationMenuFragment(messages["about_title"], navigationMenuItems = createDefaultNavigationMenu()), AboutScreenViewSetup, RequestSending {

    private val viewModel by injectPremiumKeyViewModel()

    init {
        setupRootView()
    }

    override fun Node.setupMainContent() {
        scrollPane {
            vbox {
                paddingAll = marginM.value
                spacing = marginL.value

                setupAboutSection(this@AboutLoggedInScreen)
                setupPremiumKeySection()
            }
        }
    }

    private fun VBox.setupPremiumKeySection() {
        vbox {
            textLabelHeadlineOrder1(messages["premium_headline"])

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

            bindVisibility(this@AboutLoggedInScreen, viewModel.premiumKey) {
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

            bindVisibility(this@AboutLoggedInScreen, viewModel.premiumKey) {
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

                bindTextAndVisibility(this@AboutLoggedInScreen, viewModel.premiumKey) { it?.name }
            }

            // Width of the graphic + spacing
            val cardHeadlineIconOffset = 18.0 + cardHeadline.graphicTextGap

            createInformationView(messages["premium_card_premium_key_email"]) {
                bindTextAndVisibility(this@AboutLoggedInScreen, viewModel.premiumKey) { it?.email }
            }.apply {
                paddingLeft = cardHeadlineIconOffset
            }

            createInformationView(messages["premium_card_premium_key_id"]) {
                bindTextAndVisibility(this@AboutLoggedInScreen, viewModel.premiumKey) { it?.id }
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
}

interface AboutScreenViewSetup {
    fun Node.setupAboutSection(presentingComponent: Component) {
        vbox {
            textLabelHeadlineOrder1(messages["about_headline"])

            setupVersionInformation(presentingComponent)
            setupImprintSection(presentingComponent)
            setupPrivacyPolicySection(presentingComponent)
        }
    }

    private fun VBox.setupVersionInformation(presentingComponent: Component) {
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
                    presentingComponent.openBrowser(GIT_PROJECT_URL.format(gitShortHash))
                }
            }

            textLabelBodyOrder1(formattedTextAfterGitHash)
        }
    }

    private fun VBox.setupImprintSection(presentingComponent: Component) {
        textLabelHeadlineOrder1(messages["about_imprint_headline"]) {
            paddingTop = marginL.value
        }

        vbox {
            paddingTop = marginS.value

            jfxButton(messages["about_open_imprint_button_text"]) {
                addClass(Theme.buttonPrimaryStyle)

                action {
                    presentingComponent.openBrowser(messages["about_imprint_url"])
                }
            }
        }
    }

    private fun VBox.setupPrivacyPolicySection(presentingComponent: Component) {
        textLabelHeadlineOrder1(messages["about_privacy_policy_headline"]) {
            paddingTop = marginL.value
        }

        vbox {
            paddingTop = marginS.value

            jfxButton(messages["about_open_privacy_policy_button_text"]) {
                addClass(Theme.buttonPrimaryStyle)

                action {
                    presentingComponent.openBrowser(messages["about_privacy_policy_url"])
                }
            }
        }
    }

    companion object {
        private const val GIT_PROJECT_URL = "https://github.com/passbutler/passbutler-desktop/commit/%s"
    }
}
