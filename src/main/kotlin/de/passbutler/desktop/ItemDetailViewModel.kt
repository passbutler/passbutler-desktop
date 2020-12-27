package de.passbutler.desktop

import de.passbutler.common.LoggedInUserViewModelUninitializedException
import tornadofx.ViewModel

class ItemEditingViewModelWrapper : ViewModel(), UserViewModelUsingViewModel {

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    private val itemId by param<String?>(null)

    val itemEditingViewModel by lazy {
        val loggedInUserViewModel = loggedInUserViewModel ?: throw LoggedInUserViewModelUninitializedException
        val itemEditingViewModel = loggedInUserViewModel.itemViewModels.value.find { itemViewModel -> itemViewModel.id == itemId }?.createEditingViewModel()
            ?: loggedInUserViewModel.createNewItemEditingViewModel()

        itemEditingViewModel
    }
}
