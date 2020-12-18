package de.passbutler.desktop

import de.passbutler.common.LoggedInUserViewModelUninitializedException
import de.passbutler.common.base.Result
import tornadofx.ViewModel

class ChangeMasterPasswordViewModel : ViewModel(), UserViewModelUsingViewModel {
    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    suspend fun changeMasterPassword(oldMasterPassword: String, newMasterPassword: String): Result<Unit> {
        val loggedInUserViewModel = loggedInUserViewModel ?: throw LoggedInUserViewModelUninitializedException
        return loggedInUserViewModel.updateMasterPassword(oldMasterPassword, newMasterPassword)
    }
}