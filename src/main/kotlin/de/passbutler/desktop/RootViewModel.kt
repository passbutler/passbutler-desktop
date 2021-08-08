package de.passbutler.desktop

import de.passbutler.common.LoggedInUserViewModelUninitializedException
import de.passbutler.common.UserManagerUninitializedException
import de.passbutler.common.base.Bindable
import de.passbutler.common.base.Failure
import de.passbutler.common.base.MutableBindable
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import de.passbutler.common.base.resultOrNull
import de.passbutler.common.base.resultOrThrowException
import de.passbutler.common.database.models.UserType
import de.passbutler.desktop.base.ConfigProperty
import de.passbutler.desktop.base.readConfigProperty
import de.passbutler.desktop.base.toJavaxJsonArray
import de.passbutler.desktop.base.writeConfigProperty
import de.passbutler.desktop.database.DatabaseInitializationMode
import de.passbutler.desktop.ui.VAULT_FILE_EXTENSION
import de.passbutler.desktop.ui.ensureFileExtension
import org.tinylog.kotlin.Logger
import tornadofx.Component
import tornadofx.FX
import tornadofx.ViewModel
import java.io.File
import javax.json.JsonString
import javax.json.JsonValue.EMPTY_JSON_ARRAY

class RootViewModel : ViewModel(), UserViewModelUsingViewModel {

    val rootScreenState: Bindable<RootScreenState?>
        get() = _rootScreenState

    val recentVaultFiles: Bindable<List<File>>
        get() = _recentVaultFiles

    private val _rootScreenState = MutableBindable<RootScreenState?>(null)
    private val _recentVaultFiles = MutableBindable<List<File>>(emptyList())

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    suspend fun restoreRecentVault() {
        val recentVaultFiles = restoreRecentVaultFiles()
        _recentVaultFiles.value = recentVaultFiles

        // Only try existence of most recent file (don't filter non-existing) to avoid opening old/unexpected files
        val mostRecentVaultFile = recentVaultFiles.firstOrNull()?.takeIf { it.exists() }
        Logger.debug("Restore recent vault file '$mostRecentVaultFile'")

        if (mostRecentVaultFile != null) {
            when (val openResult = openVault(mostRecentVaultFile)) {
                is Success -> {
                    Logger.debug("The recent vault file was opened")
                }
                is Failure -> {
                    Logger.debug(openResult.throwable, "The recent vault file could not be opened")
                }
            }
        } else {
            Logger.debug("No recent file available to open")
            _rootScreenState.value = RootScreenState.LoggedOut.Introduction
        }
    }

    suspend fun resetRecentVaultFiles(): Result<Unit> {
        val writeConfigurationResult = app.writeConfigProperty {
            set(ConfigProperty.RECENT_VAULTS to EMPTY_JSON_ARRAY)
        }

        if (writeConfigurationResult is Success) {
            _recentVaultFiles.value = emptyList()
        }

        return writeConfigurationResult
    }

    suspend fun openVault(selectedFile: File): Result<Unit> {
        Logger.debug("Open vault file '$selectedFile'")

        return when (val closeResult = closeVaultIfOpen()) {
            is Success -> {
                try {
                    userViewModelProvidingViewModel.initializeUserManager(selectedFile, DatabaseInitializationMode.Open).resultOrThrowException()
                    userViewModelProvidingViewModel.restoreLoggedInUser().resultOrThrowException()

                    persistRecentVaultFiles(selectedFile)
                    _rootScreenState.value = RootScreenState.LoggedIn.Locked

                    Success(Unit)
                } catch (exception: Exception) {
                    _rootScreenState.value = RootScreenState.LoggedOut.Introduction
                    Failure(exception)
                }
            }
            is Failure -> Failure(closeResult.throwable)
        }
    }

