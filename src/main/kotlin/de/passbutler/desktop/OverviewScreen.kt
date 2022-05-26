package de.passbutler.desktop

import de.passbutler.common.ItemViewModel
import de.passbutler.common.Webservices
import de.passbutler.common.base.BindableObserver
import de.passbutler.common.base.formattedRelativeDateTime
import de.passbutler.common.database.models.UserType
import de.passbutler.common.ui.ListItemIdentifiable
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ItemEditingViewModelWrapper.Companion.PARAMETER_ITEM_ID
import de.passbutler.desktop.ItemListViewSetupping.ListConfiguration
import de.passbutler.desktop.base.UrlExtensions
import de.passbutler.desktop.base.createRelativeDateFormattingTranslations
import de.passbutler.desktop.ui.Drawables
import de.passbutler.desktop.ui.NavigationMenuView
import de.passbutler.desktop.ui.ScreenPresenting
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.addLifecycleObserver
import de.passbutler.desktop.ui.bindVisibility
import de.passbutler.desktop.ui.bottomDropShadow
import de.passbutler.desktop.ui.copyToClipboard
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.createEmptyScreen
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxButton
import de.passbutler.desktop.ui.jfxFloatingActionButton
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.openBrowser
import de.passbutler.desktop.ui.paneWithDropShadow
import de.passbutler.desktop.ui.showScreenUnanimated
import de.passbutler.desktop.ui.textLabelBodyOrder1
import de.passbutler.desktop.ui.textLabelHeadlineOrder2
import de.passbutler.desktop.ui.textLabelSubtitleOrder1
import de.passbutler.desktop.ui.vectorDrawableIcon
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections.observableArrayList
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
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
import tornadofx.managedWhen
import tornadofx.onDoubleClick
import tornadofx.onLeftClick
import tornadofx.paddingBottom
import tornadofx.paddingLeft
import tornadofx.paddingRight
import tornadofx.paddingTop
import tornadofx.right
import tornadofx.select
import tornadofx.selectedItem
import tornadofx.stackpane
import tornadofx.style
import tornadofx.textfield
import tornadofx.top
import tornadofx.vbox
import tornadofx.visibleWhen

class OverviewScreen : NavigationMenuView(messages["overview_title"], navigationMenuItems = createDefaultNavigationMenu()), RequestSending, ItemListViewSetupping {

    private val viewModel by injectWithPrivateScope<OverviewViewModel>()

    private var toolbarRegistrationContainer: Node? = null
    private var toolbarSynchronizationContainer: Node? = null
    private var toolbarSynchronizationButton: Node? = null
    private var toolbarSynchronizationSubtitle: Label? = null

    private var filterTextField: TextField? = null

    override var listView: ListView<ItemEntry>? = null
    override var emptyScreenView: Node? = null

    override val unfilteredItemEntries = observableArrayList<ItemEntry>()
    override val filteredItemEntries = FilteredList(unfilteredItemEntries)

    private var updateToolbarJob: Job? = null
    private var synchronizeDataRequestSendingJob: Job? = null

    private val webservicesInitializedObserver: BindableObserver<Webservices?> = {
        if (it != null && !webservicesInitialized) {
            synchronizeData(userTriggered = false)
            webservicesInitialized = true
        }
    }

    private var webservicesInitialized = false

    override val itemViewModelsObserver: BindableObserver<List<ItemViewModel>> = { newUnfilteredItemViewModels ->
        updateItemViewModels(newUnfilteredItemViewModels, ListConfiguration.ShowOnlyNormalItems)
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
                    emptyScreenView = createEmptyScreen(messages["overview_empty_screen_headline"], messages["overview_empty_screen_description"])

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
                    toolbarRegistrationContainer = createToolbarRegisterLocalUserContainer()
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
                    updateFilterPredicate(newValue)
                }

