package de.passbutler.desktop

import de.passbutler.common.LoggedInUserViewModelUninitializedException
import de.passbutler.common.base.Failure
import de.passbutler.common.base.Result
import tornadofx.ViewModel

class RegisterLocalUserViewModel : ViewModel(), UserViewModelUsingViewModel {
    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    suspend fun registerLocalUser(serverUrlString: String, invitationCode: String, masterPassword: String): Result<Unit> {
        val loggedInUserViewModel = loggedInUserViewModel ?: throw LoggedInUserViewModelUninitializedException

        // Check the given master password locally to avoid the auth webservice is initialized with non-working authentication
        val masterPasswordTestResult = loggedInUserViewModel.decryptMasterEncryptionKey(masterPassword)

        return if (masterPasswordTestResult is Failure) {
            masterPasswordTestResult
        } else {
            loggedInUserViewModel.registerLocalUser(serverUrlString, invitationCode, masterPassword)
        }
    }
}
