package de.passbutler.desktop.base

import de.passbutler.common.base.BuildType
import de.passbutler.common.base.LoggingConstants
import de.passbutler.common.base.formattedDateTime
import de.passbutler.desktop.BuildConfig
import org.tinylog.configuration.Configuration
import org.tinylog.kotlin.Logger
import java.time.Instant
import java.util.*

interface LoggingSetupProviding {
    fun setupLogging(logFilePath: String)
}

class DebugLoggingSetupProvider : LoggingSetupProviding {
    override fun setupLogging(logFilePath: String) {
        Configuration.replace(createLoggerConfiguration(logFilePath))

        val loggingHeader = createLoggingHeader()
        Logger.debug("Started Pass Butler\n$loggingHeader")
    }

    private fun createLoggerConfiguration(logFilePath: String): Map<String, String> {
        val logFormat = LoggingConstants.LOG_FORMAT_FILE

        return mapOf(
            "writer1" to "console",
            "writer1.level" to "trace",
            "writer1.format" to logFormat,

            "writer2" to "file",
            "writer2.level" to "debug",
            "writer2.format" to logFormat,
            "writer2.file" to logFilePath,
            "writer2.charset" to "UTF-8",
            "writer2.append" to "true",
            "writer2.buffered" to "true",

            "writingthread" to "true"
        )
    }

    private fun createLoggingHeader(): String {
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        val formattedBuildTime = Instant.ofEpochMilli(BuildConfig.BUILD_TIMESTAMP).formattedDateTime()
        val gitShortHash = BuildConfig.BUILD_REVISION_HASH

        return buildString {
            appendLine("--------------------------------------------------------------------------------")
            appendLine("App:         ${BuildConfig.APPLICATION_ID} $versionName-$versionCode (build on $formattedBuildTime from $gitShortHash)")
            appendLine("Locale:      ${Locale.getDefault()}")
            appendLine("--------------------------------------------------------------------------------")
        }
    }
}

class ReleaseLoggingSetupProvider : LoggingSetupProviding {
    override fun setupLogging(logFilePath: String) {
        Configuration.replace(createLoggerConfiguration())
    }

    private fun createLoggerConfiguration(): Map<String, String> {
        // Disable also the default logger
        return mapOf(
            "writer.level" to "off",
        )
    }
}

val loggingSetupProvider = when (BuildInformationProvider.buildType) {
    BuildType.Debug -> DebugLoggingSetupProvider()
    BuildType.Release -> ReleaseLoggingSetupProvider()
    BuildType.Other -> ReleaseLoggingSetupProvider()
}
