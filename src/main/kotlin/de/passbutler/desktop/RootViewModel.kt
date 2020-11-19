package de.passbutler.desktop

import de.passbutler.common.LoggedInUserResult
import de.passbutler.common.base.BindableObserver
import de.passbutler.common.base.MutableBindable
import de.passbutler.desktop.base.CoroutineScopedViewModel
import de.passbutler.desktop.base.ViewLifecycledViewModel
import de.passbutler.desktop.ui.VAULT_FILE_EXTENSION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tornadofx.Component
import tornadofx.FX
import java.io.File

class RootViewModel : CoroutineScopedViewModel(), ViewLifecycledViewModel, UserViewModelUsingViewModel {

    val rootScreenState = MutableBindable<RootScreenState?>(null)

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    private val loggedInUserResultObserver = LoggedInUserResultObserver()

    override fun onCleared() {
        unregisterLoggedInUserResultObserver()
        userViewModelProvidingViewModel.cancelJobs()
        cancelJobs()
    }

    suspend fun restoreRecentVault() {
        // TODO: Do not hardcode
        val recentVaultFilePath = "/home/bastian/Desktop/PassButlerDatabase.sqlite"

        val recentVaultFile = withContext(Dispatchers.IO) {
            File(recentVaultFilePath)
        }

        openVault(recentVaultFile)
    }

    // TODO: close previous
    suspend fun openVault(selectedFile: File) {
        if (selectedFile.exists()) {
            initializeUserManager(selectedFile)

            userManager?.restoreLoggedInUser()

            // TODO: check if successful

            // TODO: save as recent
        } else {
            rootScreenState.value = RootScreenState.LoggedOut.Welcome
        }
    }

    // TODO: close previous
    suspend fun createVault(selectedFile: File) {
        val vaultFile = if (selectedFile.name.endsWith(".$VAULT_FILE_EXTENSION")) {
            selectedFile
        } else {
            // TODO: IO
            File("${selectedFile.absolutePath}.$VAULT_FILE_EXTENSION")
        }

        // TODO: Ensure `vaultFile` does not exists

        // TODO: Create file?
        vaultFile.createNewFile()

        initializeUserManager(vaultFile)

        // TODO: save as recent

        rootScreenState.value = RootScreenState.LoggedOut.OpeningVault
    }

    private suspend fun initializeUserManager(vaultFile: File) {
        unregisterLoggedInUserResultObserver()
        userViewModelProvidingViewModel.initializeUserManager(vaultFile)
        registerLoggedInUserResultObserver()
    }

    private fun registerLoggedInUserResultObserver() {
        // Only notify observer if result was changed triggered by `restoreLoggedInUser()` call
        userManager?.loggedInUserResult?.addObserver(this, false, loggedInUserResultObserver)
    }

    private fun unregisterLoggedInUserResultObserver() {
        userManager?.loggedInUserResult?.removeObserver(loggedInUserResultObserver)
    }

    sealed class RootScreenState {
        sealed class LoggedIn : RootScreenState() {
            object Locked : LoggedIn()
            object Unlocked : LoggedIn()
        }

        sealed class LoggedOut : RootScreenState() {
            object OpeningVault : LoggedOut()
            object Welcome : LoggedOut()
        }
    }

    private inner class LoggedInUserResultObserver : BindableObserver<LoggedInUserResult?> {
        override fun invoke(loggedInUserResult: LoggedInUserResult?) {
            when (loggedInUserResult) {
                is LoggedInUserResult.LoggedIn.PerformedLogin -> {
                    rootScreenState.value = RootScreenState.LoggedIn.Unlocked
                }
                is LoggedInUserResult.LoggedIn.RestoredLogin -> {
                    rootScreenState.value = RootScreenState.LoggedIn.Locked
                }
                is LoggedInUserResult.LoggedOut -> {
                    rootScreenState.value = RootScreenState.LoggedOut.Welcome
                }
            }
        }
    }
}

/**
 * Injects the `RootViewModel` with global scope to ensure it is always the same shared instance.
 */
inline fun <reified T> T.injectRootViewModel()
    where T : Component = inject<RootViewModel>(FX.defaultScope)
