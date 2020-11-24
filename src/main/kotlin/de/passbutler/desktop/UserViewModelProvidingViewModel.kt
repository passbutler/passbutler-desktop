package de.passbutler.desktop

import de.passbutler.common.UserManager
import de.passbutler.common.UserManagerUninitializedException
import de.passbutler.common.UserViewModel
import de.passbutler.common.base.Failure
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import de.passbutler.desktop.base.BuildInformationProvider
import de.passbutler.desktop.crypto.BiometricsProvider
import de.passbutler.desktop.database.DatabaseInitializationMode
import de.passbutler.desktop.database.createLocalRepository
import org.tinylog.kotlin.Logger
import tornadofx.Component
import tornadofx.FX
import tornadofx.ViewModel
import java.io.File

class UserViewModelProvidingViewModel : ViewModel() {

    var loggedInUserViewModel: UserViewModel? = null
        private set

    var userManager: UserManager? = null
        private set

    private val biometricsProvider = BiometricsProvider()

    suspend fun initializeUserManager(vaultFile: File, mode: DatabaseInitializationMode): Result<Unit> {
        return try {
            val localRepository = createLocalRepository(vaultFile, mode)
            userManager = UserManager(localRepository, BuildInformationProvider)

            Success(Unit)
        } catch (exception: Exception) {
            Failure(exception)
        }
    }

    suspend fun restoreLoggedInUser(): Result<Unit> {
        val userManager = userManager ?: throw UserManagerUninitializedException
        val restoreResult = userManager.restoreLoggedInUser()

        return when (restoreResult) {
            is Success -> {
                val loggedInUserResult = restoreResult.result
                loggedInUserViewModel = UserViewModel(userManager, biometricsProvider, loggedInUserResult.loggedInUser)

                Success(Unit)
            }
            is Failure -> Failure(restoreResult.throwable)
        }
    }

    suspend fun loginUser(serverUrlString: String?, username: String, masterPassword: String): Result<Unit> {
        val userManager = userManager ?: throw UserManagerUninitializedException

        val loginResult = when (serverUrlString) {
            null -> userManager.loginLocalUser(username, masterPassword)
            else -> userManager.loginRemoteUser(username, masterPassword, serverUrlString)
        }

        return when (loginResult) {
            is Success -> {
                val loggedInUserResult = loginResult.result
                val newLoggedInUserViewModel = UserViewModel(userManager, biometricsProvider, loggedInUserResult.loggedInUser)

                val decryptSensibleDataResult = newLoggedInUserViewModel.decryptSensibleData(masterPassword)

                when (decryptSensibleDataResult) {
                    is Success -> {
                        loggedInUserViewModel = newLoggedInUserViewModel
                        Success(Unit)
                    }
                    is Failure -> {
                        Logger.warn(decryptSensibleDataResult.throwable, "The initial unlock of the resources after login failed!")
                        Failure(decryptSensibleDataResult.throwable)
                    }
                }
            }
            is Failure -> Failure(loginResult.throwable)
        }
    }

    suspend fun logoutUser(): Result<Unit> {
        // TODO: On initial open vault call, it is tried to logout before the `UserManager` is initialized
        val userManager = userManager ?: throw UserManagerUninitializedException
        val logoutResult = userManager.logoutUser(UserManager.LogoutBehaviour.KeepDatabase)

        return when (logoutResult) {
            is Success -> {
                loggedInUserViewModel?.clearSensibleData()
                loggedInUserViewModel?.cancelJobs()
                loggedInUserViewModel = null

                Success(Unit)
            }
            is Failure -> Failure(logoutResult.throwable)
        }
    }
}

interface UserViewModelUsingViewModel {
    val userViewModelProvidingViewModel: UserViewModelProvidingViewModel

    val loggedInUserViewModel: UserViewModel?
        get() = userViewModelProvidingViewModel.loggedInUserViewModel

    val userManager: UserManager?
        get() = userViewModelProvidingViewModel.userManager
}

/**
 * Injects the `UserViewModelProvidingViewModel` with global scope to ensure it is always the same shared instance.
 */
inline fun <reified T> T.injectUserViewModelProvidingViewModel()
    where T : UserViewModelUsingViewModel,
          T : Component = inject<UserViewModelProvidingViewModel>(FX.defaultScope)
