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
import de.passbutler.desktop.ui.isEnabled
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.showScreenUnanimated
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelBody2
import de.passbutler.desktop.ui.textLabelHeadline1
import de.passbutler.desktop.ui.textSizeLarge
import de.passbutler.desktop.ui.validateWithRules
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TextFormatter
import javafx.scene.text.FontWeight
import tornadofx.Fieldset
import tornadofx.Form
import tornadofx.ValidationContext
import tornadofx.action
import tornadofx.addClass
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.paddingAll
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

    private val itemAuthorizationDescription by lazy {
        DependentValueGetterBindable(viewModel.isItemAuthorizationAllowed, viewModel.isItemModificationAllowed, viewModel.ownerUsername, viewModel.itemAuthorizationModifiedDate) {
            val itemOwnerUsername = viewModel.ownerUsername.value
            val itemAuthorizationModifiedDate = viewModel.itemAuthorizationModifiedDate.value?.formattedDateTime

            when {
                viewModel.isItemAuthorizationAllowed.value -> messages["itemdetail_authorizations_description_owned_item"]
                viewModel.isItemModificationAllowed.value && itemOwnerUsername != null && itemAuthorizationModifiedDate != null -> {
                    messages["itemdetail_authorizations_description_shared_item"].format(itemOwnerUsername, itemAuthorizationModifiedDate)
                }
                !viewModel.isItemModificationAllowed.value && itemOwnerUsername != null && itemAuthorizationModifiedDate != null -> {
                    messages["itemdetail_authorizations_description_shared_readonly_item"].format(itemOwnerUsername, itemAuthorizationModifiedDate)
                }
                else -> null
            }
        }
    }

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

            form {
                paddingAll = marginM.value

                setupDetailsSection()
                setupItemAuthorizationsSection()
                setupInformationSection()
                setupDeleteSection()
            }
        }
    }

    private fun Form.setupDetailsSection() {
        textLabelHeadline1(titleProperty)

        fieldset(labelPosition = Orientation.VERTICAL) {
            paddingTop = marginM.value
            setupTitleField()
        }

        textLabelHeadline1(messages["itemdetail_details_header"]) {
            paddingTop = marginM.value
        }

        fieldset(labelPosition = Orientation.VERTICAL) {
            paddingTop = marginM.value
            spacing = marginS.value

            setupUsernameField()
            setupPasswordField()
            setupUrlField()
            setupNotesField()
        }

        vbox {
            paddingTop = marginM.value
            setupSaveButton()
        }
    }

    private fun Fieldset.setupTitleField() {
        field(orientation = Orientation.VERTICAL) {
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

    private fun Fieldset.setupUsernameField() {
        field(messages["itemdetail_username_hint"], orientation = Orientation.VERTICAL) {
            textfield {
                bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
                bindInput(viewModel.username)
            }
        }
    }

    private fun Fieldset.setupPasswordField() {
        field(messages["itemdetail_password_hint"], orientation = Orientation.VERTICAL) {
            val inputField = if (viewModel.hidePasswordsEnabled) {
                passwordfield()
            } else {
                textfield()
            }

            inputField.bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
            inputField.bindInput(viewModel.password)
        }
    }

    private fun Fieldset.setupUrlField() {
        field(messages["itemdetail_url_hint"], orientation = Orientation.VERTICAL) {
            textfield {
                bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
                bindInput(viewModel.url)
            }
        }
    }

    private fun Fieldset.setupNotesField() {
        vbox {
            alignment = Pos.CENTER_RIGHT

            field(messages["itemdetail_notes_hint"], orientation = Orientation.VERTICAL) {
                textarea {
                    bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
                    bindInput(viewModel.notes)

                    prefRowCount = 2

                    textFormatter = TextFormatter<String> { change: TextFormatter.Change ->
                        if (change.controlNewText.length > NOTES_MAXIMUM_CHARACTERS) {
                            // Reject the change
                            null
                        } else {
                            change
                        }
                    }
                }
            }

            textLabelBody2 {
                bindTextAndVisibility(this@ItemDetailScreen, viewModel.notes) { notesValue ->
                    val notesLength = notesValue.length.coerceIn(0, NOTES_MAXIMUM_CHARACTERS)
                    "$notesLength/$NOTES_MAXIMUM_CHARACTERS"
                }
            }
        }
    }

    private fun Node.setupSaveButton() {
        jfxButtonRaised(messages["itemdetail_save_button_title"]) {
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

    private fun Node.setupItemAuthorizationsSection() {
        vbox {
            paddingTop = marginM.value
            spacing = marginM.value

            textLabelHeadline1(messages["itemdetail_authorizations_header"])

            textLabelBody1 {
                bindTextAndVisibility(this@ItemDetailScreen, itemAuthorizationDescription)
            }

            vbox {
                jfxButtonRaised(messages["itemdetail_authorizations_button_text"]) {
                    isEnabled = viewModel.isItemAuthorizationAvailable

                    action {
                        showScreenUnanimated(ItemAuthorizationsDetailScreen::class, parameters = params)
                    }
                }

                bindVisibility(this@ItemDetailScreen, viewModel.isItemAuthorizationAllowed)
            }

            textLabelBody2(messages["itemdetail_authorizations_footer_teaser"]) {
                bindVisibility(this@ItemDetailScreen, viewModel.isItemAuthorizationAllowed) { isItemAuthorizationAllowed ->
                    isItemAuthorizationAllowed && !viewModel.isItemAuthorizationAvailable
                }
            }

            bindVisibility(this@ItemDetailScreen, viewModel.isNewItem) { isNewItem ->
                !isNewItem
            }
        }
    }

    private fun Node.setupInformationSection() {
        vbox {
            paddingTop = marginM.value
            spacing = marginM.value

            textLabelHeadline1(messages["itemdetail_information_header"])

            vbox {
                spacing = marginS.value

                setupInformationView(messages["itemdetail_id_title"]) {
                    style {
                        fontFamily = "monospace"
                    }

                    bindTextAndVisibility(this@ItemDetailScreen, viewModel.id)
                }

                setupInformationView(messages["itemdetail_modified_title"]) {
                    bindTextAndVisibility(this@ItemDetailScreen, viewModel.modified) {
                        it?.formattedDateTime
                    }
                }

                setupInformationView(messages["itemdetail_created_title"]) {
                    bindTextAndVisibility(this@ItemDetailScreen, viewModel.created) {
                        it?.formattedDateTime
                    }
                }
            }

            bindVisibility(this@ItemDetailScreen, viewModel.isNewItem) { isNewItem ->
                !isNewItem
            }
        }
    }

    private fun Node.setupInformationView(title: String, valueSetup: Label.() -> Unit) {
        vbox {
            textLabelBody1(title) {
                style {
                    fontWeight = FontWeight.BOLD
                }
            }

            textLabelBody1(op = valueSetup)
        }
    }

    private fun Node.setupDeleteSection() {
        vbox {
            paddingTop = marginM.value
            spacing = marginM.value

            textLabelHeadline1(messages["itemdetail_delete_header"])
            setupDeleteButton()

            bindVisibility(this@ItemDetailScreen, viewModel.isNewItem) { isNewItem ->
                !isNewItem
            }
        }
    }

    private fun Node.setupDeleteButton() {
        jfxButtonRaised(messages["itemdetail_delete_button_title"]) {
            bindVisibility(this@ItemDetailScreen, viewModel.isItemModificationAllowed)

            action {
                deleteClicked()
            }
        }
    }

    private fun deleteClicked() {
        launchRequestSending(
            handleSuccess = {
                showInformation(messages["itemdetail_delete_successful_message"])
                showScreenUnanimated(OverviewScreen::class)
            },
            handleFailure = { showError(messages["itemdetail_delete_failed_general_title"]) }
        ) {
            viewModel.delete()
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