                shortcut("Shortcut+F") {
                    requestFocus()
                }
            }
        }
    }

    private fun Node.createToolbarRegisterLocalUserContainer(): Node {
        return vbox {
            alignment = Pos.CENTER_RIGHT

            // Hidden by default
            isVisible = false

            jfxButton(messages["drawer_header_register_local_user_button_title"]) {
                addClass(Theme.buttonSecondaryStyle)

                graphic = vectorDrawableIcon(Drawables.ICON_LOGIN)

                action {
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
        return listview(filteredItemEntries) {
            addClass(Theme.listViewSelectableCellStyle)
            addClass(Theme.listViewPressableCellStyle)

            cellFormat {
                graphic = cache {
                    createItemEntryView(this@cellFormat)
                }
            }

            items.addListener(ListChangeListener { listChange ->
                onListChanged(listChange)
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

                item(messages["overview_item_context_menu_delete"]).action {
                    deleteSelectedItem()
                }
            }
        }
    }

    override fun showSelectedItem() {
        listView?.selectedItem?.let { selectedItem ->
            showScreenUnanimated(ItemDetailScreen::class, parameters = mapOf(PARAMETER_ITEM_ID to selectedItem.itemViewModel.id))
        }
    }

    private fun copyUsernameOfSelectedItem() {
        copyToClipboard(this, listView?.selectedItem?.itemViewModel?.itemData?.username)
    }

    private fun copyPasswordOfSelectedItem() {
        copyToClipboard(this, listView?.selectedItem?.itemViewModel?.itemData?.password)
    }

    private fun copyUrlOfSelectedItem() {
        copyToClipboard(this, listView?.selectedItem?.itemViewModel?.itemData?.url)
    }

    private fun openUrlOfSelectedItem() {
        val url = listView?.selectedItem?.itemViewModel?.itemData?.url?.takeIf { it.isNotEmpty() && it.isNotBlank() }

        when {
            url != null && UrlExtensions.isNetworkUrl(url) -> {
                Logger.debug("Open valid network URL: '$url'")
                openBrowser(url)
            }
            url != null && UrlExtensions.obtainScheme(url) == null -> {
                val fallbackScheme = "https://"
                val correctedUrl = fallbackScheme + url

                Logger.debug("Open incomplete network URL '$url' with corrected scheme: '$correctedUrl'")
                openBrowser(correctedUrl)
            }
            else -> {
                showError(messages["overview_open_url_failed_title"])
            }
        }
    }

    private fun deleteSelectedItem() {
        listView?.selectedItem?.itemViewModel?.let { selectedItemViewModel ->
            val itemEditingViewModel = selectedItemViewModel.createEditingViewModel()

            launchRequestSending(
                handleSuccess = { showInformation(messages["itemdetail_delete_successful_message"]) },
                handleFailure = { showError(messages["itemdetail_delete_failed_general_title"]) }
            ) {
                itemEditingViewModel.delete()
            }
        }
    }

    private fun StackPane.setupAddButton() {
        val addItemAction = {
            showScreenUnanimated(ItemDetailScreen::class, parameters = mapOf(PARAMETER_ITEM_ID to null))
        }

        hbox {
            alignment = Pos.BOTTOM_RIGHT
            paddingRight = marginM.value
            paddingBottom = marginM.value

            // Do not consume clicks on lower panes
            isPickOnBounds = false

            jfxFloatingActionButton("\uFF0B") {
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

interface ItemListViewSetupping : ScreenPresenting {
    var listView: ListView<ItemEntry>?
    var emptyScreenView: Node?

    val unfilteredItemEntries: ObservableList<ItemEntry>
    val filteredItemEntries: FilteredList<ItemEntry>

    val itemViewModelsObserver: BindableObserver<List<ItemViewModel>>

    fun updateItemViewModels(newUnfilteredItemViewModels: List<ItemViewModel>, listConfiguration: ListConfiguration) {
        val newItemViewModels = newUnfilteredItemViewModels.filter {
            when (listConfiguration) {
                ListConfiguration.ShowOnlyNormalItems -> !it.deleted
                ListConfiguration.ShowOnlyDeletedItems -> it.deleted
            }
        }
        Logger.debug("newItemViewModels.size = ${newItemViewModels.size}")

        val newItemEntries = newItemViewModels
            .map { ItemEntry(it) }
            .sorted()

        unfilteredItemEntries.setAll(newItemEntries)

        val showEmptyScreen = newItemEntries.isEmpty()
        emptyScreenView?.isVisible = showEmptyScreen
    }

    fun updateFilterPredicate(newValue: String) {
        val filterTermAvailable = newValue.isNotEmpty()
        val newPredicate: ((ItemEntry) -> Boolean)? = if (filterTermAvailable) {
            { it.itemViewModel.title?.contains(newValue, ignoreCase = true) ?: false }
        } else {
            null
        }

        filteredItemEntries.setPredicate(newPredicate)
    }

    fun ListView<*>.onListChanged(listChange: ListChangeListener.Change<*>) {
        val newListItems = listChange.list
        val shownItemSize = newListItems.size

        // Automatically focus first element if the filter is active
        if (filteredItemEntries.predicate != null && shownItemSize == 1) {
            selectionModel?.selectFirst()
        }
    }

    fun showSelectedItem()

    enum class ListConfiguration {
        ShowOnlyNormalItems,
        ShowOnlyDeletedItems
    }
}

class ItemEntry(val itemViewModel: ItemViewModel) : ListItemIdentifiable, Comparable<ItemEntry> {
    override val listItemId: String
        get() = itemViewModel.id

    val titleProperty = SimpleStringProperty(itemViewModel.title)
    val subtitleProperty = SimpleStringProperty(itemViewModel.itemData?.username?.takeIf { it.isNotEmpty() } ?: messages["overview_item_subtitle_username_missing"])
    val isSharedItemProperty = SimpleBooleanProperty(itemViewModel.isSharedItem)

    override fun compareTo(other: ItemEntry): Int {
        return compareValuesBy(this, other) { it.itemViewModel.title?.lowercase(FX.locale) }
    }
}

fun Node.createGenericItemEntryView(listCell: ListCell<ItemEntry>, op: Node.() -> Unit): Node {
    return hbox {
        alignment = Pos.CENTER_LEFT
        padding = insets(marginM.value, marginS.value)

        vectorDrawableIcon(Drawables.ICON_TEXT_SNIPPET) {
            addClass(Theme.vectorDrawableIconAccent)

            managedWhen(listCell.itemProperty().select { it.isSharedItemProperty.not() })
            visibleWhen(listCell.itemProperty().select { it.isSharedItemProperty.not() })
        }

        vectorDrawableIcon(Drawables.ICON_GROUP) {
            addClass(Theme.vectorDrawableIconAccent)

            managedWhen(listCell.itemProperty().select { it.isSharedItemProperty })
            visibleWhen(listCell.itemProperty().select { it.isSharedItemProperty })
        }

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
