package de.passbutler.desktop

import de.passbutler.common.base.Failure
import de.passbutler.common.base.LoggingConstants
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import de.passbutler.common.base.formattedDateTime
import de.passbutler.common.base.resultOrNull
import de.passbutler.desktop.PassButlerApplication.Configuration.Companion.applicationConfiguration
import de.passbutler.desktop.base.PathProvider
import de.passbutler.desktop.ui.ThemeManager
import de.passbutler.desktop.ui.ThemeType
import javafx.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.tinylog.kotlin.Logger
import tornadofx.App
import tornadofx.Component
import tornadofx.ConfigProperties
import tornadofx.FX
import tornadofx.launch
import tornadofx.px
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.util.*

typealias TinylogConfiguration = org.tinylog.configuration.Configuration

class PassButlerApplication : App(RootScreen::class, ThemeManager.themeType.kotlinClass) {

    override val configBasePath: Path = Paths.get(PathProvider.obtainDirectoryBlocking { configurationDirectory }.absolutePath)

    override fun start(stage: Stage) {
        stage.minWidth = 800.px.value
        stage.minHeight = 600.px.value

        setupLogger()
        setupTheme()
        setupLocale()

        super.start(stage)
    }

    private fun setupLogger() {
        Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler())

        TinylogConfiguration.replace(createLoggerConfiguration())

        val loggingHeader = createLoggingHeader()
        Logger.debug("Started Pass Butler\n$loggingHeader")
    }

    private fun createLoggerConfiguration(): Map<String, String> {
        val logFilePath = PathProvider.obtainFileBlocking { logFile }.absolutePath
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

    private fun setupTheme() {
        val restoredThemeType = runBlocking {
            applicationConfiguration.readValue {
                string(Configuration.THEME_TYPE)
            }.resultOrNull()?.let { ThemeType.valueOfOrNull(it) }
        }

        if (restoredThemeType != null) {
            ThemeManager.themeType = restoredThemeType
        }
    }

    private fun setupLocale() {
        val restoredLanguage = runBlocking {
            applicationConfiguration.readValue {
                string(Configuration.LANGUAGE_CODE)
            }.resultOrNull()?.let { Language.valueOfOrNull(it) }
        }

        if (restoredLanguage != null) {
            FX.locale = Locale(restoredLanguage.languageCode)
        }
    }

    class Configuration(private val application: PassButlerApplication) {
        suspend fun <T> readValue(valueGetter: ConfigProperties.() -> T?): Result<T> {
            return withContext(Dispatchers.IO) {
                val readValue = with(application.config) {
                    valueGetter(this)
                }

                if (readValue != null) {
                    Success(readValue)
                } else {
                    Failure(NotFoundException)
                }
            }
        }

        suspend fun writeValue(valueSetter: ConfigProperties.() -> Unit): Result<Unit> {
            return try {
                withContext(Dispatchers.IO) {
                    with(application.config) {
                        valueSetter()
                        save()
                    }
                }

                Success(Unit)
            } catch (exception: Exception) {
                Failure(exception)
            }
        }

        companion object {
            const val RECENT_VAULT = "recentVault"
            const val THEME_TYPE = "themeType"
            const val LANGUAGE_CODE = "language"

            val Component.applicationConfiguration: Configuration
                get() = Configuration(app as PassButlerApplication)

            val PassButlerApplication.applicationConfiguration: Configuration
                get() = Configuration(this)
        }

        object NotFoundException : Exception("The value was not found!")
    }
}

enum class Language(val languageCode: String) {
    DE("de"),
    EN("en");

    companion object {
        fun valueOfOrNull(languageCode: String): Language? {
            return values().find { it.languageCode == languageCode }
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
