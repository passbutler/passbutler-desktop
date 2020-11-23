package de.passbutler.desktop

import de.passbutler.common.base.Failure
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.ViewModel

class LoginViewModel : ViewModel(), UserViewModelUsingViewModel {

    val serverUrlProperty = bind { SimpleStringProperty() }
    val usernameProperty = bind { SimpleStringProperty() }
    val passwordProperty = bind { SimpleStringProperty() }
    val isLocalLoginProperty = bind { SimpleBooleanProperty() }

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    private val rootViewModel by injectRootViewModel()

    suspend fun loginUser(serverUrlString: String?, username: String, masterPassword: String): Result<Unit> {
        val loginResult = userViewModelProvidingViewModel.loginUser(serverUrlString, username, masterPassword)

        return when (loginResult) {
            is Success -> {
                rootViewModel.rootScreenState.value = RootViewModel.RootScreenState.LoggedIn.Unlocked
                Success(Unit)
            }
            is Failure -> Failure(loginResult.throwable)
        }
    }
}