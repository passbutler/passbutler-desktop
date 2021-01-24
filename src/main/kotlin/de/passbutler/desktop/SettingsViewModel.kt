package de.passbutler.desktop

import de.passbutler.common.base.Failure
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import de.passbutler.desktop.base.ConfigProperty
import de.passbutler.desktop.base.writeConfigProperty
import de.passbutler.desktop.ui.ThemeManager
import de.passbutler.desktop.ui.ThemeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tornadofx.ViewModel

class SettingsViewModel : ViewModel(), UserViewModelUsingViewModel {

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    var hidePasswordsEnabledSetting: Boolean
        get() = loggedInUserViewModel?.hidePasswordsEnabled?.value ?: false
        set(value) {
            loggedInUserViewModel?.hidePasswordsEnabled?.value = value
        }

    private val premiumKeyViewModel by injectPremiumKeyViewModel()

    suspend fun saveThemeType(): Result<ThemeType> {
        val premiumKey = premiumKeyViewModel.premiumKey.value

        return if (premiumKey != null) {
            val newThemeType = when (ThemeManager.themeType) {
                ThemeType.LIGHT -> ThemeType.DARK
                ThemeType.DARK -> ThemeType.LIGHT
            }

            val saveSettingResult = app.writeConfigProperty {
                set(ConfigProperty.THEME_TYPE to newThemeType.name)
            }

            when (saveSettingResult) {
                is Success -> {
                    withContext(Dispatchers.Main) {
                        ThemeManager.themeType = newThemeType
                    }

                    Success(newThemeType)
                }
                is Failure -> Failure(saveSettingResult.throwable)
            }
        } else {
            Failure(PremiumKeyRequiredException)
        }
    }
}
