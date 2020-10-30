package de.passbutler.desktop

import de.passbutler.common.base.Result
import kotlinx.coroutines.delay
import tornadofx.ViewModel

class OverviewViewModel : ViewModel(), UserViewModelUsingViewModel {

    override val userViewModelProvidingViewModel by inject<UserViewModelProvidingViewModel>()

    suspend fun logoutUser(): Result<Unit> {
        // Some artificial delay to look flow more natural
        delay(500)

        val loggedInUserViewModel = loggedInUserViewModel ?: throw LoggedInUserViewModelUninitializedException
        return loggedInUserViewModel.logout()
    }
}