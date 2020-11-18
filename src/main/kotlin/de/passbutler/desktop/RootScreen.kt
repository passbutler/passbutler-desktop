package de.passbutler.desktop

import com.jfoenix.controls.JFXSnackbar
import de.passbutler.common.base.BindableObserver
import de.passbutler.desktop.ui.BaseView
import de.passbutler.desktop.ui.DarkTheme
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.UIPresenter
import de.passbutler.desktop.ui.bottomDropShadow
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

    private val viewModel by inject<RootViewModel>()

    private val rootScreenStateObserver: BindableObserver<RootViewModel.RootScreenState?> = {
        updateRootScreen()
    }

    private val lockScreenStateObserver: BindableObserver<RootViewModel.LockScreenState?> = {
        updateRootScreen()
    }

    init {
        with(root) {
            center {
                stackpane {
                    contentContainer = stackpane()
                    progressView = createProgressView()
                    bannerView = createBannerView()
                }
            }

            top {
                menubar {
                    // Enforce dark theme to menu view because it should look always dark
                    addStylesheet(DarkTheme::class)

                    effect = bottomDropShadow()

                    menu(messages["app_name"]) {
                        item(messages["menu_create_container"]).action {
                            // TODO close vault, show file chooser and create file
                        }
                        item(messages["menu_open_container"]).action {
                            // TODO close vault, show file chooser and open file
                        }
                        item(messages["menu_close_application"]).action {
                            closeApplicationClicked()
                        }
                    }
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
        viewModel.lockScreenState.addObserver(this, false, lockScreenStateObserver)

        launch {
            viewModel.openRecentVault()
        }
    }

    override fun onUndock() {
        super.onUndock()

        viewModel.rootScreenState.removeObserver(rootScreenStateObserver)
        viewModel.lockScreenState.removeObserver(lockScreenStateObserver)
        viewModel.onCleared()

        Logger.debug("RootScreen was undocked")
    }

    private fun updateRootScreen() {
        val rootScreenState = viewModel.rootScreenState.value
        val lockScreenState = viewModel.lockScreenState.value
        Logger.debug("Show screen state '$rootScreenState' with lock screen state '$lockScreenState'")

        when (rootScreenState) {
            is RootViewModel.RootScreenState.LoggedIn -> {
                when (lockScreenState) {
                    RootViewModel.LockScreenState.Locked -> showLockedState()
                    RootViewModel.LockScreenState.Unlocked -> showLoggedInState()
                }
            }
            is RootViewModel.RootScreenState.LoggedOut -> {
                // TODO: login/welcome selection
                showLoggedOutState()
            }
        }
    }

    private fun showLockedState() {
        if (!isScreenShown(LockedScreen::class)) {
            showScreen(LockedScreen::class)
        }
    }

    private fun showLoggedInState() {
        if (!isScreenShown(OverviewScreen::class)) {
            showScreen(OverviewScreen::class)
        }
    }

    private fun showLoggedOutState() {
        if (!isScreenShown(WelcomeScreen::class)) {
            showScreen(WelcomeScreen::class)
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
