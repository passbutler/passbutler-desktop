package de.passbutler.desktop

import de.passbutler.common.ItemEditingViewModel.Companion.NOTES_MAXIMUM_CHARACTERS
import de.passbutler.common.base.Bindable
import de.passbutler.common.base.DependentValueGetterBindable
import de.passbutler.common.base.formattedDateTime
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ItemEditingViewModelWrapper.Companion.PARAMETER_ITEM_ID
import de.passbutler.desktop.ui.Drawables
import de.passbutler.desktop.ui.FormFieldValidatorRule
import de.passbutler.desktop.ui.FormValidating
import de.passbutler.desktop.ui.NavigationMenuFragment
import de.passbutler.desktop.ui.PasswordGeneratorDialog
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.ThemeFonts
import de.passbutler.desktop.ui.bind
import de.passbutler.desktop.ui.bindEnabled
import de.passbutler.desktop.ui.bindInput
import de.passbutler.desktop.ui.bindTextAndVisibility
import de.passbutler.desktop.ui.bindVisibility
import de.passbutler.desktop.ui.copyToClipboard
import de.passbutler.desktop.ui.createCancelButton
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.createInformationView
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.isEnabled
import de.passbutler.desktop.ui.jfxButton
import de.passbutler.desktop.ui.marginL
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.marginXS
import de.passbutler.desktop.ui.scrollPane
import de.passbutler.desktop.ui.showConfirmDialog
import de.passbutler.desktop.ui.showScreenUnanimated
import de.passbutler.desktop.ui.sp
import de.passbutler.desktop.ui.textLabelBodyOrder1
import de.passbutler.desktop.ui.textLabelBodyOrder2
import de.passbutler.desktop.ui.textLabelCaption
import de.passbutler.desktop.ui.textLabelHeadlineOrder1
import de.passbutler.desktop.ui.unmaskablePasswordField
import de.passbutler.desktop.ui.vectorDrawableIcon
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.FontWeight
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
import tornadofx.hgrow
import tornadofx.hyperlink
import tornadofx.onLeftClick
import tornadofx.paddingBottom
import tornadofx.paddingTop
import tornadofx.style
import tornadofx.textarea
import tornadofx.textfield
import tornadofx.textflow
import tornadofx.vbox

class ItemDetailScreen : NavigationMenuFragment(navigationMenuItems = createDefaultNavigationMenu()), FormValidating, RequestSending {

    private val validationContext = ValidationContext()

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

    private lateinit var passwordField: TextField

    init {
        setupRootView()

        shortcut("ESC") {
            showPreviousScreen()
        }

        shortcut("Shortcut+S") {
            if (viewModel.isItemModificationAllowed.value && isItemModified.value) {
                saveClicked()
            }
        }
    }

    private fun showPreviousScreen() {
        val showPreviousScreenAction = {
            val screenClass = if (viewModel.deleted.value == true) {
                RecycleBinScreen::class
            } else {
                OverviewScreen::class
            }

            showScreenUnanimated(screenClass)
        }

        if (viewModel.isItemModificationAllowed.value && isItemModified.value) {
            showDiscardChangesConfirmDialog {
                showPreviousScreenAction()
            }
        } else {
            showPreviousScreenAction()
        }
    }

    override fun onNavigationItemClicked(clickedAction: () -> Unit) {
        if (viewModel.isItemModificationAllowed.value && isItemModified.value) {
            showDiscardChangesConfirmDialog {
                super.onNavigationItemClicked(clickedAction)
            }
        } else {
            super.onNavigationItemClicked(clickedAction)
        }
    }

    override fun Node.setupMainContent() {
        scrollPane {
            form {
                spacing = marginL.value

                setupDetailsSection()
                setupItemAuthorizationsSection()
                setupInformationSection()

                if (viewModel.deleted.value == true) {
                    setupRestoreSection()
                } else {
                    setupDeleteSection()
                }
            }
        }
    }

