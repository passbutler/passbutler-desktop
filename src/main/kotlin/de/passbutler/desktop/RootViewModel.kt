package de.passbutler.desktop

import de.passbutler.common.LoggedInUserResult
import de.passbutler.common.UserManager
import de.passbutler.common.base.BindableObserver
import de.passbutler.common.base.Failure
import de.passbutler.common.base.MutableBindable
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import de.passbutler.desktop.base.CoroutineScopedViewModel
import de.passbutler.desktop.base.ViewLifecycledViewModel
import de.passbutler.desktop.ui.VAULT_FILE_EXTENSION
import tornadofx.Component
import tornadofx.FX
import java.io.File

class RootViewModel : CoroutineScopedViewModel(), ViewLifecycledViewModel, UserViewModelUsingViewModel {

    val rootScreenState = MutableBindable<RootScreenState?>(null)

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    private val loggedInUserResultObserver = LoggedInUserResultObserver()

    override fun onCleared() {
        unregisterLoggedInUserResultObserver()
        cancelJobs()
    }

    suspend fun restoreRecentVault() {
        val recentVaultFile = applicationConfiguration.readValue {
            string(PassButlerConfiguration.RECENT_VAULT)
        }?.let { File(it) }?.takeIf { it.exists() }

        if (recentVaultFile != null) {
            openVault(recentVaultFile)
        } else {
            rootScreenState.value = RootScreenState.LoggedOut.Welcome
        }
    }

    suspend fun openVault(selectedFile: File): Result<Unit> {
        return when (val closeResult = closeVaultIfOpened()) {
            is Success -> {
                initializeUserManager(selectedFile)

                userManager?.restoreLoggedInUser()

                // TODO: check if successful

                // TODO: save as recent only if successful
                persistRecentVault(selectedFile)

                Success(Unit)
            }
            is Failure -> Failure(closeResult.throwable)
        }
    }

    suspend fun createVault(selectedFile: File): Result<Unit> {
        return when (val closeResult = closeVaultIfOpened()) {
            is Success -> {
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

                // TODO: save as recent only if successful
                persistRecentVault(vaultFile)

                rootScreenState.value = RootScreenState.LoggedOut.OpeningVault

                Success(Unit)
            }
            is Failure -> Failure(closeResult.throwable)
        }
    }

    private suspend fun closeVaultIfOpened(): Result<Unit> {
        return loggedInUserViewModel?.logout(UserManager.LogoutBehaviour.KeepDatabase) ?: Success(Unit)
    }

    private suspend fun initializeUserManager(vaultFile: File) {
        unregisterLoggedInUserResultObserver()
        userViewModelProvidingViewModel.initializeUserManager(vaultFile)
        registerLoggedInUserResultObserver()
    }

    private suspend fun persistRecentVault(vaultFile: File) {
        applicationConfiguration.writeValue {
            set(PassButlerConfiguration.RECENT_VAULT to vaultFile.absolutePath)
        }
    }

    private fun registerLoggedInUserResultObserver() {
        // Only notify observer if result was changed triggered by `restoreLoggedInUser()` call
        userManager?.loggedInUserResult?.addObserver(this, false, loggedInUserResultObserver)
    }

    private fun unregisterLoggedInUserResultObserver() {
        userManager?.loggedInUserResult?.removeObserver(loggedInUserResultObserver)
    }

    suspend fun closeVault(): Result<Unit> {
        val loggedInUserViewModel = loggedInUserViewModel ?: throw LoggedInUserViewModelUninitializedException
        return loggedInUserViewModel.logout(UserManager.LogoutBehaviour.KeepDatabase)
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
