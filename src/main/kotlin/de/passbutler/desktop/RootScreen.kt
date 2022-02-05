package de.passbutler.desktop

import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.BannerView
import de.passbutler.desktop.ui.BaseView
import de.passbutler.desktop.ui.DarkTheme
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.UIPresenter
import de.passbutler.desktop.ui.addLifecycleObserver
import de.passbutler.desktop.ui.bind
import de.passbutler.desktop.ui.bottomDropShadow
import de.passbutler.desktop.ui.jfxSpinner
import de.passbutler.desktop.ui.showConfirmDialog
import de.passbutler.desktop.ui.showOpenVaultFileChooser
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
import tornadofx.separator
import tornadofx.stackpane
import tornadofx.top
import java.io.File
import kotlin.reflect.KClass

class RootScreen : BaseView(), RequestSending {

    override val root = borderpane()

    lateinit var contentContainer: Node
        private set

    lateinit var progressView: Node
        private set

    lateinit var bannerView: BannerView
        private set

    lateinit var dialogContainerView: StackPane
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
                    dialogContainerView = createDialogContainerView()
                }
            }

            // Draw afterwards to apply drop shadow over content area
            top {
                menubar {
                    // Enforce dark theme to menu view because it should look always dark
                    addStylesheet(DarkTheme::class)

                    effect = bottomDropShadow()

                    menuView = menu(messages["general_app_name"])
                }
            }
        }
    }

    private fun StackPane.createProgressView(): Node {
        return hbox(alignment = Pos.CENTER) {
            addClass(Theme.backgroundScrimProgressStyle)

            jfxSpinner()

            isVisible = false
        }
    }

    private fun StackPane.createBannerView(): BannerView {
        return snackbarBannerView(this)
    }

    private fun StackPane.createDialogContainerView(): StackPane {
        return stackpane {
            addClass(Theme.backgroundScrimDialogStyle)

            isVisible = false
        }
    }

    override fun onDock() {
        super.onDock()

        uiPresentingDelegate = UIPresenter(this)

        bind(this, viewModel.rootScreenState, viewModel.recentVaultFiles) { rootScreenState, recentVaultFiles ->
            updateMenu(rootScreenState, recentVaultFiles)
        }

        // Do not notify-on-register because it's triggered when open/restore vault
        viewModel.rootScreenState.addLifecycleObserver(this, false) { rootScreenState ->
            updateRootScreen(rootScreenState)
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

    private fun updateMenu(rootScreenState: RootViewModel.RootScreenState?, recentVaultFiles: List<File>) {
        menuView?.apply {
            items.clear()

            setupOpenVaultItem()
            setupRecentVaultsItem(recentVaultFiles)
            setupCloseVaultItem(rootScreenState)
            setupCloseApplicationItem()
        }
    }

    private fun Menu.setupOpenVaultItem() {
        item(messages["menu_open_vault"], "Shortcut+O").action {
            openVaultChooserClicked()
        }
    }

    private fun Menu.setupRecentVaultsItem(recentVaultFiles: List<File>) {
        if (recentVaultFiles.isNotEmpty()) {
            menu(messages["menu_recent_vaults"]) {
                recentVaultFiles.forEach {
                    item(it.absolutePath).action {
                        openRecentVaultClicked(it)
                    }
                }

                separator()

                item(messages["menu_recent_vaults_clear"]).action {
                    resetRecentVaultFilesClicked()
                }
            }
        }
    }

    private fun Menu.setupCloseVaultItem(rootScreenState: RootViewModel.RootScreenState?) {
        if (rootScreenState is RootViewModel.RootScreenState.LoggedIn) {
            item(messages["menu_close_vault"], "Shortcut+W").action {
                closeVaultClicked()
            }
        }
    }

    private fun Menu.setupCloseApplicationItem() {
        item(messages["menu_close_application"], "Shortcut+Q").action {
            closeApplicationClicked()
        }
    }

    private fun openVaultChooserClicked() {
        if (viewModel.rootScreenState.value is RootViewModel.RootScreenState.LoggedIn.Unlocked) {
            showCloseVaultConfirmDialog {
                chooseVault()
            }
        } else {
            chooseVault()
        }
    }

    private fun chooseVault() {
        showOpenVaultFileChooser(messages["menu_open_vault"]) { chosenFile ->
            openVault(chosenFile)
        }
    }

    private fun openVault(vaultFile: File) {
        launchRequestSending(
            handleFailure = { showError(messages["general_open_vault_failed_title"]) }
        ) {
            viewModel.openVault(vaultFile)
        }
    }

    private fun openRecentVaultClicked(vaultFile: File) {
        if (viewModel.rootScreenState.value is RootViewModel.RootScreenState.LoggedIn.Unlocked) {
            showCloseVaultConfirmDialog {
                openVault(vaultFile)
            }
        } else {
            openVault(vaultFile)
        }
    }

    private fun resetRecentVaultFilesClicked() {
        launch {
            viewModel.resetRecentVaultFiles()
        }
    }

    private fun closeVaultClicked() {
        if (viewModel.rootScreenState.value is RootViewModel.RootScreenState.LoggedIn.Unlocked) {
            showCloseVaultConfirmDialog {
                closeVault()
            }
        } else {
            closeVault()
        }
    }

    private fun closeVault() {
        launchRequestSending(
            handleFailure = { showError(messages["overview_logout_failed_title"]) }
        ) {
            viewModel.closeVault()
        }
    }

    private fun closeApplicationClicked() {
        Platform.exit()
    }

    private fun updateRootScreen(rootScreenState: RootViewModel.RootScreenState?) {
        Logger.debug("Show screen state '$rootScreenState'")

        when (rootScreenState) {
            is RootViewModel.RootScreenState.LoggedIn.Locked -> showScreenIfNotShown(LockedScreen::class)
            is RootViewModel.RootScreenState.LoggedIn.Unlocked -> showScreenIfNotShown(OverviewScreen::class)
            is RootViewModel.RootScreenState.LoggedOut.Introduction -> showScreenIfNotShown(IntroductionScreen::class)
            null -> {
                // Nothing to do
            }
        }
    }

    private fun <T : UIComponent> showScreenIfNotShown(screenClass: KClass<T>) {
        if (!isScreenShown(screenClass)) {
            // Show unanimated because when showing screens non-user-triggered very quickly, the transition animation gets out-of-sync
            showScreen(screenClass, userTriggered = false)
        }
    }

    override fun onUndock() {
        super.onUndock()

        Logger.debug("RootScreen was undocked")
    }

    private fun showCloseVaultConfirmDialog(positiveClickAction: () -> Unit) {
        showConfirmDialog(
            title = messages["general_close_vault_confirmation_title"],
            message = messages["general_close_vault_confirmation_message"],
            positiveActionTitle = messages["general_confirm"],
            positiveClickAction = positiveClickAction
        )
    }
}
