package de.passbutler.desktop

import de.passbutler.common.LoggedInUserViewModelUninitializedException
import tornadofx.ViewModel

class ItemEditingViewModelWrapper : ViewModel(), UserViewModelUsingViewModel {

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    val itemEditingViewModel by lazy {
        val loggedInUserViewModel = loggedInUserViewModel ?: throw LoggedInUserViewModelUninitializedException
        val itemEditingViewModel = loggedInUserViewModel.itemViewModels.value.find { itemViewModel -> itemViewModel.id == params[PARAMETER_ITEM_ID] }?.createEditingViewModel()
            ?: loggedInUserViewModel.createNewItemEditingViewModel()

        itemEditingViewModel
    }

    companion object {
        const val PARAMETER_ITEM_ID = "itemId"
    }
}
