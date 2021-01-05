package de.passbutler.desktop

import de.passbutler.common.base.resultOrNull
import de.passbutler.desktop.base.ConfigProperty
import de.passbutler.desktop.base.LoggingSetupProviding
import de.passbutler.desktop.base.PathProvider
import de.passbutler.desktop.base.loggingSetupProvider
import de.passbutler.desktop.base.readConfigProperty
import de.passbutler.desktop.ui.ThemeManager
import de.passbutler.desktop.ui.ThemeType
import javafx.application.Platform
import javafx.stage.Stage
import kotlinx.coroutines.runBlocking
import org.tinylog.kotlin.Logger
import tornadofx.App
import tornadofx.DefaultErrorHandler
import tornadofx.FX
import tornadofx.launch
import tornadofx.px
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class PassButlerApplication : App(RootScreen::class, ThemeManager.themeType.kotlinClass), LoggingSetupProviding by loggingSetupProvider {

    override val configBasePath: Path = Paths.get(PathProvider.obtainDirectoryBlocking { configurationDirectory }.absolutePath)

    private val premiumKeyViewModel by injectPremiumKeyViewModel()

    override fun start(stage: Stage) {
        stage.minWidth = 800.px.value
        stage.minHeight = 600.px.value

        super.start(stage)

        setupLogging()
        setupTheme()
        setupLocale()
        setupCrashHandler()
    }

    private fun setupLogging() {
        val logFilePath = PathProvider.obtainFileBlocking { logFile }.absolutePath
        setupLogging(logFilePath)
    }

    private fun setupTheme() {
        val restoredThemeType = runBlocking {
            // Before first usage, load premium key from configuration
            premiumKeyViewModel.initializePremiumKey()

            val premiumKey = premiumKeyViewModel.premiumKey.value

            val themeType = readConfigProperty {
                string(ConfigProperty.THEME_TYPE)
            }.resultOrNull()?.let { ThemeType.valueOfOrNull(it) }?.takeIf { premiumKey != null }

            themeType
        }

        if (restoredThemeType != null) {
            ThemeManager.themeType = restoredThemeType
        }
    }

    private fun setupLocale() {
        val restoredLanguageCode = runBlocking {
            val supportedLanguageCodes = listOf(
                "en",
                "de"
            )

            readConfigProperty {
                string(ConfigProperty.LANGUAGE_CODE)
            }.resultOrNull()?.takeIf { supportedLanguageCodes.contains(it) }
        }

        if (restoredLanguageCode != null) {
            FX.locale = Locale(restoredLanguageCode)
        }
    }

    private fun setupCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler())

        DefaultErrorHandler.filter = { errorEvent ->
            // Consume error event to avoid showing an alert window
            errorEvent.consume()

            // Do not call `System.exit(-1)` to allow JavaFX to shutdown properly
            Platform.exit()
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
