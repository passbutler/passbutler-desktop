package de.passbutler.desktop

import de.passbutler.common.LoggedInUserResult
import de.passbutler.common.UserViewModel
import de.passbutler.common.base.BindableObserver
import de.passbutler.desktop.base.CoroutineScopedViewModel
import de.passbutler.desktop.base.ViewLifecycledViewModel
import kotlinx.coroutines.launch
import org.tinylog.kotlin.Logger

class RootViewModel : CoroutineScopedViewModel(), ViewLifecycledViewModel, UserViewModelUsingViewModel {

    override val userViewModelProvidingViewModel by inject<UserViewModelProvidingViewModel>()

    private val loggedInUserResultObserver = LoggedInUserResultObserver()

    override fun onCleared() {
        userViewModelProvidingViewModel.onCleared()
        unregisterLoggedInUserResultObserver()
        cancelJobs()
    }

    suspend fun restoreLoggedInUser() {
        registerLoggedInUserResultObserver()

        val wasRestored = userManager.restoreLoggedInUser()

        // If the logged-in user was already restored, trigger the observers manually to initialize the view
        if (!wasRestored) {
            loggedInUserResultObserver.invoke(userManager.loggedInUserResult.value)
        }
    }

    private fun registerLoggedInUserResultObserver() {
        // Only notify observer if result was changed triggered by `restoreLoggedInUser()` call
        userManager.loggedInUserResult.addObserver(this, false, loggedInUserResultObserver)
    }

    private fun unregisterLoggedInUserResultObserver() {
        userManager.loggedInUserResult.removeObserver(loggedInUserResultObserver)
    }

    private fun restoreWebservices(loggedInUserViewModel: UserViewModel, masterPassword: String) {
        // Restore webservices asynchronously to avoid slow network is blocking unlock progress
        loggedInUserViewModel.launch {
            userManager.restoreWebservices(masterPassword)
        }
    }

    private inner class LoggedInUserResultObserver : BindableObserver<LoggedInUserResult?> {
        override fun invoke(loggedInUserResult: LoggedInUserResult?) {
            when (loggedInUserResult) {
                is LoggedInUserResult.LoggedIn.PerformedLogin -> {
                    // TODO: Implement
                    Logger.debug("PerformedLogin")
                }
                is LoggedInUserResult.LoggedIn.RestoredLogin -> {
                    // TODO: Implement
                    Logger.debug("RestoredLogin")
                }
                is LoggedInUserResult.LoggedOut -> {
                    // TODO: Implement
                    Logger.debug("LoggedOut")
                }
            }
        }
    }
}