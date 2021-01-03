package de.passbutler.desktop

import de.passbutler.common.base.resultOrNull
import de.passbutler.desktop.base.ConfigProperty
import de.passbutler.desktop.base.LoggingSetupProviding
import de.passbutler.desktop.base.PathProvider
import de.passbutler.desktop.base.loggingSetupProvider
import de.passbutler.desktop.base.readConfigProperty
import de.passbutler.desktop.ui.ThemeManager
import de.passbutler.desktop.ui.ThemeType
import javafx.stage.Stage
import kotlinx.coroutines.runBlocking
import tornadofx.App
import tornadofx.FX
import tornadofx.launch
import tornadofx.px
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class PassButlerApplication : App(RootScreen::class, ThemeManager.themeType.kotlinClass), LoggingSetupProviding by loggingSetupProvider {

    override val configBasePath: Path = Paths.get(PathProvider.obtainDirectoryBlocking { configurationDirectory }.absolutePath)

    override fun start(stage: Stage) {
        stage.minWidth = 800.px.value
        stage.minHeight = 600.px.value

        val logFilePath = PathProvider.obtainFileBlocking { logFile }.absolutePath
        setupLogging(logFilePath)

        setupTheme()
        setupLocale()

        super.start(stage)
    }

    private fun setupTheme() {
        val restoredThemeType = runBlocking {
            readConfigProperty {
                string(ConfigProperty.THEME_TYPE)
            }.resultOrNull()?.let { ThemeType.valueOfOrNull(it) }
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
}

fun main(args: Array<String>) {
    launch<PassButlerApplication>(args)
}
