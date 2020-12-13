package de.passbutler.desktop

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
}
