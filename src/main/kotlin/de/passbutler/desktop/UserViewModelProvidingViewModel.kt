package de.passbutler.desktop

import de.passbutler.common.LoggedInUserResult
import de.passbutler.common.UserManager
import de.passbutler.common.UserViewModel
import de.passbutler.common.base.BindableObserver
import de.passbutler.desktop.base.CoroutineScopedViewModel
import de.passbutler.desktop.base.ViewLifecycledViewModel
import de.passbutler.desktop.crypto.BiometricsProvider
import kotlinx.coroutines.launch

class UserViewModelProvidingViewModel : CoroutineScopedViewModel(), ViewLifecycledViewModel {

    var loggedInUserViewModel: UserViewModel? = null
        private set

    val userManager
        get() = PassButlerApplication.userManager

    private val loggedInUserResultObserver = LoggedInUserResultObserver()

    init {
        registerLoggedInUserResultObserver()
    }

    override fun onCleared() {
        unregisterLoggedInUserResultObserver()
        cancelJobs()
    }

    private fun registerLoggedInUserResultObserver() {
        // Initially notify observer to be sure, the `loggedInUserViewModel` is restored immediately
        userManager.loggedInUserResult.addObserver(this, true, loggedInUserResultObserver)
    }

    private fun unregisterLoggedInUserResultObserver() {
        userManager.loggedInUserResult.removeObserver(loggedInUserResultObserver)
    }

    private inner class LoggedInUserResultObserver : BindableObserver<LoggedInUserResult?> {
        private val biometricsProvider = BiometricsProvider()

        override fun invoke(loggedInUserResult: LoggedInUserResult?) {
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

object LoggedInUserViewModelUninitializedException : IllegalStateException("The logged-in UserViewModel is null!")

interface UserViewModelUsingViewModel {
    val userViewModelProvidingViewModel: UserViewModelProvidingViewModel

    val loggedInUserViewModel: UserViewModel?
        get() = userViewModelProvidingViewModel.loggedInUserViewModel

    val userManager: UserManager
        get() = userViewModelProvidingViewModel.userManager
}

