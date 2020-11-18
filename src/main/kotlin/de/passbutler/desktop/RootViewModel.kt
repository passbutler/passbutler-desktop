package de.passbutler.desktop

import de.passbutler.common.LoggedInUserResult
import de.passbutler.common.base.BindableObserver
import de.passbutler.common.base.MutableBindable
import de.passbutler.desktop.base.CoroutineScopedViewModel
import de.passbutler.desktop.base.ViewLifecycledViewModel
import de.passbutler.desktop.ui.VAULT_FILE_EXTENSION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class RootViewModel : CoroutineScopedViewModel(), ViewLifecycledViewModel, UserViewModelUsingViewModel {

    val rootScreenState = MutableBindable<RootScreenState?>(null)
    val lockScreenState = MutableBindable<LockScreenState?>(null)

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    private val loggedInUserResultObserver = LoggedInUserResultObserver()

    override fun onCleared() {
        unregisterLoggedInUserResultObserver()
        cancelJobs()
    }










    suspend fun openRecentVault() {
        // TODO: Do not hardcode
        val recentVaultFilePath = "/home/bastian/Desktop/PassButlerDatabase.sqlite"

        val recentVaultFile = withContext(Dispatchers.IO) {
            File(recentVaultFilePath)
        }

        openVault(recentVaultFile)
    }

    // TODO: close previous
    suspend fun createVault(selectedFile: File) {
        val selectedFileName = selectedFile.name

        val vaultFile = if (selectedFileName.endsWith(".$VAULT_FILE_EXTENSION")) {
            selectedFile
        } else {
            // TODO: IO
            File("$selectedFileName.$VAULT_FILE_EXTENSION")
        }

        // TODO: Create file?
        vaultFile.createNewFile()

        unregisterLoggedInUserResultObserver()
        userViewModelProvidingViewModel.initializeUserManager(vaultFile)
        registerLoggedInUserResultObserver()

        // TODO: save as recent
    }

    // TODO: close previous
    suspend fun openVault(selectedFile: File) {
        if (selectedFile.exists()) {
            unregisterLoggedInUserResultObserver()
            userViewModelProvidingViewModel.initializeUserManager(selectedFile)
            registerLoggedInUserResultObserver()

            userManager?.restoreLoggedInUser()

            // TODO: save as recent
        } else {
            rootScreenState.value = RootScreenState.LoggedOut
            lockScreenState.value = null
        }
    }

    private fun registerLoggedInUserResultObserver() {
        // Only notify observer if result was changed triggered by `restoreLoggedInUser()` call
        userManager?.loggedInUserResult?.addObserver(this, false, loggedInUserResultObserver)
    }

    private fun unregisterLoggedInUserResultObserver() {
        userManager?.loggedInUserResult?.removeObserver(loggedInUserResultObserver)
    }












































    sealed class RootScreenState {
        object LoggedIn : RootScreenState()
        object LoggedOut : RootScreenState()
    }

    sealed class LockScreenState {
        object Locked : LockScreenState()
        object Unlocked : LockScreenState()
    }

    private inner class LoggedInUserResultObserver : BindableObserver<LoggedInUserResult?> {
        override fun invoke(loggedInUserResult: LoggedInUserResult?) {
            when (loggedInUserResult) {
                is LoggedInUserResult.LoggedIn.PerformedLogin -> {
                    rootScreenState.value = RootScreenState.LoggedIn
                    lockScreenState.value = LockScreenState.Unlocked
                }
                is LoggedInUserResult.LoggedIn.RestoredLogin -> {
                    rootScreenState.value = RootScreenState.LoggedIn
                    lockScreenState.value = LockScreenState.Locked
                }
                is LoggedInUserResult.LoggedOut -> {
                    rootScreenState.value = RootScreenState.LoggedOut
                    lockScreenState.value = null
                }
            }
        }
    }
}