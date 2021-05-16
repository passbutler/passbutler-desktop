package de.passbutler.desktop.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.tinylog.kotlin.Logger
import java.io.File
import java.io.IOException
import java.util.*

interface DirectoryPathProviding {
    val homeDirectory: String
    val configurationDirectory: String
}

interface FilePathProviding {
    val logFile: String
}

object PathProvider : DirectoryPathProviding, FilePathProviding {

    override val homeDirectory: String
        get() = getPropertyOrNull("user.home") ?: throw IllegalStateException("The home directory path could not be determined!")

    override val configurationDirectory: String
        get() = when (obtainOperatingSystem()) {
            is OperatingSystem.Linux -> "$homeDirectory/.config/PassButler"
            is OperatingSystem.MacOS -> "$homeDirectory/Library/Application Support/PassButler"
            is OperatingSystem.Windows -> getEnvOrNull("APPDATA") ?: throw IllegalStateException("The configuration directory path could not be determined!")
        }

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

    @Throws(IllegalStateException::class)
    fun obtainOperatingSystem(): OperatingSystem {
        return getPropertyOrNull("os.name", "unknown")?.let { operatingSystemName ->
            val lowercasedOperatingSystemName = operatingSystemName.lowercase(Locale.ENGLISH)

            when {
                lowercasedOperatingSystemName.startsWith("linux") -> OperatingSystem.Linux(operatingSystemName)
                lowercasedOperatingSystemName.startsWith("windows") -> OperatingSystem.Windows(operatingSystemName)
                lowercasedOperatingSystemName.startsWith("mac os") -> OperatingSystem.MacOS(operatingSystemName)
                else -> null
            }
        } ?: throw IllegalStateException("The operating system type could not be determined!")
    }

    private fun getPropertyOrNull(key: String, fallback: String? = null): String? {
        return try {
            System.getProperty(key, fallback)
        } catch (exception: SecurityException) {
            Logger.warn("The property '$key' could not be retrieved!")
            null
        }
    }

    private fun getEnvOrNull(name: String): String? {
        return try {
            System.getenv(name)
        } catch (exception: SecurityException) {
            Logger.warn("The environment variable '$name' could not be retrieved!")
            null
        }
    }

    sealed class OperatingSystem(val internalString: String) {
        class Linux(internalString: String) : OperatingSystem(internalString)
        class Windows(internalString: String) : OperatingSystem(internalString)
        class MacOS(internalString: String) : OperatingSystem(internalString)
    }
}
