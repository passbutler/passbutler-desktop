package de.passbutler.desktop

import de.passbutler.desktop.ui.BaseFragment
import de.passbutler.desktop.ui.Drawables
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.VectorDrawable
import de.passbutler.desktop.ui.createHeaderView
import de.passbutler.desktop.ui.createTransparentSectionedLayout
import de.passbutler.desktop.ui.jfxButton
import de.passbutler.desktop.ui.marginL
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.showScreenFaded
import de.passbutler.desktop.ui.textLabelBase
import de.passbutler.desktop.ui.textLabelBodyOrder1
import de.passbutler.desktop.ui.textLabelHeadlineOrder2
import de.passbutler.desktop.ui.vectorDrawableIcon
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.VBox
import tornadofx.FX.Companion.messages
import tornadofx.action
import tornadofx.addClass
import tornadofx.get
import tornadofx.paddingTop
import tornadofx.stackpane
import tornadofx.vbox

class IntroductionScreen : BaseFragment(messages["introduction_title"]) {

    override val root = stackpane()

    init {
        with(root) {
            setupRootView()
        }
    }

    private fun Node.setupRootView() {
        createTransparentSectionedLayout(
            topSetup = {
                setupHeader()
            },
            centerSetup = {
                setupContent()
            }
        )
    }

    private fun Node.setupHeader() {
        createHeaderView()
    }

    private fun Node.setupContent() {
        vbox(alignment = Pos.CENTER_LEFT) {
            spacing = marginL.value

            textLabelBase(messages["introduction_headline"]) {
                addClass(Theme.textHeadline4Style)
            }

            createIntroductionSection(
                messages["introduction_create_user_headline"],
                messages["introduction_create_user_description"],
                messages["introduction_create_user_button_text"],
                Drawables.ICON_CHECK_CIRCLE
            ) {
                showScreenFaded(CreateLocalUserWizardScreen::class)
            }

            createIntroductionSection(
                messages["introduction_login_user_headline"],
                messages["introduction_login_user_description"],
                messages["introduction_login_user_button_text"],
                Drawables.ICON_LOGIN
            ) {
                showScreenFaded(LoginScreen::class)
            }
        }
    }

    private fun Node.createIntroductionSection(title: String, description: String, buttonTitle: String, buttonIcon: VectorDrawable, buttonAction: () -> Unit): VBox {
        return vbox {
            textLabelHeadlineOrder2(title)

            textLabelBodyOrder1(description) {
                paddingTop = marginS.value
            }

            vbox {
                paddingTop = marginS.value

                jfxButton(buttonTitle) {
                    addClass(Theme.buttonPrimaryStyle)

                    graphic = vectorDrawableIcon(buttonIcon)

                    action(buttonAction)
                }
            }
        }
    }
}
