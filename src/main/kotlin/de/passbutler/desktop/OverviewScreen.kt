package de.passbutler.desktop

import de.passbutler.common.ItemViewModel
import de.passbutler.common.Webservices
import de.passbutler.common.base.BindableObserver
import de.passbutler.common.base.formattedRelativeDateTime
import de.passbutler.common.database.models.UserType
import de.passbutler.common.ui.ListItemIdentifiable
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.base.createRelativeDateFormattingTranslations
import de.passbutler.desktop.ui.Drawables
import de.passbutler.desktop.ui.NavigationMenuView
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.addLifecycleObserver
import de.passbutler.desktop.ui.bindVisibility
import de.passbutler.desktop.ui.bottomDropShadow
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.createEmptyScreen
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxFloatingActionButtonRaised
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.paneWithDropShadow
import de.passbutler.desktop.ui.showScreenUnanimated
import de.passbutler.desktop.ui.textLabelBodyOrder1
import de.passbutler.desktop.ui.textLabelHeadlineOrder2
import de.passbutler.desktop.ui.textLabelSubtitleOrder1
import de.passbutler.desktop.ui.vectorDrawableIcon
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections.observableArrayList
import javafx.collections.ListChangeListener
import javafx.collections.transformation.FilteredList
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.layout.StackPane
import javafx.scene.text.FontWeight
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.tinylog.kotlin.Logger
import tornadofx.FX
import tornadofx.FX.Companion.messages
import tornadofx.action
import tornadofx.addClass
import tornadofx.borderpane
import tornadofx.cache
import tornadofx.center
import tornadofx.contextmenu
import tornadofx.get
import tornadofx.hbox
import tornadofx.insets
import tornadofx.item
import tornadofx.left
import tornadofx.listview
import tornadofx.onDoubleClick
import tornadofx.onLeftClick
import tornadofx.paddingBottom
import tornadofx.paddingLeft
import tornadofx.paddingRight
import tornadofx.paddingTop
import tornadofx.putString
import tornadofx.right
import tornadofx.select
import tornadofx.selectedItem
import tornadofx.stackpane
import tornadofx.style
import tornadofx.textfield
import tornadofx.top
import tornadofx.vbox

class OverviewScreen : NavigationMenuView(messages["overview_title"], navigationMenuItems = createDefaultNavigationMenu()), RequestSending {

    private val viewModel by injectWithPrivateScope<OverviewViewModel>()

    private var toolbarRegistrationContainer: Node? = null
    private var toolbarSynchronizationContainer: Node? = null
    private var toolbarSynchronizationButton: Node? = null
    private var toolbarSynchronizationSubtitle: Label? = null

    private var filterTextField: TextField? = null

    private var listView: ListView<ItemEntry>? = null
    private var emptyScreenView: Node? = null

    private val unfilteredItemEntries = observableArrayList<ItemEntry>()
    private val itemEntries = FilteredList(unfilteredItemEntries)

    private var updateToolbarJob: Job? = null
    private var synchronizeDataRequestSendingJob: Job? = null

    private val webservicesInitializedObserver: BindableObserver<Webservices?> = {
        if (it != null && !webservicesInitialized) {
            synchronizeData(userTriggered = false)
            webservicesInitialized = true
        }
    }

    private var webservicesInitialized = false

    private val itemViewModelsObserver: BindableObserver<List<ItemViewModel>> = { newUnfilteredItemViewModels ->
        // Only show non-deleted items
        val newItemViewModels = newUnfilteredItemViewModels.filter { !it.deleted }
        Logger.debug("newItemViewModels.size = ${newItemViewModels.size}")

        val newItemEntries = newItemViewModels
            .map { ItemEntry(it) }
            .sorted()

        unfilteredItemEntries.setAll(newItemEntries)

        val showEmptyScreen = newItemEntries.isEmpty()
        emptyScreenView?.isVisible = showEmptyScreen
    }

    init {
        setupRootView()

        shortcut("ESC") {
            if (filterTextField?.text != "") {
                // For the first step reset filter text
                filterTextField?.text = ""
            } else {
                // If the filter text was already reset, clear focus on filter text field
                root.requestFocus()
            }
        }
    }

    override fun Node.setupMainContent() {
        borderpane {
            center {
                stackpane {
                    listView = createListView()
                    emptyScreenView = createEmptyScreen(messages["overview_empty_screen_title"], messages["overview_empty_screen_description"])

                    setupAddButton()
                }
            }

            // Draw afterwards to apply drop shadow
            top {
                setupToolbar()
            }
        }
    }

    private fun Node.setupToolbar() {
        paneWithDropShadow(bottomDropShadow()) {
            setupToolbarContent()
        }
    }

    private fun Node.setupToolbarContent() {
        borderpane {
            addClass(Theme.toolbarStyle)

            left {
                setupToolbarFilterContainer()
            }

            right {
                stackpane {
                    toolbarRegistrationContainer = createToolbarRegistrationContainer()
                    toolbarSynchronizationContainer = createToolbarSynchronizationContainer()
                }
            }
        }
    }

