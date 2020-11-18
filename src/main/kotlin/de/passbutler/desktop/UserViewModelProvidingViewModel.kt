package de.passbutler.desktop

import de.passbutler.common.LoggedInUserResult
import de.passbutler.common.UserManager
import de.passbutler.common.UserViewModel
import de.passbutler.common.base.BindableObserver
import de.passbutler.desktop.base.BuildInformationProvider
import de.passbutler.desktop.base.CoroutineScopedViewModel
import de.passbutler.desktop.crypto.BiometricsProvider
import de.passbutler.desktop.database.createLocalRepository
import kotlinx.coroutines.launch
import tornadofx.Component
import tornadofx.FX
import java.io.File

class UserViewModelProvidingViewModel : CoroutineScopedViewModel() {

    var userManager: UserManager? = null
        private set

    var loggedInUserViewModel: UserViewModel? = null
        private set

    private val loggedInUserResultObserver = LoggedInUserResultObserver()












    // TODO: When call `cancelJobs()`?

    suspend fun initializeUserManager(vaultFile: File) {
        unregisterLoggedInUserResultObserver()
        userManager = createUserManager(vaultFile)
        registerLoggedInUserResultObserver()
    }

    private suspend fun createUserManager(vaultFile: File): UserManager {
        val databasePath = vaultFile.absolutePath
        val localRepository = createLocalRepository(databasePath)

        // TODO: Exception handling
        return UserManager(localRepository, BuildInformationProvider)
    }

    private fun registerLoggedInUserResultObserver() {
        // Initially notify observer to be sure, the `loggedInUserViewModel` is restored immediately
        userManager?.loggedInUserResult?.addObserver(this, true, loggedInUserResultObserver)
    }

    private fun unregisterLoggedInUserResultObserver() {
        userManager?.loggedInUserResult?.removeObserver(loggedInUserResultObserver)
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
                    launch {
                        loggedInUserViewModel?.clearSensibleData()
                        loggedInUserViewModel?.cancelJobs()
                        loggedInUserViewModel = null
                    }
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
