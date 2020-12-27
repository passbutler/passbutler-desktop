package de.passbutler.desktop

import de.passbutler.common.ItemAuthorizationsDetailViewModel
import de.passbutler.common.LoggedInUserViewModelUninitializedException
import tornadofx.ViewModel

class ItemAuthorizationsDetailViewModelWrapper : ViewModel(), UserViewModelUsingViewModel {

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    private val itemId by param<String?>(null)

    val itemAuthorizationsDetailViewModel by lazy {
        val loggedInUserViewModel = loggedInUserViewModel ?: throw LoggedInUserViewModelUninitializedException
        val itemId = itemId ?: throw IllegalStateException("The passed item ID is null!")
        val itemAuthorizationsDetailViewModel = ItemAuthorizationsDetailViewModel(itemId, loggedInUserViewModel, loggedInUserViewModel.localRepository)

        itemAuthorizationsDetailViewModel
    }
}
