package de.passbutler.desktop

import de.passbutler.common.UserViewModel
import de.passbutler.common.base.Failure
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import de.passbutler.common.base.resultOrThrowException
import de.passbutler.common.database.models.UserType
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.launch
import tornadofx.FX
import tornadofx.ViewModel

class LockedScreenViewModel : ViewModel(), UserViewModelUsingViewModel {

    val passwordProperty = bind { SimpleStringProperty() }

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    private val rootViewModel by injectRootViewModel()

    suspend fun unlockScreenWithPassword(masterPassword: String): Result<Unit> {
        val loggedInUserViewModel = loggedInUserViewModel ?: throw LoggedInUserViewModelUninitializedException

        return try {
            loggedInUserViewModel.decryptSensibleData(masterPassword).resultOrThrowException()

            if (loggedInUserViewModel.userType == UserType.REMOTE) {
                restoreWebservices(loggedInUserViewModel, masterPassword)
            }

            rootViewModel.rootScreenState.value = RootViewModel.RootScreenState.LoggedIn.Unlocked

            Success(Unit)
        } catch (exception: Exception) {
            Failure(exception)
        }
    }

    private fun restoreWebservices(loggedInUserViewModel: UserViewModel, masterPassword: String) {
        val userManager = userManager ?: throw UserManagerUninitializedException

        // Restore webservices asynchronously to avoid slow network is blocking unlock progress
        loggedInUserViewModel.launch {
            userManager.restoreWebservices(masterPassword)
        }
    }
}