package de.passbutler.desktop

import de.passbutler.common.base.Result
import tornadofx.ViewModel
import java.io.File

class CreateLocalUserWizardViewModel : ViewModel() {
    private val rootViewModel by injectRootViewModel()

    suspend fun createVault(username: String, masterPassword: String, selectedFile: File): Result<Unit> {
        return rootViewModel.createVault(username, masterPassword, selectedFile)
    }
}
