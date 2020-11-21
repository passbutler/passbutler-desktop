package de.passbutler.desktop.ui

import de.passbutler.desktop.base.PathProvider
import javafx.stage.FileChooser
import kotlinx.coroutines.runBlocking
import tornadofx.FX
import tornadofx.FileChooserMode
import tornadofx.chooseFile
import tornadofx.get
import java.io.File

fun showOpenVaultFileChooser(title: String, chosenFileBlock: (File) -> Unit) {
    val homeDirectory = runBlocking {
        PathProvider.obtainDirectory { homeDirectory }
    }

    chooseFile(title, createFileChooserExtensionFilter(), initialDirectory = homeDirectory, mode = FileChooserMode.Single).firstOrNull()?.let {
        chosenFileBlock(it)
    }
}

fun showSaveVaultFileChooser(title: String, chosenFileBlock: (File) -> Unit) {
    val homeDirectory = runBlocking {
        PathProvider.obtainDirectory { homeDirectory }
    }

    chooseFile(title, createFileChooserExtensionFilter(), initialDirectory = homeDirectory, mode = FileChooserMode.Save).firstOrNull()?.let {
        chosenFileBlock(it)
    }
}

private fun createFileChooserExtensionFilter(): Array<FileChooser.ExtensionFilter> {
    val userFacingFileExtensionDescription = FX.messages["file_extension_description"]
    val userFacingFileExtensionPattern = "*.$VAULT_FILE_EXTENSION"
    val extensionFilter = FileChooser.ExtensionFilter("$userFacingFileExtensionDescription ($userFacingFileExtensionPattern)", userFacingFileExtensionPattern)
    return arrayOf(extensionFilter)
}

const val VAULT_FILE_EXTENSION = "sqlite"
