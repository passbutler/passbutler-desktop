package de.passbutler.desktop

import de.passbutler.common.base.formattedDateTime
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.base.PathProvider
import de.passbutler.desktop.ui.Drawables
import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.bindTextAndVisibility
import de.passbutler.desktop.ui.bindVisibility
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.createInformationView
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.marginL
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.smallSVGIcon
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelHeadline1
import javafx.scene.Node
import javafx.scene.layout.VBox
import tornadofx.FX
import tornadofx.FX.Companion.messages
import tornadofx.FileChooserMode
import tornadofx.action
import tornadofx.addClass
import tornadofx.chooseFile
import tornadofx.get
import tornadofx.paddingAll
import tornadofx.paddingLeft
import tornadofx.paddingTop
import tornadofx.vbox
import java.io.File
import java.time.Instant

class AboutScreen : NavigationMenuScreen(messages["about_title"], navigationMenuItems = createDefaultNavigationMenu()), RequestSending {

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
            textLabelHeadline1(messages["about_header"])

            textLabelBody1 {
                paddingTop = marginS.value

                val versionName = BuildConfig.VERSION_NAME
                val formattedBuildTime = Instant.ofEpochMilli(BuildConfig.BUILD_TIMESTAMP).formattedDateTime(FX.locale)
                val gitShortHash = BuildConfig.BUILD_REVISION_HASH
                text = messages["about_subheader"].format(versionName, formattedBuildTime, gitShortHash)
            }

            textLabelBody1(messages["about_passage_1"]) {
                paddingTop = marginS.value
            }
        }
    }

    private fun VBox.setupPremiumKeySection() {
        vbox {
            textLabelHeadline1(messages["premium_header"])

            vbox {
                paddingTop = marginS.value

                setupNoPremiumKeyView()
                setupAvailablePremiumKeyView()
            }
        }
    }

    private fun Node.setupNoPremiumKeyView() {
        vbox {
            textLabelBody1(messages["premium_description_no_premium_key"])

            vbox {
                paddingTop = marginM.value

                jfxButtonRaised(messages["premium_register_premium_key_button_text"]) {
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
        showOpenPremiumKeyFileChooser(messages["premium_register_premium_key_button_text"]) { choosenFile ->
            launchRequestSending(
                handleSuccess = { showInformation(messages["premium_register_premium_key_successful_message"]) },
                handleFailure = { showError(messages["premium_register_premium_key_failed_general_title"]) }
            ) {
                viewModel.registerPremiumKey(choosenFile)
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

            textLabelBody1(messages["premium_description_available_premium_key"])

            setupPremiumKeyCard()

            vbox {
                jfxButtonRaised(messages["premium_remove_premium_key_button_text"]) {
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

            addClass(Theme.emphasizedCardStyle)

            val cardHeadline = textLabelHeadline1 {
                graphic = smallSVGIcon(Drawables.ICON_VERIFIED.svgPath)
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
        launchRequestSending(
            handleSuccess = { showInformation(messages["premium_remove_premium_key_successful_message"]) },
            handleFailure = { showError(messages["premium_remove_premium_key_failed_general_title"]) }
        ) {
            viewModel.removePremiumKey()
        }
    }
}
