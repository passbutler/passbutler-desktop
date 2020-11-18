package de.passbutler.desktop

import de.passbutler.common.base.Result
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.ViewModel

class LoginViewModel : ViewModel(), UserViewModelUsingViewModel {

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    val serverUrlProperty = bind { SimpleStringProperty() }
    val usernameProperty = bind { SimpleStringProperty() }
    val passwordProperty = bind { SimpleStringProperty() }
    val isLocalLoginProperty = bind { SimpleBooleanProperty() }

    suspend fun loginUser(serverUrlString: String?, username: String, masterPassword: String): Result<Unit> {
        val userManager = userManager ?: throw UserManagerUninitializedException

        return when (serverUrlString) {
            null -> userManager.loginLocalUser(username, masterPassword)
            else -> userManager.loginRemoteUser(username, masterPassword, serverUrlString)
        }
    }
}