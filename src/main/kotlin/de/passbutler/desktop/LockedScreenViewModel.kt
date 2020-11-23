package de.passbutler.desktop

import de.passbutler.common.base.Result
import javafx.beans.property.SimpleStringProperty
import tornadofx.ViewModel

class LockedScreenViewModel : ViewModel() {

    val passwordProperty = bind { SimpleStringProperty() }

    private val rootViewModel by injectRootViewModel()

    suspend fun unlockScreenWithPassword(masterPassword: String): Result<Unit> {
        return rootViewModel.unlockVaultWithPassword(masterPassword)
    }
}
