package de.passbutler.desktop

import tornadofx.ViewModel

class RecycleBinViewModel : ViewModel(), UserViewModelUsingViewModel {
    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()
}
