package de.passbutler.desktop

import de.passbutler.common.base.Result
import tornadofx.ViewModel

class LoginViewModel : ViewModel() {

    private val rootViewModel by injectRootViewModel()

    suspend fun loginUser(serverUrlString: String?, username: String, masterPassword: String): Result<Unit> {
        return rootViewModel.loginVault(serverUrlString, username, masterPassword)
    }
}
