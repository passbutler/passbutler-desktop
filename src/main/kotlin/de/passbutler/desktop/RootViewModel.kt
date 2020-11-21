package de.passbutler.desktop

import de.passbutler.common.LoggedInUserResult
import de.passbutler.common.UserManager
import de.passbutler.common.base.BindableObserver
import de.passbutler.common.base.Failure
import de.passbutler.common.base.MutableBindable
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import de.passbutler.desktop.base.CoroutineScopedViewModel
import de.passbutler.desktop.base.PathProvider
import de.passbutler.desktop.base.ViewLifecycledViewModel
import de.passbutler.desktop.ui.VAULT_FILE_EXTENSION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
        cancelJobs()
    }

    suspend fun restoreRecentVault() {
        // TODO: IO Exceptions
        val configurationFile = PathProvider.obtainFile { configurationFile }

        val recentVaultFile = if (configurationFile.exists()) {
            val configurationFileContent = configurationFile.readText()
            val configuration = PassButlerConfiguration.Deserializer.deserializeOrNull(configurationFileContent)

            val mostRecentVaultFile = configuration?.recentVaultFiles?.lastOrNull()

            val recentVaultFile = mostRecentVaultFile?.let {
                withContext(Dispatchers.IO) { File(it) }.takeIf { it.exists() }
            }

            recentVaultFile
        } else {
            null
        }

        if (recentVaultFile != null) {
            openVault(recentVaultFile)
        } else {
            rootScreenState.value = RootScreenState.LoggedOut.Welcome
        }
    }

    private suspend fun appendRecentVault(vaultFile: File) {
        val configurationFile = PathProvider.obtainFile { configurationFile }

        val configuration = if (configurationFile.exists()) {
            val configurationFileContent = configurationFile.readText()
            PassButlerConfiguration.Deserializer.deserializeOrNull(configurationFileContent)
        } else {
            withContext(Dispatchers.IO) {
                configurationFile.createNewFile()
            }

            null
        }

        val updatedRecentVaultFiles = (configuration?.recentVaultFiles ?: emptyList()) + vaultFile.absolutePath

        val updatedConfiguration = configuration?.copy(
            recentVaultFiles = updatedRecentVaultFiles
        ) ?: PassButlerConfiguration(updatedRecentVaultFiles)

        configurationFile.writeText(updatedConfiguration.serialize().toString())
    }

    suspend fun openVault(selectedFile: File): Result<Unit> {
        return when (val closeResult = closeVaultIfOpened()) {
            is Success -> {
                initializeUserManager(selectedFile)

                userManager?.restoreLoggedInUser()

                // TODO: check if successful

                // TODO: save as recent only if successful
                appendRecentVault(selectedFile)

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
                appendRecentVault(vaultFile)

                rootScreenState.value = RootScreenState.LoggedOut.OpeningVault

                Success(Unit)
            }
            is Failure -> Failure(closeResult.throwable)
        }
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

    private suspend fun closeVaultIfOpened(): Result<Unit> {
        return loggedInUserViewModel?.logout(UserManager.LogoutBehaviour.KeepDatabase) ?: Success(Unit)
    }

    suspend fun closeVault(): Result<Unit> {
        // Some artificial delay to look flow more natural
        delay(500)

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
