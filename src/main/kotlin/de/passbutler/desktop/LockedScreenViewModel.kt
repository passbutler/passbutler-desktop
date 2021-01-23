package de.passbutler.desktop

import de.passbutler.common.base.Result
import tornadofx.ViewModel

class LockedScreenViewModel : ViewModel() {

    private val rootViewModel by injectRootViewModel()

    suspend fun unlockScreenWithPassword(masterPassword: String): Result<Unit> {
        return rootViewModel.unlockVaultWithPassword(masterPassword)
    }
}
