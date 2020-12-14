package de.passbutler.desktop

import de.passbutler.common.base.BindableObserver
import de.passbutler.common.base.DependentValueGetterBindable
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.FormFieldValidatorRule
import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.textLabelHeadline1
import de.passbutler.desktop.ui.validatorWithRules
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Button
import tornadofx.Field
import tornadofx.Fieldset
import tornadofx.Form
import tornadofx.action
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.textfield
import tornadofx.vbox
import tornadofx.whenDocked

class ItemDetailScreen : NavigationMenuScreen(navigationMenuItems = createDefaultNavigationMenu()), RequestSending {

    private val viewModel
        get() = viewModelWrapper.itemEditingViewModel

    private val viewModelWrapper by injectWithPrivateScope<ItemEditingViewModelWrapper>(params)

    private val isItemModified by lazy {
        DependentValueGetterBindable(
            viewModel.title,
            viewModel.username,
            viewModel.password,
            viewModel.url,
            viewModel.notes
        ) {
            listOf(
                viewModel.title,
                viewModel.username,
                viewModel.password,
                viewModel.url,
                viewModel.notes
            ).any { it.isModified }
        }
    }

    private val isNewItemObserver: BindableObserver<Boolean> = {
        updateTitle()
    }

    private val isItemModifiedObserver: BindableObserver<Boolean> = {
        updateSaveButton()
    }

    private var saveButton: Button? = null

    init {
        setupRootView()
    }

    override fun Node.createMainContent() {
        vbox {
            paddingAll = marginM.value

            textLabelHeadline1(titleProperty)

            form {
                fieldset(labelPosition = Orientation.VERTICAL) {
                    paddingTop = marginS.value

                    createTitleField()
                }

                saveButton = createSaveButton()
            }
        }
    }

    private fun Fieldset.createTitleField(): Field {
        return field(messages["itemdetail_title_hint"]) {
            textfield(viewModelWrapper.itemTitleProperty) {
                whenDocked {
                    requestFocus()
                }

                validatorWithRules {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["itemdetail_title_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Form.createSaveButton(): Button {
        return jfxButtonRaised(messages["itemdetail_save_button_title"]) {
            isDefaultButton = true

            action {
                saveClicked()
            }
        }
    }

    private fun saveClicked() {
        viewModelWrapper.validate()

        if (viewModelWrapper.valid.value) {
            launchRequestSending(
                handleFailure = { showError(messages["itemdetail_save_failed_general_title"]) }
            ) {
                viewModel.save()
            }
        }
    }

    override fun onDock() {
        super.onDock()

        viewModel.isNewItem.addObserver(this, true, isNewItemObserver)
        isItemModified.addObserver(this, true, isItemModifiedObserver)
    }

    override fun onUndock() {
        viewModel.isNewItem.removeObserver(isNewItemObserver)
        isItemModified.removeObserver(isItemModifiedObserver)

        super.onUndock()
    }

    private fun updateTitle() {
        title = if (viewModel.isNewItem.value) {
            messages["itemdetail_title_new"]
        } else {
            messages["itemdetail_title_edit"]
        }
    }

    private fun updateSaveButton() {
        saveButton?.isVisible = viewModel.isItemModificationAllowed.value
        saveButton?.isDisable = !isItemModified.value
    }
}
