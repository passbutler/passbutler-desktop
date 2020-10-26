package de.passbutler.desktop

import de.passbutler.common.base.BindableObserver
import de.passbutler.desktop.ui.CoroutineScopedView
import javafx.util.Duration
import kotlinx.coroutines.launch
import org.tinylog.kotlin.Logger
import tornadofx.*

class RootScreen : CoroutineScopedView() {

    override val root = stackpane()

    private val viewModel: RootViewModel by inject()

    private val rootScreenStateObserver: BindableObserver<RootViewModel.RootScreenState?> = {
        showRootScreen()
    }

    override fun onDock() {
        super.onDock()

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
        val overview = find(OverviewScreen::class)
        replaceView(overview)
    }

    private fun showLoggedOutState() {
        val login = find(LoginScreen::class)
        replaceView(login)
    }

    private fun replaceView(component: UIComponent) {
        root.getChildList()?.apply {
            title = component.title

            val existingView = getOrNull(0)

            if (existingView != null) {
                existingView.replaceWith(component.root, ViewTransition.Slide(Duration(500.0)))
            } else {
                add(component.root)
            }
        }
    }
}