    private fun Node.setupToolbarFilterContainer() {
        vbox {
            alignment = Pos.CENTER_LEFT

            filterTextField = textfield {
                promptText = messages["general_search"]

                textProperty().addListener { _, _, newValue ->
                    val filterTermAvailable = newValue.isNotEmpty()
                    val newPredicate: ((ItemEntry) -> Boolean)? = if (filterTermAvailable) {
                        { it.itemViewModel.title?.contains(newValue, ignoreCase = true) ?: false }
                    } else {
                        null
                    }

                    itemEntries.setPredicate(newPredicate)
                }

                shortcut("Shortcut+F") {
                    requestFocus()
                }
            }
        }
    }

    private fun Node.createToolbarRegistrationContainer(): Node {
        return vbox {
            alignment = Pos.CENTER_RIGHT

            // Hidden by default
            isVisible = false

            textLabelBodyOrder1(messages["drawer_header_usertype_local"]) {
                addClass(Theme.backgroundPressableStyle)

                style {
                    fontWeight = FontWeight.BOLD
                }

                graphic = vectorDrawableIcon(Drawables.ICON_ACCOUNT_CIRCLE)
                graphicTextGap = marginS.value

                onLeftClick {
                    showScreenUnanimated(RegisterLocalUserScreen::class)
                }
            }
        }
    }

    private fun Node.createToolbarSynchronizationContainer(): Node {
        val synchronizeDataAction = {
            if (viewModel.loggedInUserViewModel?.webservices?.value != null) {
                synchronizeData(userTriggered = true)
            }
        }

        return vbox {
            alignment = Pos.CENTER_RIGHT

            // Hidden by default
            isVisible = false

            toolbarSynchronizationButton = textLabelBodyOrder1(messages["overview_sync_button_title"]) {
                addClass(Theme.backgroundPressableStyle)

                style {
                    fontWeight = FontWeight.BOLD
                }

                graphic = vectorDrawableIcon(Drawables.ICON_REFRESH)
                graphicTextGap = marginS.value

                onLeftClick(action = synchronizeDataAction)
            }

            toolbarSynchronizationSubtitle = textLabelBodyOrder1 {
                paddingTop = marginS.value
            }

            shortcut("Shortcut+R") {
                synchronizeDataAction.invoke()
            }
        }
    }

    private fun Node.createListView(): ListView<ItemEntry> {
        return listview(itemEntries) {
            addClass(Theme.listViewSelectableCellStyle)
            addClass(Theme.listViewPressableCellStyle)

            cellFormat {
                graphic = cache {
                    createItemEntryView(this@cellFormat)
                }
            }

            items.addListener(ListChangeListener {
                val shownItemSize = it.list.size

                // Automatically focus first element if the filter is active
                if (itemEntries.predicate != null && shownItemSize == 1) {
                    selectionModel?.selectFirst()
                }
            })

            shortcut("ENTER") {
                showSelectedItem()
            }

            shortcut("Shortcut+B") {
                copyUsernameOfSelectedItem()
            }

            shortcut("Shortcut+C") {
                copyPasswordOfSelectedItem()
            }

            shortcut("Shortcut+U") {
                openUrlOfSelectedItem()
            }
        }
    }

    private fun Node.createItemEntryView(listCell: ListCell<ItemEntry>): Node {
        return createGenericItemEntryView(listCell) {
            onDoubleClick {
                showSelectedItem()
            }

            contextmenu {
                item(messages["overview_item_context_menu_copy_username"]).action {
                    copyUsernameOfSelectedItem()
                }

                item(messages["overview_item_context_menu_copy_password"]).action {
                    copyPasswordOfSelectedItem()
                }

                item(messages["overview_item_context_menu_copy_url"]).action {
                    copyUrlOfSelectedItem()
                }

                item(messages["overview_item_context_menu_open_url"]).action {
                    openUrlOfSelectedItem()
                }
            }
        }
    }

    private fun showSelectedItem() {
        listView?.selectedItem?.let { selectedItem ->
            showScreenUnanimated(ItemDetailScreen::class, parameters = mapOf("itemId" to selectedItem.itemViewModel.id))
        }
    }

    private fun copyUsernameOfSelectedItem() {
        copyItemInformationToClipboard(listView?.selectedItem?.itemViewModel?.itemData?.username)
    }

    private fun copyPasswordOfSelectedItem() {
        copyItemInformationToClipboard(listView?.selectedItem?.itemViewModel?.itemData?.password)
    }

    private fun copyUrlOfSelectedItem() {
        copyItemInformationToClipboard(listView?.selectedItem?.itemViewModel?.itemData?.url)
    }

    private fun copyItemInformationToClipboard(itemInformation: String?) {
        if (itemInformation?.isNotBlank() == true) {
            clipboard.putString(itemInformation)
            showInformation(messages["overview_item_information_clipboard_successful_message"])
        } else {
            showError(messages["overview_item_information_clipboard_failed_empty_title"])
        }
    }

