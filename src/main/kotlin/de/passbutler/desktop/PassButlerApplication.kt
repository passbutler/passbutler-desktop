package de.passbutler.desktop

import de.passbutler.common.base.LoggingConstants
import de.passbutler.common.base.formattedDateTime
import de.passbutler.desktop.base.PathProvider
import de.passbutler.desktop.ui.ThemeManager
import javafx.stage.Stage
import org.tinylog.configuration.Configuration
import org.tinylog.kotlin.Logger
import tornadofx.App
import tornadofx.launch
import tornadofx.px
import java.time.Instant
import java.util.*

class PassButlerApplication : App(RootScreen::class, ThemeManager.themeType.kotlinClass) {

    override fun start(stage: Stage) {
        stage.minWidth = 800.px.value
        stage.minHeight = 600.px.value
        super.start(stage)

        setupLogger()
    }

    private fun setupLogger() {
        Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler())

        Configuration.replace(createLoggerConfiguration())

        val loggingHeader = createLoggingHeader()
        Logger.debug("Started Pass Butler\n$loggingHeader")
    }

    private fun createLoggerConfiguration(): Map<String, String> {
        val logFilePath = PathProvider.logFile
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
        val formattedBuildTime = Instant.ofEpochMilli(BuildConfig.BUILD_TIMESTAMP).formattedDateTime
        val gitShortHash = BuildConfig.BUILD_REVISION_HASH

        return buildString {
            appendLine("--------------------------------------------------------------------------------")
            appendLine("App:         ${BuildConfig.APPLICATION_ID} $versionName-$versionCode (build on $formattedBuildTime from $gitShortHash)")
            appendLine("Locale:      ${Locale.getDefault()}")
            appendLine("--------------------------------------------------------------------------------")
        }
    }
}

private class UncaughtExceptionHandler : Thread.UncaughtExceptionHandler {
    private val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        Logger.error(e, "⚠️⚠️⚠️ FATAL ⚠️⚠️⚠️")
        defaultUncaughtExceptionHandler?.uncaughtException(t, e)
    }
}

fun main(args: Array<String>) {
    launch<PassButlerApplication>(args)
}