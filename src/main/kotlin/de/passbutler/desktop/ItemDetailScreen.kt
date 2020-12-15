package de.passbutler.desktop

import de.passbutler.common.base.DependentValueGetterBindable
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.FormFieldValidatorRule
import de.passbutler.desktop.ui.FormValidating
import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.Theme.Companion.fontLight
import de.passbutler.desktop.ui.addLifecycleObserver
import de.passbutler.desktop.ui.bindEnabled
import de.passbutler.desktop.ui.bindInput
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.marginL
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.textLabelHeadline1
import de.passbutler.desktop.ui.textSizeLarge
import de.passbutler.desktop.ui.validateWithRules
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Button
import tornadofx.Field
import tornadofx.Fieldset
import tornadofx.Form
import tornadofx.ValidationContext
import tornadofx.action
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.paddingAll
import tornadofx.paddingBottom
import tornadofx.paddingTop
import tornadofx.passwordfield
import tornadofx.style
import tornadofx.textfield
import tornadofx.vbox

class ItemDetailScreen : NavigationMenuScreen(navigationMenuItems = createDefaultNavigationMenu()), FormValidating, RequestSending {

    override val validationContext = ValidationContext()

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

    private var saveButton: Button? = null

    init {
        setupRootView()
    }

    override fun Node.setupMainContent() {
        vbox {
            paddingAll = marginM.value

            textLabelHeadline1(titleProperty)

            form {
                paddingAll = 0

                fieldset(labelPosition = Orientation.VERTICAL) {
                    paddingTop = marginS.value
                    paddingBottom = marginL.value
                    spacing = marginS.value

                    createTitleField()

                    textLabelHeadline1(messages["itemdetail_details_header"]) {
                        paddingTop = marginM.value
                    }

                    createUsernameField()
                    createPasswordField()
                    createUrlField()
                }

                saveButton = createSaveButton()
            }
        }
    }

    private fun Fieldset.createTitleField(): Field {
        return field {
            textfield {
                style {
                    fontSize = textSizeLarge
                    fontFamily = fontLight
                }

                promptText = messages["itemdetail_title_hint"]

                bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
                bindInput(viewModel.title)

                validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["itemdetail_title_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Fieldset.createUsernameField(): Field {
        return field(messages["itemdetail_username_hint"]) {
            textfield {
                bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
                bindInput(viewModel.username)
            }
        }
    }

    private fun Fieldset.createPasswordField(): Field {
        return field(messages["itemdetail_password_hint"]) {
            val inputField = if (viewModel.hidePasswordsEnabled) {
                passwordfield()
            } else {
                textfield()
            }

            inputField.bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
            inputField.bindInput(viewModel.password)
        }
    }

    private fun Fieldset.createUrlField(): Field {
        return field(messages["itemdetail_url_hint"]) {
            textfield {
                bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
                bindInput(viewModel.url)
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
        validationContext.validate()

        if (validationContext.isValid) {
            launchRequestSending(
                handleFailure = { showError(messages["itemdetail_save_failed_general_title"]) }
            ) {
                viewModel.save()
            }
        }
    }

    override fun onDock() {
        super.onDock()

        viewModel.isNewItem.addLifecycleObserver(this, true) {
            updateTitle()
        }

        isItemModified.addLifecycleObserver(this, true) {
            updateSaveButton()
        }
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
