package de.passbutler.desktop

import de.passbutler.common.LoggedInUserResult
import de.passbutler.common.UserManager
import de.passbutler.common.UserViewModel
import de.passbutler.common.base.BindableObserver
import de.passbutler.desktop.base.BuildInformationProvider
import de.passbutler.desktop.crypto.BiometricsProvider
import de.passbutler.desktop.database.createLocalRepository
import tornadofx.Component
import tornadofx.FX
import tornadofx.ViewModel
import java.io.File

class UserViewModelProvidingViewModel : ViewModel() {

    var userManager: UserManager? = null
        private set(value) {
            if (value != field) {
                // Unregister observer from old `UserManager` instance if existing
                field?.loggedInUserResult?.removeObserver(loggedInUserResultObserver)

                field = value

                // Initially notify observer to be sure, the `loggedInUserViewModel` is restored immediately
                field?.loggedInUserResult?.addObserver(null, true, loggedInUserResultObserver)
            }
        }

    var loggedInUserViewModel: UserViewModel? = null
        private set

    private val loggedInUserResultObserver = LoggedInUserResultObserver()

    // TODO: This may be called before our observer fully logged out
    suspend fun initializeUserManager(vaultFile: File) {
        val databasePath = vaultFile.absolutePath
        val localRepository = createLocalRepository(databasePath)

        // TODO: Exception handling if file is corrupt etc.
        userManager = UserManager(localRepository, BuildInformationProvider)
    }

    private inner class LoggedInUserResultObserver : BindableObserver<LoggedInUserResult?> {
        private val biometricsProvider = BiometricsProvider()

        override fun invoke(loggedInUserResult: LoggedInUserResult?) {
            val userManager = userManager ?: throw UserManagerUninitializedException

            when (loggedInUserResult) {
                is LoggedInUserResult.LoggedIn.PerformedLogin -> {
                    loggedInUserViewModel = UserViewModel(userManager, biometricsProvider, loggedInUserResult.loggedInUser, loggedInUserResult.masterPassword)
                }
                is LoggedInUserResult.LoggedIn.RestoredLogin -> {
                    loggedInUserViewModel = UserViewModel(userManager, biometricsProvider, loggedInUserResult.loggedInUser, null)
                }
                is LoggedInUserResult.LoggedOut -> {
                    // Finally clear crypto resources and reset related jobs
                    loggedInUserViewModel?.clearSensibleData()
                    loggedInUserViewModel?.cancelJobs()
                    loggedInUserViewModel = null
                }
            }
        }
    }
}

object UserManagerUninitializedException : IllegalStateException("The UserManager is null!")
object LoggedInUserViewModelUninitializedException : IllegalStateException("The logged-in UserViewModel is null!")

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
