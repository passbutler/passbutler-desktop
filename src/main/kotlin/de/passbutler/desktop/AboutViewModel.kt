package de.passbutler.desktop

import de.passbutler.common.base.Bindable
import de.passbutler.common.base.Failure
import de.passbutler.common.base.MutableBindable
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import de.passbutler.common.base.resultOrNull
import de.passbutler.desktop.base.ConfigProperty
import de.passbutler.desktop.base.readConfigProperty
import de.passbutler.desktop.base.writeConfigProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tornadofx.ViewModel
import java.io.File

class AboutViewModel : ViewModel() {

    val premiumKey: Bindable<PremiumKey?>
        get() = _premiumKey

    private val _premiumKey = MutableBindable<PremiumKey?>(null)

    suspend fun checkPremiumKey() {
        _premiumKey.value = app.readConfigProperty {
            string(ConfigProperty.PREMIUM_KEY)
        }.resultOrNull()?.let { PremiumKey.Deserializer.deserializeOrNull(it) }
    }

    suspend fun registerPremiumKey(premiumKeyFile: File): Result<Unit> {
        val (newRawPremiumKey, newParsedPremiumKey) = withContext(Dispatchers.IO) {
            val rawPremiumKey = premiumKeyFile.readLines().firstOrNull() ?: ""
            val parsedPremiumKey = PremiumKey.Deserializer.deserializeOrNull(rawPremiumKey)

            rawPremiumKey to parsedPremiumKey
        }

        return if (newParsedPremiumKey != null) {
            val writeResult = app.writeConfigProperty {
                set(ConfigProperty.PREMIUM_KEY to newRawPremiumKey)
            }

            if (writeResult is Success) {
                _premiumKey.value = newParsedPremiumKey
            }
            writeResult
        } else {
            // TODO: Real exception
            Failure(Exception(""))
        }
    }

    suspend fun removePremiumKey(): Result<Unit> {
        val writeResult = app.writeConfigProperty {
            set(ConfigProperty.PREMIUM_KEY to null)
        }

        if (writeResult is Success) {
            _premiumKey.value = null
        }

        return writeResult
    }
}