    private fun openUrlOfSelectedItem() {
        val url = listView?.selectedItem?.itemViewModel?.itemData?.url

        if (url?.isNotBlank() == true) {
            hostServices.showDocument(url)
        } else {
            showError(messages["overview_open_url_failed_title"])
        }
    }

    private fun StackPane.setupAddButton() {
        val addItemAction = {
            showScreenUnanimated(ItemDetailScreen::class, parameters = mapOf("itemId" to null))
        }

        hbox {
            alignment = Pos.BOTTOM_RIGHT
            paddingRight = marginM.value
            paddingBottom = marginM.value

            // Do not consume clicks on lower panes
            isPickOnBounds = false

            jfxFloatingActionButtonRaised("＋") {
                setOnAction {
                    addItemAction.invoke()
                }
            }

            shortcut("Shortcut+N") {
                addItemAction.invoke()
            }
        }
    }

    override fun onDock() {
        super.onDock()

        viewModel.loggedInUserViewModel?.webservices?.addLifecycleObserver(this, true, webservicesInitializedObserver)
        viewModel.loggedInUserViewModel?.itemViewModels?.addLifecycleObserver(this, true, itemViewModelsObserver)

        viewModel.loggedInUserViewModel?.loggedInStateStorage?.let { loggedInStateStorageBindable ->
            toolbarRegistrationContainer?.bindVisibility(this@OverviewScreen, loggedInStateStorageBindable) { loggedInStateStorageValue ->
                loggedInStateStorageValue?.userType == UserType.LOCAL
            }

            toolbarSynchronizationContainer?.bindVisibility(this@OverviewScreen, loggedInStateStorageBindable) { loggedInStateStorageValue ->
                loggedInStateStorageValue?.userType == UserType.REMOTE
            }
        }

        viewModel.loggedInUserViewModel?.loggedInStateStorage?.addLifecycleObserver(this, true) {
            updateToolbarSubtitle()
        }

        updateToolbarJob?.cancel()
        updateToolbarJob = launch {
            while (isActive) {
                Logger.debug("Update relative time in toolbar subtitle")

                // Update relative time in toolbar periodically
                updateToolbarSubtitle()
                delay(10_000)
            }
        }

        Platform.runLater {
            filterTextField?.requestFocus()
        }
    }

    private fun updateToolbarSubtitle() {
        toolbarSynchronizationSubtitle?.text = if (viewModel.loggedInUserViewModel?.userType == UserType.REMOTE) {
            val newDate = viewModel.loggedInUserViewModel?.lastSuccessfulSyncDate
            val relativeDateFormattingTranslations = createRelativeDateFormattingTranslations(this)
            val formattedLastSuccessfulSync = newDate?.formattedRelativeDateTime(relativeDateFormattingTranslations) ?: messages["overview_last_sync_never"]
            messages["overview_last_sync_subtitle"].format(formattedLastSuccessfulSync)
        } else {
            null
        }
    }

    private fun synchronizeData(userTriggered: Boolean) {
        val synchronizeDataRequestRunning = synchronizeDataRequestSendingJob?.isActive ?: false

        if (!synchronizeDataRequestRunning) {
            synchronizeDataRequestSendingJob = launchRequestSending(
                handleSuccess = {
                    // Only show user feedback if it was user triggered to avoid confusing the user
                    if (userTriggered) {
                        showInformation(messages["overview_sync_successful_message"])
                    }
                },
                handleFailure = {
                    // Only show user feedback if it was user triggered to avoid confusing the user
                    if (userTriggered) {
                        showError(messages["overview_sync_failed_message"])
                    }
                },
                handleLoadingChanged = { isLoading ->
                    toolbarSynchronizationButton?.isDisable = isLoading
                }
            ) {
                viewModel.synchronizeData()
            }
        } else {
            Logger.debug("The synchronize data request is already running - skip call")
        }
    }
}

class ItemEntry(val itemViewModel: ItemViewModel) : ListItemIdentifiable, Comparable<ItemEntry> {
    override val listItemId: String
        get() = itemViewModel.id

    val titleProperty = SimpleStringProperty(itemViewModel.title)
    val subtitleProperty = SimpleStringProperty(itemViewModel.itemData?.username?.takeIf { it.isNotEmpty() } ?: messages["overview_item_subtitle_username_missing"])

    override fun compareTo(other: ItemEntry): Int {
        return compareValuesBy(this, other) { it.itemViewModel.title?.lowercase(FX.locale) }
    }
}

fun Node.createGenericItemEntryView(listCell: ListCell<ItemEntry>, op: Node.() -> Unit): Node {
    return hbox {
        alignment = Pos.CENTER_LEFT
        padding = insets(marginM.value, marginS.value)

        vectorDrawableIcon(Drawables.ICON_TEXT_SNIPPET)

        vbox {
            paddingLeft = marginM.value

            textLabelHeadlineOrder2(listCell.itemProperty().select { it.titleProperty })
            textLabelSubtitleOrder1(listCell.itemProperty().select { it.subtitleProperty }) {
                paddingTop = marginS.value
            }
        }

        op(this)
    }
}
