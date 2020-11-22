package de.passbutler.desktop.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

interface DirectoryPathProviding {
    val homeDirectory: String
    val configurationDirectory: String
}

interface FilePathProviding {
    val logFile: String
}

// TODO: Handle other platform paths
object PathProvider : DirectoryPathProviding, FilePathProviding {

    override val homeDirectory: String
        get() = System.getProperty("user.home")

    override val configurationDirectory: String
        get() = "$homeDirectory/.config/PassButler"

    override val logFile: String
        get() = "$configurationDirectory/debug.log"

    @Throws(IOException::class)
    suspend fun obtainDirectory(directoryPathProviding: DirectoryPathProviding.() -> String): File {
        return withContext(Dispatchers.IO) {
            val directoryPathProvidingString = directoryPathProviding.invoke(this@PathProvider)
            val directoryFile = File(directoryPathProvidingString)

            // Ensure the path exists
            directoryFile.mkdirs()

            directoryFile
        }
    }

    @Throws(IOException::class)
    fun obtainDirectoryBlocking(directoryPathProviding: DirectoryPathProviding.() -> String): File {
        return runBlocking {
            obtainDirectory(directoryPathProviding)
        }
    }

    @Throws(IOException::class)
    suspend fun obtainFile(filePathProviding: FilePathProviding.() -> String): File {
        return withContext(Dispatchers.IO) {
            val filePathProvidingString = filePathProviding.invoke(this@PathProvider)
            val file = File(filePathProvidingString)

            // Ensure the path exists
            file.parentFile.mkdirs()

            // Ensure the file exists
            if (!file.exists()) {
                // Because the blocking call is dispatched to IO, there should be no problem
                @Suppress("BlockingMethodInNonBlockingContext")
                file.createNewFile()
            }

            file
        }
    }

    @Throws(IOException::class)
    fun obtainFileBlocking(filePathProviding: FilePathProviding.() -> String): File {
        return runBlocking {
            obtainFile(filePathProviding)
        }
    }
}