package de.passbutler.desktop

import de.passbutler.common.LoggedInUserViewModelUninitializedException
import javafx.beans.property.SimpleStringProperty
import tornadofx.ViewModel

class ItemDetailsViewModel : ViewModel(), UserViewModelUsingViewModel {
    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()
}

class ItemEditingViewModelWrapper : ViewModel(), UserViewModelUsingViewModel {

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    private val itemId by param<String?>(null)

    val itemEditingViewModel by lazy {
        val loggedInUserViewModel = loggedInUserViewModel ?: throw LoggedInUserViewModelUninitializedException
        val itemEditingViewModel = loggedInUserViewModel.itemViewModels.value.find { itemViewModel -> itemViewModel.id == itemId }?.createEditingViewModel()
            ?: loggedInUserViewModel.createNewItemEditingViewModel()

        itemEditingViewModel
    }

    val itemTitleProperty = bind { SimpleStringProperty(itemEditingViewModel.title.value) }
    val itemUsernameProperty = bind { SimpleStringProperty(itemEditingViewModel.username.value) }
    val itemPasswordProperty = bind { SimpleStringProperty(itemEditingViewModel.password.value) }
}

//fun DiscardableMutableBindable<String>.asStringProperty(): StringProperty {
//    val stringProperty = SimpleStringProperty(this.value)
//
//    stringProperty.addListener { _, _, newValue ->
//        this.value = newValue
//    }
//
//    return stringProperty
//}