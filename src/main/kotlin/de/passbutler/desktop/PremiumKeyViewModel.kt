package de.passbutler.desktop

import de.passbutler.common.base.Bindable
import de.passbutler.common.base.Failure
import de.passbutler.common.base.MutableBindable
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import de.passbutler.common.base.resultOrNull
import de.passbutler.common.base.resultOrThrowException
import de.passbutler.desktop.base.ConfigProperty
import de.passbutler.desktop.base.readConfigProperty
import de.passbutler.desktop.base.writeConfigProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tinylog.kotlin.Logger
import tornadofx.App
import tornadofx.Component
import tornadofx.FX
import tornadofx.ViewModel
import java.io.File
import java.io.IOException

class PremiumKeyViewModel : ViewModel() {

    val premiumKey: Bindable<PremiumKey?>
        get() = _premiumKey

    private val _premiumKey = MutableBindable<PremiumKey?>(null)

    suspend fun initializePremiumKey() {
        val premiumKeyJsonWebToken = app.readConfigProperty {
            string(ConfigProperty.PREMIUM_KEY)
        }.resultOrNull()

        if (premiumKeyJsonWebToken != null) {
            when (val parseResult = PremiumKey.parse(premiumKeyJsonWebToken)) {
                is Success -> {
                    val premiumKey = parseResult.result
                    Logger.debug("The premium key (id = '${premiumKey.id}') was parsed successfully")

                    _premiumKey.value = premiumKey
                }
                is Failure -> {
                    Logger.warn(parseResult.throwable, "The premium key could not be parsed!")
                }
            }
        } else {
            Logger.debug("No premium key available")
        }
    }

    suspend fun registerPremiumKey(premiumKeyFile: File): Result<Unit> {
        return try {
            val premiumKeyJsonWebToken = withContext(Dispatchers.IO) {
                premiumKeyFile.readLines().firstOrNull() ?: throw IOException("The first line of the premium key file is empty!")
            }

            val premiumKeyParseResult = PremiumKey.parse(premiumKeyJsonWebToken).resultOrThrowException()

            val writeConfigurationResult = app.writeConfigProperty {
                set(ConfigProperty.PREMIUM_KEY to premiumKeyJsonWebToken)
            }

            if (writeConfigurationResult is Success) {
                _premiumKey.value = premiumKeyParseResult
            }

            writeConfigurationResult
        } catch (exception: Exception) {
            Failure(exception)
        }
    }

    suspend fun removePremiumKey(): Result<Unit> {
        val writeConfigurationResult = app.writeConfigProperty {
            set(ConfigProperty.PREMIUM_KEY to null)
        }

        if (writeConfigurationResult is Success) {
            _premiumKey.value = null
        }

        return writeConfigurationResult
    }
}

/**
 * Injects the `PremiumKeyViewModel` with global scope to ensure it is always the same shared instance.
 */
inline fun <reified T> T.injectPremiumKeyViewModel()
    where T : Component = inject<PremiumKeyViewModel>(FX.defaultScope)

fun App.injectPremiumKeyViewModel() = inject<PremiumKeyViewModel>(FX.defaultScope)
