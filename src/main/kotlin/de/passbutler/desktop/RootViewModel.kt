package de.passbutler.desktop

import de.passbutler.common.UserManager
import de.passbutler.common.base.Failure
import de.passbutler.common.base.MutableBindable
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import de.passbutler.common.base.resultOrThrowException
import de.passbutler.desktop.ui.VAULT_FILE_EXTENSION
import de.passbutler.desktop.ui.ensureFileExtension
import tornadofx.Component
import tornadofx.FX
import tornadofx.ViewModel
import java.io.File

class RootViewModel : ViewModel(), UserViewModelUsingViewModel {

    val rootScreenState = MutableBindable<RootScreenState?>(null)

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    suspend fun restoreRecentVault() {
        val recentVaultFile = restoreRecentVaultFile()

        if (recentVaultFile != null) {
            openVault(recentVaultFile)
        } else {
            rootScreenState.value = RootScreenState.LoggedOut.Welcome
        }
    }

    suspend fun openVault(selectedFile: File): Result<Unit> {
        return when (val closeResult = closeVault()) {
            is Success -> {
                try {
                    userViewModelProvidingViewModel.initializeUserManager(selectedFile).resultOrThrowException()
                    userViewModelProvidingViewModel.restoreLoggedInUser().resultOrThrowException()

                    persistRecentVaultFile(selectedFile)
                    rootScreenState.value = RootScreenState.LoggedIn.Locked

                    Success(Unit)
                } catch (exception: Exception) {
                    Failure(exception)
                }
            }
            is Failure -> Failure(closeResult.throwable)
        }
    }

    suspend fun createVault(selectedFile: File): Result<Unit> {
        return when (val closeResult = closeVault()) {
            is Success -> {
                val vaultFile = selectedFile.ensureFileExtension(VAULT_FILE_EXTENSION)

                if (vaultFile.exists()) {
                    Failure(VaultFileAlreadyExistsException)
                } else {
                    val initializeResult = userViewModelProvidingViewModel.initializeUserManager(selectedFile)

                    when (initializeResult) {
                        is Success -> {
                            persistRecentVaultFile(vaultFile)
                            rootScreenState.value = RootScreenState.LoggedOut.OpeningVault

                            Success(Unit)
                        }
                        is Failure -> Failure(initializeResult.throwable)
                    }
                }
            }
            is Failure -> Failure(closeResult.throwable)
        }
    }

    suspend fun closeVault(): Result<Unit> {
        val logoutResult = userViewModelProvidingViewModel.logoutUser(UserManager.LogoutBehaviour.KeepDatabase)

        return when (logoutResult) {
            is Success -> {
                rootScreenState.value = RootScreenState.LoggedOut.Welcome
                Success(Unit)
            }
            is Failure -> Failure(logoutResult.throwable)
        }
    }

    private suspend fun restoreRecentVaultFile(): File? {
        return applicationConfiguration.readValue {
            string(PassButlerConfiguration.RECENT_VAULT)
        }?.let { File(it) }?.takeIf { it.exists() }
    }

    private suspend fun persistRecentVaultFile(vaultFile: File) {
        applicationConfiguration.writeValue {
            set(PassButlerConfiguration.RECENT_VAULT to vaultFile.absolutePath)
        }
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
}

object VaultFileAlreadyExistsException : IllegalArgumentException("The selected file to create vault already exists!")

/**
 * Injects the `RootViewModel` with global scope to ensure it is always the same shared instance.
 */
inline fun <reified T> T.injectRootViewModel()
    where T : Component = inject<RootViewModel>(FX.defaultScope)
