package de.passbutler.desktop

import de.passbutler.common.base.Failure
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import de.passbutler.desktop.PassButlerApplication.Configuration.Companion.applicationConfiguration
import de.passbutler.desktop.ui.ThemeManager
import de.passbutler.desktop.ui.ThemeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tornadofx.ViewModel

class SettingsViewModel : ViewModel(), UserViewModelUsingViewModel {

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    var hidePasswordsEnabledSetting: Boolean
        get() {
            return loggedInUserViewModel?.hidePasswordsEnabled?.value ?: false
        }
        set(value) {
            loggedInUserViewModel?.hidePasswordsEnabled?.value = value
        }

    suspend fun saveThemeType(): Result<ThemeType> {
        val newThemeType = when (ThemeManager.themeType) {
            ThemeType.LIGHT -> ThemeType.DARK
            ThemeType.DARK -> ThemeType.LIGHT
        }

        val saveSettingResult = applicationConfiguration.writeValue {
            set(PassButlerApplication.Configuration.THEME_TYPE to newThemeType.name)
        }

        return when (saveSettingResult) {
            is Success -> {
                withContext(Dispatchers.Main) {
                    ThemeManager.themeType = newThemeType
                }

                Success(newThemeType)
            }
            is Failure -> Failure(saveSettingResult.throwable)
        }
    }
}
