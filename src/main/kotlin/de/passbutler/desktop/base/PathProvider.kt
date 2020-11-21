package de.passbutler.desktop.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

interface DirectoryPathProviding {
    val homeDirectory: String
    val configurationDirectory: String
    val logDirectory: String
}

interface FilePathProviding {
    val configurationFile: String
    val logFile: String
}

// TODO: Respect other platform paths
object PathProvider : DirectoryPathProviding, FilePathProviding {

    override val homeDirectory: String
        get() = System.getProperty("user.home")

    override val configurationDirectory: String
        get() = "$homeDirectory/.config/PassButler"

    override val configurationFile: String
        get() = "$configurationDirectory/configuration.json"

    override val logDirectory: String
        get() = configurationDirectory

    override val logFile: String
        get() = "$logDirectory/debug.log"

    suspend fun obtainDirectory(directoryPathProviding: DirectoryPathProviding.() -> String): File {
        return withContext(Dispatchers.IO) {
            val directoryPathProvidingString = directoryPathProviding.invoke(this@PathProvider)
            File(directoryPathProvidingString)
        }
    }

    // TODO: Ensure, the path exists?
    suspend fun obtainFile(filePathProviding: FilePathProviding.() -> String): File {
        return withContext(Dispatchers.IO) {
            val filePathProvidingString = filePathProviding.invoke(this@PathProvider)
            File(filePathProvidingString)
        }
    }
}