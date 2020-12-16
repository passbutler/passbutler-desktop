package de.passbutler.desktop

import de.passbutler.common.ItemEditingViewModel.Companion.NOTES_MAXIMUM_CHARACTERS
import de.passbutler.common.base.DependentValueGetterBindable
import de.passbutler.common.base.formattedDateTime
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.FormFieldValidatorRule
import de.passbutler.desktop.ui.FormValidating
import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.Theme.Companion.fontLight
import de.passbutler.desktop.ui.addLifecycleObserver
import de.passbutler.desktop.ui.bindEnabled
import de.passbutler.desktop.ui.bindInput
import de.passbutler.desktop.ui.bindTextAndVisibility
import de.passbutler.desktop.ui.bindVisibility
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.marginL
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelHeadline1
import de.passbutler.desktop.ui.textSizeLarge
import de.passbutler.desktop.ui.validateWithRules
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.text.FontWeight
import tornadofx.Field
import tornadofx.Fieldset
import tornadofx.ValidationContext
import tornadofx.action
import tornadofx.addClass
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.paddingAll
import tornadofx.paddingBottom
import tornadofx.paddingTop
import tornadofx.passwordfield
import tornadofx.scrollpane
import tornadofx.style
import tornadofx.textarea
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

    init {
        setupRootView()
    }

    override fun Node.setupMainContent() {
        scrollpane(fitToWidth = true, fitToHeight = true) {
            addClass(Theme.scrollPaneBorderlessStyle)

            vbox {
                paddingAll = marginM.value

                textLabelHeadline1(titleProperty)

                form {
                    fieldset(labelPosition = Orientation.VERTICAL) {
                        paddingTop = marginS.value
                        paddingBottom = marginL.value

                        createTitleField()

                        // TODO: Spacing not working
                        vbox {
                            paddingTop = marginM.value
                            spacing = marginS.value

                            textLabelHeadline1(messages["itemdetail_details_header"])

                            createUsernameField()
                            createPasswordField()
                            createUrlField()
                            createNotesField()
                        }

                        vbox {
                            paddingTop = marginM.value
                            spacing = marginS.value

                            textLabelHeadline1(messages["itemdetail_information_header"])

                            createInformationView(messages["itemdetail_id_title"]) {
                                style {
                                    fontFamily = "monospace"
                                }

                                bindTextAndVisibility(this@ItemDetailScreen, viewModel.id)
                            }

                            createInformationView(messages["itemdetail_modified_title"]) {
                                bindTextAndVisibility(this@ItemDetailScreen, viewModel.modified) {
                                    it?.formattedDateTime
                                }
                            }

                            createInformationView(messages["itemdetail_created_title"]) {
                                bindTextAndVisibility(this@ItemDetailScreen, viewModel.created) {
                                    it?.formattedDateTime
                                }
                            }

                            bindVisibility(this@ItemDetailScreen, viewModel.isNewItem) { isNewItem ->
                                !isNewItem
                            }
                        }
                    }

                    createSaveButton()
                }
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

    private fun Fieldset.createNotesField(): Field {
        return field(messages["itemdetail_notes_hint"]) {
            textarea {
                bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
                bindInput(viewModel.notes)

                prefRowCount = 2

                textProperty().addListener { _, _, newValue ->
                    // Do not reject new value / use old value - only accept the first N of characters
                    text = newValue.take(NOTES_MAXIMUM_CHARACTERS)
                }

                // TODO: Counter
            }
        }
    }

    private fun Node.createInformationView(title: String, valueSetup: Label.() -> Unit): Node {
        return vbox {
            textLabelBody1(title) {
                style {
                    fontWeight = FontWeight.BOLD
                }
            }

            textLabelBody1(op = valueSetup)
        }
    }

    private fun Node.createSaveButton(): Button {
        return jfxButtonRaised(messages["itemdetail_save_button_title"]) {
            isDefaultButton = true

            bindVisibility(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
            bindEnabled(this@ItemDetailScreen, isItemModified)

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
    }

    private fun updateTitle() {
        title = if (viewModel.isNewItem.value) {
            messages["itemdetail_title_new"]
        } else {
            messages["itemdetail_title_edit"]
        }
    }
}
