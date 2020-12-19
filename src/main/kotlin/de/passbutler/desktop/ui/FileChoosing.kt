package de.passbutler.desktop.ui

import de.passbutler.desktop.base.PathProvider
import javafx.stage.FileChooser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tornadofx.FX
import tornadofx.FileChooserMode
import tornadofx.chooseFile
import tornadofx.get
import java.io.File

const val VAULT_FILE_EXTENSION = "sqlite"

fun showOpenVaultFileChooser(title: String, chosenFileBlock: (File) -> Unit) {
    val homeDirectory = PathProvider.obtainDirectoryBlocking { homeDirectory }

    chooseFile(title, createFileChooserExtensionFilter(), initialDirectory = homeDirectory, mode = FileChooserMode.Single).firstOrNull()?.let {
        chosenFileBlock(it)
    }
}

fun showSaveVaultFileChooser(title: String, chosenFileBlock: (File) -> Unit) {
    val homeDirectory = PathProvider.obtainDirectoryBlocking { homeDirectory }

    chooseFile(title, createFileChooserExtensionFilter(), initialDirectory = homeDirectory, mode = FileChooserMode.Save).firstOrNull()?.let {
        chosenFileBlock(it)
    }
}

private fun createFileChooserExtensionFilter(): Array<FileChooser.ExtensionFilter> {
    val userFacingFileExtensionDescription = FX.messages["general_file_extension_description"]
    val userFacingFileExtensionPattern = "*.$VAULT_FILE_EXTENSION"
    val extensionFilter = FileChooser.ExtensionFilter("$userFacingFileExtensionDescription ($userFacingFileExtensionPattern)", userFacingFileExtensionPattern)
    return arrayOf(extensionFilter)
}

suspend fun File.ensureFileExtension(extension: String): File {
    val initialFile = this
    val extensionSuffix = ".$extension"

    return if (initialFile.name.endsWith(extensionSuffix)) {
        initialFile
    } else {
        withContext(Dispatchers.IO) {
            File("${initialFile.path}.$extension")
        }
    }
}