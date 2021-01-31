package de.passbutler.desktop

import de.passbutler.common.ItemEditingViewModel.Companion.NOTES_MAXIMUM_CHARACTERS
import de.passbutler.common.base.DependentValueGetterBindable
import de.passbutler.common.base.formattedDateTime
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.FormFieldValidatorRule
import de.passbutler.desktop.ui.FormValidating
import de.passbutler.desktop.ui.NavigationMenuFragment
import de.passbutler.desktop.ui.ScrollSpeed
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.Theme.Companion.fontLight
import de.passbutler.desktop.ui.addLifecycleObserver
import de.passbutler.desktop.ui.bindEnabled
import de.passbutler.desktop.ui.bindInput
import de.passbutler.desktop.ui.bindTextAndVisibility
import de.passbutler.desktop.ui.bindVisibility
import de.passbutler.desktop.ui.createCancelButton
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.createInformationView
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.isEnabled
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.marginL
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.setScrollSpeed
import de.passbutler.desktop.ui.showScreenUnanimated
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelBody2
import de.passbutler.desktop.ui.textLabelHeadline1
import de.passbutler.desktop.ui.textSizeLarge
import de.passbutler.desktop.ui.validateWithRules
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.TextFormatter
import tornadofx.FX
import tornadofx.Fieldset
import tornadofx.Form
import tornadofx.ValidationContext
import tornadofx.action
import tornadofx.addClass
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.hbox
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.passwordfield
import tornadofx.scrollpane
import tornadofx.style
import tornadofx.textarea
import tornadofx.textfield
import tornadofx.vbox

class ItemDetailScreen : NavigationMenuFragment(navigationMenuItems = createDefaultNavigationMenu()), FormValidating, RequestSending {

    override val validationContext = ValidationContext()

    private val viewModel
        get() = viewModelWrapper.itemEditingViewModel

    private val viewModelWrapper by injectWithPrivateScope<ItemEditingViewModelWrapper>(params)

    private val itemAuthorizationDescription by lazy {
        DependentValueGetterBindable(viewModel.isItemAuthorizationAllowed, viewModel.isItemModificationAllowed, viewModel.ownerUsername, viewModel.itemAuthorizationModifiedDate) {
            val itemOwnerUsername = viewModel.ownerUsername.value
            val itemAuthorizationModifiedDate = viewModel.itemAuthorizationModifiedDate.value?.formattedDateTime(FX.locale)

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

        shortcut("ESC") {
            showPreviousScreen()
        }
    }

    private fun showPreviousScreen() {
        showScreenUnanimated(OverviewScreen::class)
    }

    override fun Node.setupMainContent() {
        scrollpane(fitToWidth = true, fitToHeight = false) {
            addClass(Theme.scrollPaneBorderlessStyle)

            form {
                paddingAll = marginM.value
                spacing = marginL.value

                setupDetailsSection()
                setupItemAuthorizationsSection()
                setupInformationSection()
                setupDeleteSection()
            }
        }.also {
            it.setScrollSpeed(ScrollSpeed.MEDIUM)
        }
    }

    private fun Form.setupDetailsSection() {
        vbox {
            textLabelHeadline1(titleProperty)

            fieldset(labelPosition = Orientation.VERTICAL) {
                paddingTop = marginM.value
                setupTitleField()
            }

            textLabelHeadline1(messages["itemdetail_details_header"]) {
                paddingTop = marginL.value
            }

            fieldset(labelPosition = Orientation.VERTICAL) {
                paddingTop = marginM.value
                spacing = marginS.value

                setupUsernameField()
                setupPasswordField()
                setupUrlField()
                setupNotesField()
            }

            hbox {
                paddingTop = marginM.value
                spacing = marginM.value

                setupSaveButton()

                createCancelButton {
                    showPreviousScreen()
                }
            }
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
                bindInput(this@ItemDetailScreen, viewModel.title)

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
                bindInput(this@ItemDetailScreen, viewModel.username)
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
            inputField.bindInput(this@ItemDetailScreen, viewModel.password)
        }
    }

    private fun Fieldset.setupUrlField() {
        field(messages["itemdetail_url_hint"], orientation = Orientation.VERTICAL) {
            textfield {
                bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
                bindInput(this@ItemDetailScreen, viewModel.url)
            }
        }
    }

    private fun Fieldset.setupNotesField() {
        vbox {
            alignment = Pos.CENTER_RIGHT

            field(messages["itemdetail_notes_hint"], orientation = Orientation.VERTICAL) {
                textarea {
                    bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
                    bindInput(this@ItemDetailScreen, viewModel.notes)

                    prefRowCount = 5

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

            bindEnabled(this@ItemDetailScreen, isItemModified, viewModel.isItemModificationAllowed)

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
            textLabelHeadline1(messages["itemdetail_authorizations_header"])

            textLabelBody1 {
                paddingTop = marginS.value

                bindTextAndVisibility(this@ItemDetailScreen, itemAuthorizationDescription)
            }

            vbox {
                paddingTop = marginM.value

                jfxButtonRaised(messages["itemdetail_authorizations_button_text"]) {
                    isEnabled = viewModel.isItemAuthorizationAvailable

                    action {
                        // Explicitly set the item ID to avoid `null` is passed for newly created item because `params` still does not contain an ID
                        showScreenUnanimated(ItemAuthorizationsDetailScreen::class, parameters = mapOf("itemId" to viewModel.id.value))
                    }
                }

                bindVisibility(this@ItemDetailScreen, viewModel.isItemAuthorizationAllowed)
            }

            textLabelBody2(messages["itemdetail_authorizations_footer_teaser"]) {
                paddingTop = marginS.value

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
            textLabelHeadline1(messages["itemdetail_information_header"])

            vbox {
                paddingTop = marginS.value
                spacing = marginS.value

                createInformationView(messages["itemdetail_id_title"]) {
                    style {
                        fontFamily = "monospace"
                    }

                    bindTextAndVisibility(this@ItemDetailScreen, viewModel.id)
                }

                createInformationView(messages["itemdetail_modified_title"]) {
                    bindTextAndVisibility(this@ItemDetailScreen, viewModel.modified) {
                        it?.formattedDateTime(FX.locale)
                    }
                }

                createInformationView(messages["itemdetail_created_title"]) {
                    bindTextAndVisibility(this@ItemDetailScreen, viewModel.created) {
                        it?.formattedDateTime(FX.locale)
                    }
                }
            }

            bindVisibility(this@ItemDetailScreen, viewModel.isNewItem) { isNewItem ->
                !isNewItem
            }
        }
    }

    private fun Node.setupDeleteSection() {
        vbox {
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
            bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)

            action {
                deleteClicked()
            }
        }
    }

    private fun deleteClicked() {
        launchRequestSending(
            handleSuccess = {
                showInformation(messages["itemdetail_delete_successful_message"])
                showPreviousScreen()
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
