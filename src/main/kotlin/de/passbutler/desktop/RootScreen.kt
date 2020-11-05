package de.passbutler.desktop

import com.jfoenix.controls.JFXSnackbar
import de.passbutler.common.base.BindableObserver
import de.passbutler.desktop.ui.*
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Pane
import kotlinx.coroutines.launch
import org.tinylog.kotlin.Logger
import tornadofx.addClass
import tornadofx.hbox
import tornadofx.stackpane

class RootScreen : BaseView() {

    override val root = stackpane {
        contentContainer = stackpane()
        progressView = createProgressView()
        bannerView = createBannerView()
    }

    var contentContainer: Node? = null
        private set

    var progressView: Node? = null
        private set

    var bannerView: JFXSnackbar? = null
        private set

    private val viewModel: RootViewModel by inject()

    private val rootScreenStateObserver: BindableObserver<RootViewModel.RootScreenState?> = {
        showRootScreen()
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
        showScreen(OverviewScreen::class)
    }

    private fun showLoggedOutState() {
        showScreen(LoginScreen::class)
    }
}

private fun Pane.createProgressView(): Pane {
    return hbox(alignment = Pos.CENTER) {
        addClass(BaseTheme.scrimBackgroundStyle)

        jfxSpinner()
        isVisible = false
    }
}

private fun Pane.createBannerView(): JFXSnackbar {
    return jfxSnackbar(this)
}