    suspend fun createVault(username: String, masterPassword: String, selectedFile: File): Result<Unit> {
        Logger.debug("Create vault file '$selectedFile'")

        return when (val closeResult = closeVaultIfOpen()) {
            is Success -> {
                val vaultFile = selectedFile.ensureFileExtension(VAULT_FILE_EXTENSION)

                if (vaultFile.exists()) {
                    Failure(VaultFileAlreadyExistsException)
                } else {
                    when (val initializeResult = userViewModelProvidingViewModel.initializeUserManager(vaultFile, DatabaseInitializationMode.Create)) {
                        is Success -> {
                            val serverUrlString = null

                            when (val loginResult = userViewModelProvidingViewModel.loginUser(serverUrlString, username, masterPassword)) {
                                is Success -> {
                                    persistRecentVaultFiles(vaultFile)
                                    _rootScreenState.value = RootScreenState.LoggedIn.Unlocked
                                    Success(Unit)
                                }
                                is Failure -> Failure(loginResult.throwable)
                            }
                        }
                        is Failure -> {
                            _rootScreenState.value = RootScreenState.LoggedOut.Introduction
                            Failure(initializeResult.throwable)
                        }
                    }
                }
            }
            is Failure -> Failure(closeResult.throwable)
        }
    }

    suspend fun loginVault(serverUrlString: String, username: String, masterPassword: String): Result<Unit> {
        Logger.debug("Login current vault")

        return when (val loginResult = userViewModelProvidingViewModel.loginUser(serverUrlString, username, masterPassword)) {
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
        Logger.debug("Close current vault")

        return when (val logoutResult = userViewModelProvidingViewModel.logoutUser()) {
            is Success -> {
                _rootScreenState.value = RootScreenState.LoggedOut.Introduction
                Success(Unit)
            }
            is Failure -> Failure(logoutResult.throwable)
        }
    }

    private suspend fun restoreRecentVaultFiles(): List<File> {
        return app.readConfigProperty {
            jsonArray(ConfigProperty.RECENT_VAULTS)
                ?.getValuesAs(JsonString::getString)
                ?.map { absolutePath ->
                    File(absolutePath)
                }
        }.resultOrNull()?.take(MAXIMUM_RECENT_VAULT_FILES) ?: emptyList()
    }

    private suspend fun persistRecentVaultFiles(newVaultFile: File) {
        val oldRecentVaultFiles = restoreRecentVaultFiles()

        // Temporarily convert to set to remove duplicates
        val newRecentVaultFiles = oldRecentVaultFiles.toMutableList().apply {
            add(0, newVaultFile)
        }.toSet().toList()

        _recentVaultFiles.value = newRecentVaultFiles

        val persistResult = app.writeConfigProperty {
            val serializableRecentVaultFiles = newRecentVaultFiles.map { it.absolutePath }
            set(ConfigProperty.RECENT_VAULTS to serializableRecentVaultFiles.toJavaxJsonArray())
        }

        if (persistResult is Failure) {
            Logger.warn("The recent vault files could not be persisted!")
        }
    }

    private suspend fun closeVaultIfOpen(): Result<Unit> {
        Logger.debug("Close current vault if opened")

        return when (val logoutResult = userViewModelProvidingViewModel.logoutUser()) {
            is Success -> Success(Unit)
            is Failure -> {
                // Ignore missing `UserManager` if it is tried to close vault initially when open/create vault
                if (logoutResult.throwable is UserManagerUninitializedException) {
                    Success(Unit)
                } else {
                    Failure(logoutResult.throwable)
                }
            }
        }
    }

    sealed class RootScreenState {
        sealed class LoggedIn : RootScreenState() {
            object Locked : LoggedIn()
            object Unlocked : LoggedIn()
        }

        sealed class LoggedOut : RootScreenState() {
            object Introduction : LoggedOut()
        }
    }

    companion object {
        private const val MAXIMUM_RECENT_VAULT_FILES = 10
    }
}

object VaultFileAlreadyExistsException : IllegalArgumentException("The selected file to create vault already exists!")

/**
 * Injects the `RootViewModel` with global scope to ensure it is always the same shared instance.
 */
inline fun <reified T> T.injectRootViewModel()
    where T : Component = inject<RootViewModel>(FX.defaultScope)