    private fun Form.setupDetailsSection() {
        vbox {
            textLabelHeadlineOrder1(titleProperty)

            fieldset(labelPosition = Orientation.VERTICAL) {
                paddingTop = marginM.value
                setupTitleField()
            }

            textLabelHeadlineOrder1(messages["itemdetail_details_headline"]) {
                paddingTop = marginL.value
            }

            fieldset(labelPosition = Orientation.VERTICAL) {
                paddingTop = marginM.value
                spacing = marginS.value

                setupUsernameField()
                setupPasswordField()
                setupPasswordGeneratorText()
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
        field(messages["itemdetail_title_hint"], orientation = Orientation.VERTICAL) {
            textfield {
                // Same style as `Theme.textHeadline6Style`
                style {
                    fontSize = 20.sp
                    fontFamily = ThemeFonts.ROBOTO_MEDIUM
                }

                bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
                bindInput(this@ItemDetailScreen, viewModel.title)

                validationContext.validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["itemdetail_title_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Fieldset.setupUsernameField() {
        field(messages["itemdetail_username_hint"], orientation = Orientation.VERTICAL) {
            hbox(alignment = Pos.CENTER, spacing = marginS.value) {
                textfield {
                    hgrow = Priority.ALWAYS
                    bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
                    bindInput(this@ItemDetailScreen, viewModel.username)
                }

                vectorDrawableIcon(Drawables.ICON_CONTENT_COPY) {
                    addClass(Theme.vectorDrawableIconClickable)

                    onLeftClick {
                        copyToClipboard(this@ItemDetailScreen, viewModel.username.value)
                    }
                }
            }
        }
    }

    private fun Fieldset.setupPasswordField() {
        field(messages["itemdetail_password_hint"], orientation = Orientation.VERTICAL) {
            hbox(alignment = Pos.CENTER, spacing = marginS.value) {
                val unmaskablePasswordField = unmaskablePasswordField(viewModel.hidePasswordsEnabled) {
                    bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
                    bindInput(this@ItemDetailScreen, viewModel.password)
                }

                unmaskablePasswordField.apply {
                    hgrow = Priority.ALWAYS
                }

                passwordField = unmaskablePasswordField.wrappedPasswordField

                vectorDrawableIcon(Drawables.ICON_CONTENT_COPY) {
                    addClass(Theme.vectorDrawableIconClickable)

                    onLeftClick {
                        copyToClipboard(this@ItemDetailScreen, viewModel.password.value)
                    }
                }
            }
        }
    }

    private fun Fieldset.setupPasswordGeneratorText() {
        textflow {
            // Add some extra spacing to separate from next field
            paddingBottom = marginS.value

            bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)

            val generateWord = messages["itemdetail_password_generator_generate_word"]
            hyperlink(generateWord) {
                action {
                    showPasswordGeneratorDialog()
                }
            }

            val formattedText = messages["itemdetail_password_generator_text"].format(generateWord)
            val formattedTextAfterGenerateWord = formattedText.substringAfter(generateWord)
            textLabelBodyOrder1(formattedTextAfterGenerateWord)
        }
    }

    private fun showPasswordGeneratorDialog() {
        val passwordGeneratorDialog = PasswordGeneratorDialog(
            presentingFragment = this,
            positiveClickAction = { newPassword ->
                dismissDialog()

                viewModel.password.value = newPassword
                passwordField.requestFocus()
            },
            negativeClickAction = {
                dismissDialog()
            }
        )

        showDialog(passwordGeneratorDialog)
    }

    private fun Fieldset.setupUrlField() {
        field(messages["itemdetail_url_hint"], orientation = Orientation.VERTICAL) {
            hbox(alignment = Pos.CENTER, spacing = marginS.value) {
                textfield {
                    hgrow = Priority.ALWAYS
                    bindEnabled(this@ItemDetailScreen, viewModel.isItemModificationAllowed)
                    bindInput(this@ItemDetailScreen, viewModel.url)
                }

                vectorDrawableIcon(Drawables.ICON_CONTENT_COPY) {
                    addClass(Theme.vectorDrawableIconClickable)

                    onLeftClick {
                        copyToClipboard(this@ItemDetailScreen, viewModel.url.value)
                    }
                }
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
                    isWrapText = true

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

            textLabelBodyOrder2 {
                bindTextAndVisibility(this@ItemDetailScreen, viewModel.notes) { notesValue ->
                    val notesLength = notesValue.length.coerceIn(0, NOTES_MAXIMUM_CHARACTERS)
                    "$notesLength/$NOTES_MAXIMUM_CHARACTERS"
                }
            }
        }
    }

    private fun Node.setupSaveButton() {
        jfxButton(messages["itemdetail_save_button_title"]) {
            addClass(Theme.buttonPrimaryStyle)

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
            textLabelHeadlineOrder1(messages["itemdetail_authorizations_headline"])

            textLabelBodyOrder1 {
                paddingTop = marginS.value

                bindTextAndVisibility(this@ItemDetailScreen, itemAuthorizationDescription)
            }

            vbox {
                paddingTop = marginM.value

                jfxButton(messages["itemdetail_authorizations_button_text"]) {
                    addClass(Theme.buttonPrimaryStyle)

                    isEnabled = viewModel.isItemAuthorizationAvailable

                    action {
                        // Explicitly set the item ID to avoid `null` is passed for newly created item because `params` still does not contain an ID
                        showScreenUnanimated(ItemAuthorizationsDetailScreen::class, parameters = mapOf(PARAMETER_ITEM_ID to viewModel.id.value))
                    }
                }

                bindVisibility(this@ItemDetailScreen, viewModel.isItemAuthorizationAllowed)
            }

            textLabelCaption(messages["itemdetail_authorizations_footer_teaser"]) {
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
            textLabelHeadlineOrder1(messages["itemdetail_information_headline"])

            vbox {
                paddingTop = marginS.value
                spacing = marginS.value

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

                createInformationViewWithCopyIcon(messages["itemdetail_id_title"], viewModel.id)
            }

            bindVisibility(this@ItemDetailScreen, viewModel.isNewItem) { isNewItem ->
                !isNewItem
            }
        }
    }

    private fun Node.createInformationViewWithCopyIcon(title: String, informationBindable: Bindable<String?>): VBox {
        return vbox {
            textLabelBodyOrder1(title) {
                style {
                    fontWeight = FontWeight.BOLD
                }
            }

            hbox {
                alignment = Pos.CENTER_LEFT
                spacing = marginS.value

                textLabelBodyOrder2 {
                    paddingTop = marginXS.value

                    style {
                        fontFamily = "monospace"
                    }

                    bindTextAndVisibility(this@ItemDetailScreen, informationBindable)
                }

                vectorDrawableIcon(Drawables.ICON_CONTENT_COPY) {
                    addClass(Theme.vectorDrawableIconClickable)

                    onLeftClick {
                        copyToClipboard(this@ItemDetailScreen, informationBindable.value)
                    }
                }
            }
        }
    }

    private fun Node.setupDeleteSection() {
        vbox {
            spacing = marginM.value

            textLabelHeadlineOrder1(messages["itemdetail_delete_headline"])
            setupDeleteButton()

            bindVisibility(this@ItemDetailScreen, viewModel.isNewItem, viewModel.isItemModificationAllowed) { isNewItem, isItemModificationAllowed ->
                !isNewItem && isItemModificationAllowed
            }
        }
    }

    private fun Node.setupDeleteButton() {
        jfxButton(messages["itemdetail_delete_button_title"]) {
            addClass(Theme.buttonPrimaryStyle)

            action {
                deleteItemClicked()
            }
        }
    }

    private fun deleteItemClicked() {
        showConfirmDialog(
            title = messages["itemdetail_delete_confirmation_title"],
            positiveActionTitle = messages["itemdetail_delete_confirmation_button_title"],
            positiveClickAction = {
                deleteItem()
            }
        )
    }

    private fun deleteItem() {
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

    private fun Node.setupRestoreSection() {
        vbox {
            spacing = marginM.value

            textLabelHeadlineOrder1(messages["itemdetail_restore_headline"])
            setupRestoreButton()

            bindVisibility(this@ItemDetailScreen, viewModel.isNewItem, viewModel.isItemModificationAllowed) { isNewItem, isItemModificationAllowed ->
                !isNewItem && isItemModificationAllowed
            }
        }
    }

    private fun Node.setupRestoreButton() {
        jfxButton(messages["itemdetail_restore_button_title"]) {
            addClass(Theme.buttonPrimaryStyle)

            action {
                restoreItemClicked()
            }
        }
    }

    private fun restoreItemClicked() {
        showConfirmDialog(
            title = messages["itemdetail_restore_confirmation_title"],
            positiveActionTitle = messages["itemdetail_restore_confirmation_button_title"],
            positiveClickAction = {
                restoreItem()
            }
        )
    }

    private fun restoreItem() {
        launchRequestSending(
            handleSuccess = {
                showInformation(messages["itemdetail_restore_successful_message"])
                showPreviousScreen()
            },
            handleFailure = { showError(messages["itemdetail_restore_failed_general_title"]) }
        ) {
            viewModel.restore()
        }
    }

    override fun onDock() {
        super.onDock()

        bind(this, viewModel.isNewItem) { isNewItem ->
            title = if (isNewItem) {
                messages["itemdetail_title_new"]
            } else {
                messages["itemdetail_title_edit"]
            }
        }
    }

    private fun showDiscardChangesConfirmDialog(positiveClickAction: () -> Unit) {
        showConfirmDialog(
            title = messages["itemdetail_discard_changes_confirmation_title"],
            message = messages["itemdetail_discard_changes_confirmation_message"],
            positiveActionTitle = messages["general_discard"],
            positiveClickAction = positiveClickAction
        )
    }
}
