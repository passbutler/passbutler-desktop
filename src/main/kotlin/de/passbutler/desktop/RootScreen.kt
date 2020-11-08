package de.passbutler.desktop

import com.jfoenix.controls.JFXSnackbar
import de.passbutler.common.base.BindableObserver
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.BaseView
import de.passbutler.desktop.ui.DarkTheme
import de.passbutler.desktop.ui.UIPresenter
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxSnackbar
import de.passbutler.desktop.ui.jfxSpinner
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Pane
import kotlinx.coroutines.launch
import org.tinylog.kotlin.Logger
import tornadofx.action
import tornadofx.addClass
import tornadofx.addStylesheet
import tornadofx.borderpane
import tornadofx.center
import tornadofx.get
import tornadofx.hbox
import tornadofx.item
import tornadofx.menu
import tornadofx.menubar
import tornadofx.stackpane
import tornadofx.top

class RootScreen : BaseView() {

    override val root = borderpane()

    var contentContainer: Node? = null
        private set

    var progressView: Node? = null
        private set

    var bannerView: JFXSnackbar? = null
        private set

    private val viewModel by injectWithPrivateScope<RootViewModel>()

    private val rootScreenStateObserver: BindableObserver<RootViewModel.RootScreenState?> = {
        showRootScreen()
    }

    init {
        with(root) {
            top {
                menubar {
                    // Enforce dark theme to menu view because it should look always dark
                    addStylesheet(DarkTheme::class)

                    menu(messages["app_name"]) {
                        item(messages["menu_create_container"]).action {
                            // TODO
                        }
                        item(messages["menu_open_container"]).action {
                            // TODO
                        }
                        item(messages["menu_close_application"]).action {
                            closeApplicationClicked()
                        }
                    }
                }
            }

            center {
                stackpane {
                    contentContainer = stackpane()
                    progressView = createProgressView()
                    bannerView = createBannerView()
                }
            }
        }
    }

    private fun closeApplicationClicked() {
        Platform.exit()
    }

    override fun onDock() {
        super.onDock()

        uiPresentingDelegate = UIPresenter(this)

        viewModel.rootScreenState.addObserver(this, false, rootScreenStateObserver)

        launch {
            viewModel.restoreLoggedInUser()
        }
    }

    override fun onUndock() {
        super.onUndock()

        viewModel.rootScreenState.removeObserver(rootScreenStateObserver)
        viewModel.onCleared()

        Logger.debug("RootScreen was undocked")
    }

    private fun showRootScreen() {
        val rootScreenState = viewModel.rootScreenState.value
        Logger.debug("Show screen state '$rootScreenState'")

        when (rootScreenState) {
            is RootViewModel.RootScreenState.LoggedIn -> showLoggedInState()
            is RootViewModel.RootScreenState.LoggedOut -> showLoggedOutState()
        }
    }

    private fun showLoggedInState() {
        if (!isScreenShown(OverviewScreen::class)) {
            showScreen(OverviewScreen::class)
        }
    }

    private fun showLoggedOutState() {
        if (!isScreenShown(LoginScreen::class)) {
            showScreen(LoginScreen::class)
        }
    }
}

private fun Pane.createProgressView(): Pane {
    return hbox(alignment = Pos.CENTER) {
        addClass(Theme.scrimBackgroundStyle)

        jfxSpinner()
        isVisible = false
    }
}

private fun Pane.createBannerView(): JFXSnackbar {
    return jfxSnackbar(this)
}
