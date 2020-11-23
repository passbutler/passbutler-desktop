package de.passbutler.desktop

import de.passbutler.common.base.Result
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.ViewModel

class LoginViewModel : ViewModel() {

    val serverUrlProperty = bind { SimpleStringProperty() }
    val usernameProperty = bind { SimpleStringProperty() }
    val passwordProperty = bind { SimpleStringProperty() }
    val isLocalLoginProperty = bind { SimpleBooleanProperty() }

    private val rootViewModel by injectRootViewModel()

    suspend fun loginUser(serverUrlString: String?, username: String, masterPassword: String): Result<Unit> {
        return rootViewModel.loginVault(serverUrlString, username, masterPassword)
    }
}
