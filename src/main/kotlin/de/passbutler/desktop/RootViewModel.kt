package de.passbutler.desktop

import de.passbutler.common.LoggedInUserViewModelUninitializedException
import de.passbutler.common.base.Bindable
import de.passbutler.common.base.Failure
import de.passbutler.common.base.MutableBindable
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import de.passbutler.common.base.resultOrThrowException
import de.passbutler.common.database.models.UserType
import de.passbutler.desktop.database.DatabaseInitializationMode
import de.passbutler.desktop.ui.VAULT_FILE_EXTENSION
import de.passbutler.desktop.ui.ensureFileExtension
import org.tinylog.kotlin.Logger
import tornadofx.Component
import tornadofx.FX
import tornadofx.ViewModel
import java.io.File

class RootViewModel : ViewModel(), UserViewModelUsingViewModel {

    val rootScreenState: Bindable<RootScreenState?>
        get() = _rootScreenState

    private val _rootScreenState = MutableBindable<RootScreenState?>(null)

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    suspend fun restoreRecentVault() {
        val recentVaultFile = restoreRecentVaultFile()
        Logger.debug("Restore recent vault file recentVaultFile='$recentVaultFile'")

        val openResult = recentVaultFile?.let {
            openVault(recentVaultFile)
        } ?: Failure(NoRecentVaultFileAvailableException)

        when (openResult) {
            is Success -> {
                Logger.debug("The recent vault file was opened")
            }
            is Failure -> {
                val exceptionMessage = openResult.throwable.message
                Logger.debug("The recent vault file could not be opened: $exceptionMessage")

                _rootScreenState.value = RootScreenState.LoggedOut.Welcome
            }
        }
    }

    suspend fun openVault(selectedFile: File): Result<Unit> {
        Logger.debug("Open vault file '$selectedFile'")

        return when (val closeResult = closeVault()) {
            is Success -> {
                try {
                    userViewModelProvidingViewModel.initializeUserManager(selectedFile, DatabaseInitializationMode.Open).resultOrThrowException()
                    userViewModelProvidingViewModel.restoreLoggedInUser().resultOrThrowException()

                    persistRecentVaultFile(selectedFile)
                    _rootScreenState.value = RootScreenState.LoggedIn.Locked

                    Success(Unit)
                } catch (exception: Exception) {
                    Failure(exception)
                }
            }
            is Failure -> Failure(closeResult.throwable)
        }
    }

    suspend fun createVault(selectedFile: File): Result<Unit> {
        Logger.debug("Create vault file '$selectedFile'")

        return when (val closeResult = closeVault()) {
            is Success -> {
                val vaultFile = selectedFile.ensureFileExtension(VAULT_FILE_EXTENSION)

                if (vaultFile.exists()) {
                    Failure(VaultFileAlreadyExistsException)
                } else {
                    val initializeResult = userViewModelProvidingViewModel.initializeUserManager(vaultFile, DatabaseInitializationMode.Create)

                    when (initializeResult) {
                        is Success -> {
                            persistRecentVaultFile(vaultFile)
                            _rootScreenState.value = RootScreenState.LoggedOut.OpeningVault

                            Success(Unit)
                        }
                        is Failure -> Failure(initializeResult.throwable)
                    }
                }
            }
            is Failure -> Failure(closeResult.throwable)
        }
    }

    suspend fun loginVault(serverUrlString: String?, username: String, masterPassword: String): Result<Unit> {
        Logger.debug("Login current vault")

        val loginResult = userViewModelProvidingViewModel.loginUser(serverUrlString, username, masterPassword)

        return when (loginResult) {
            is Success -> {
                _rootScreenState.value = RootScreenState.LoggedIn.Unlocked
                Success(Unit)
            }
            is Failure -> Failure(loginResult.throwable)
        }
    }

    suspend fun unlockVaultWithPassword(masterPassword: String): Result<Unit> {
        Logger.debug("Unlock current vault with password")

        val loggedInUserViewModel = loggedInUserViewModel ?: throw LoggedInUserViewModelUninitializedException

        return try {
            loggedInUserViewModel.decryptSensibleData(masterPassword).resultOrThrowException()

            if (loggedInUserViewModel.userType == UserType.REMOTE) {
                loggedInUserViewModel.restoreWebservices(masterPassword)
            }

            _rootScreenState.value = RootScreenState.LoggedIn.Unlocked

            Success(Unit)
        } catch (exception: Exception) {
            Failure(exception)
        }
    }

    suspend fun closeVault(): Result<Unit> {
        Logger.debug("Close current vault if opened")

        val logoutResult = userViewModelProvidingViewModel.logoutUser()

        return when (logoutResult) {
            is Success -> {
                _rootScreenState.value = RootScreenState.LoggedOut.Welcome
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

object NoRecentVaultFileAvailableException : IllegalArgumentException("No recent file available to open!")
object VaultFileAlreadyExistsException : IllegalArgumentException("The selected file to create vault already exists!")

/**
 * Injects the `RootViewModel` with global scope to ensure it is always the same shared instance.
 */
inline fun <reified T> T.injectRootViewModel()
    where T : Component = inject<RootViewModel>(FX.defaultScope)
