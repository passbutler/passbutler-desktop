package de.passbutler.desktop

import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.BannerView
import de.passbutler.desktop.ui.BaseView
import de.passbutler.desktop.ui.DarkTheme
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.UIPresenter
import de.passbutler.desktop.ui.addLifecycleObserver
import de.passbutler.desktop.ui.bottomDropShadow
import de.passbutler.desktop.ui.showOpenVaultFileChooser
import de.passbutler.desktop.ui.showSaveVaultFileChooser
import de.passbutler.desktop.ui.snackbarBannerView
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Menu
import javafx.scene.layout.StackPane
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tinylog.kotlin.Logger
import tornadofx.UIComponent
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
import tornadofx.progressindicator
import tornadofx.stackpane
import tornadofx.top
import java.io.File
import kotlin.reflect.KClass

class RootScreen : BaseView(), RequestSending {

    override val root = borderpane()

    var contentContainer: Node? = null
        private set

    var progressView: Node? = null
        private set

    var bannerView: BannerView? = null
        private set

    private var menuView: Menu? = null

    private val viewModel by injectRootViewModel()

    init {
        with(root) {
            center {
                stackpane {
                    contentContainer = stackpane()
                    progressView = createProgressView()
                    bannerView = createBannerView()
                }
            }

            // Draw afterwards to apply drop shadow over content area
            top {
                menubar {
                    // Enforce dark theme to menu view because it should look always dark
                    addStylesheet(DarkTheme::class)

                    effect = bottomDropShadow()

                    menuView = menu(messages["general_app_name"])
                    updateMenu()
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()

        uiPresentingDelegate = UIPresenter(this)

        viewModel.rootScreenState.addLifecycleObserver(this, false) {
            updateMenu()
            updateRootScreen()
        }

        launch {
            val launchArgumentVaultFilePath = app.parameters.raw.firstOrNull()
            Logger.debug("launchArgumentVaultFilePath = $launchArgumentVaultFilePath")

            val launchArgumentVaultFile = withContext(Dispatchers.IO) {
                launchArgumentVaultFilePath?.let { File(it) }?.takeIf { it.exists() }
            }

            if (launchArgumentVaultFile != null) {
                viewModel.openVault(launchArgumentVaultFile)
            } else {
                viewModel.restoreRecentVault()
            }
        }
    }

    override fun onUndock() {
        super.onUndock()

        Logger.debug("RootScreen was undocked")
    }

    private fun StackPane.createProgressView(): Node {
        return hbox(alignment = Pos.CENTER) {
            addClass(Theme.scrimBackgroundStyle)

            progressindicator()

            isVisible = false
        }
    }

    private fun StackPane.createBannerView(): BannerView {
        return snackbarBannerView(this)
    }

    private fun updateMenu() {
        menuView?.apply {
            items.clear()

            item(messages["menu_create_vault"]).action {
                createVaultClicked()
            }

            item(messages["menu_open_vault"]).action {
                openVaultClicked()
            }

            if (viewModel.rootScreenState.value is RootViewModel.RootScreenState.LoggedIn) {
                item(messages["menu_close_vault"]).action {
                    closeVaultClicked()
                }
            }

            item(messages["menu_close_application"]).action {
                closeApplicationClicked()
            }
        }
    }

    private fun createVaultClicked() {
        showSaveVaultFileChooser(messages["menu_create_vault"]) { chosenFile ->
            launchRequestSending(
                handleFailure = {
                    val errorStringResourceId = when (it) {
                        is VaultFileAlreadyExistsException -> "general_create_vault_failed_already_existing_title"
                        else -> "general_create_vault_failed_title"
                    }

                    showError(messages[errorStringResourceId])
                }
            ) {
                viewModel.createVault(chosenFile)
            }
        }
    }

    private fun openVaultClicked() {
        showOpenVaultFileChooser(messages["menu_open_vault"]) { chosenFile ->
            launchRequestSending(
                handleFailure = { showError(messages["general_open_vault_failed_title"]) }
            ) {
                viewModel.openVault(chosenFile)
            }
        }
    }

    private fun closeVaultClicked() {
        launchRequestSending(
            handleFailure = { showError(messages["overview_logout_failed_title"]) }
        ) {
            viewModel.closeVault()
        }
    }

    private fun closeApplicationClicked() {
        Platform.exit()
    }

    private fun updateRootScreen() {
        val rootScreenState = viewModel.rootScreenState.value
        Logger.debug("Show screen state '$rootScreenState'")

        when (rootScreenState) {
            is RootViewModel.RootScreenState.LoggedIn.Locked -> showScreenIfNotShown(LockedScreen::class)
            is RootViewModel.RootScreenState.LoggedIn.Unlocked -> showScreenIfNotShown(OverviewScreen::class)
            is RootViewModel.RootScreenState.LoggedOut.Welcome -> showScreenIfNotShown(WelcomeScreen::class)
            is RootViewModel.RootScreenState.LoggedOut.OpeningVault -> showScreenIfNotShown(LoginScreen::class)
        }
    }

    private fun <T : UIComponent> showScreenIfNotShown(screenClass: KClass<T>) {
        if (!isScreenShown(screenClass)) {
            // Show unanimated because when showing screens non-user-triggered very quickly, the transition animation gets out-of-sync
            showScreen(screenClass, userTriggered = false)
        }
    }
}