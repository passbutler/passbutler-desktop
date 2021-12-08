package de.passbutler.desktop

import de.passbutler.desktop.ui.BaseFragment
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.createHeaderView
import de.passbutler.desktop.ui.createTransparentSectionedLayout
import de.passbutler.desktop.ui.jfxButton
import de.passbutler.desktop.ui.marginL
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.showScreenFaded
import javafx.geometry.Pos
import javafx.scene.Node
import tornadofx.FX.Companion.messages
import tornadofx.UIComponent
import tornadofx.action
import tornadofx.addClass
import tornadofx.get
import tornadofx.paddingAll
import tornadofx.stackpane
import tornadofx.vbox
import kotlin.reflect.KClass

class AboutLoggedOutScreen : BaseFragment(messages["about_title"]), AboutScreenViewSetup {

    override val root = stackpane()

    private val previousScreen by param<KClass<UIComponent>>(null)

    init {
        with(root) {
            setupRootView()
        }

        shortcut("ESC") {
            showPreviousScreen()
        }
    }

    private fun showPreviousScreen() {
        showScreenFaded(previousScreen)
    }

    private fun Node.setupRootView() {
        createTransparentSectionedLayout(
            topSetup = {
                setupHeader()
            },
            centerSetup = {
                setupContent()
            },
            bottomSetup = {
                setupFooter()
            }
        )
    }

    private fun Node.setupHeader() {
        createHeaderView {
            paddingAll = marginM.value
        }
    }

    private fun Node.setupContent() {
        vbox(alignment = Pos.CENTER_LEFT) {
            paddingAll = marginM.value
            spacing = marginL.value

            setupAboutSection(this@AboutLoggedOutScreen)
        }
    }

    private fun Node.setupFooter() {
        vbox(alignment = Pos.CENTER_LEFT) {
            jfxButton(messages["general_back"]) {
                addClass(Theme.buttonTextOnSurfaceStyle)

                action {
                    showPreviousScreen()
                }
            }
        }
    }

    companion object {
        // Must the same name as property above
        const val PARAMETER_PREVIOUS_SCREEN = "previousScreen"
    }
}
