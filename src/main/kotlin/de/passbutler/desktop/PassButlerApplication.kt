package de.passbutler.desktop

import de.passbutler.common.UserManager
import de.passbutler.desktop.base.BuildConfig
import de.passbutler.desktop.base.BuildInformationProvider
import de.passbutler.desktop.database.createLocalRepository
import de.passbutler.desktop.ui.Styles
import javafx.stage.Stage
import kotlinx.coroutines.runBlocking
import org.tinylog.configuration.Configuration
import org.tinylog.kotlin.Logger
import tornadofx.App
import tornadofx.launch
import java.util.*

class PassButlerApplication : App(LoginScreen::class, Styles::class) {
    override fun start(stage: Stage) {
        super.start(stage)

        setupLogger()

        userManager = createUserManager()
    }

    private fun setupLogger() {
        Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler())

        Configuration.replace(createLoggerConfiguration())

        val loggingHeader = createLoggingHeader()
        Logger.debug("Started Pass Butler\n$loggingHeader")
    }

    private fun createLoggerConfiguration(): Map<String, String> {
        // TODO: Do not hardcode
        val logFilePath = "/home/bastian/Desktop/passbutler-debug.log"

        return mapOf(
                "writer1" to "file",
                "writer1.level" to "debug",
                "writer1.format" to "{date} {level} {class-name}.{method}() [{thread}]: {message}",
                "writer1.file" to logFilePath,
                "writer1.charset" to "UTF-8",
                "writer1.append" to "true",
                "writer1.buffered" to "true",

                "writingthread" to "true"
        )
    }

    private fun createUserManager(): UserManager {
        val localRepository = runBlocking {
            // TODO: Do not hardcode
            val databasePath = "/home/bastian/Desktop/PassButlerDatabase.sqlite"
            createLocalRepository(databasePath)
        }

        return UserManager(localRepository, BuildInformationProvider)
    }

    private fun createLoggingHeader(): String {
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE

        return StringBuilder().apply {
            appendln("--------------------------------------------------------------------------------")
            appendln("App:         ${BuildConfig.APPLICATION_ID} $versionName-$versionCode")
            appendln("Locale:      ${Locale.getDefault()}")
            appendln("--------------------------------------------------------------------------------")
        }.toString()
    }

    companion object {
        lateinit var userManager: UserManager
            private set
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