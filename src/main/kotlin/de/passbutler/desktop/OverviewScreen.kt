package de.passbutler.desktop

import de.passbutler.common.ItemViewModel
import de.passbutler.common.Webservices
import de.passbutler.common.base.BindableObserver
import de.passbutler.common.base.formattedDateTime
import de.passbutler.common.database.models.LoggedInStateStorage
import de.passbutler.common.database.models.UserType
import de.passbutler.common.ui.ListItemIdentifiable
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.Drawables
import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.bottomDropShadow
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.marginXS
import de.passbutler.desktop.ui.smallSVGIcon
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelHeadline
import javafx.collections.FXCollections.observableArrayList
import javafx.collections.transformation.FilteredList
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.tinylog.kotlin.Logger
import tornadofx.FX.Companion.messages
import tornadofx.addClass
import tornadofx.borderpane
import tornadofx.cache
import tornadofx.center
import tornadofx.get
import tornadofx.hbox
import tornadofx.insets
import tornadofx.left
import tornadofx.listview
import tornadofx.onChange
import tornadofx.onLeftClick
import tornadofx.paddingAll
import tornadofx.paddingLeft
import tornadofx.paddingTop
import tornadofx.pane
import tornadofx.right
import tornadofx.stackpane
import tornadofx.textfield
import tornadofx.top
import tornadofx.vbox

class OverviewScreen : NavigationMenuScreen(messages["overview_title"]), RequestSending {

    private val viewModel by injectWithPrivateScope<OverviewViewModel>()

    private var toolbarSynchronizationContainer: Node? = null
    private var toolbarSynchronizationSubtitle: Label? = null
    private var toolbarSynchronizationIcon: Node? = null

    private var listScreenLayout: ListView<ItemEntry>? = null
    private var emptyScreenLayout: Node? = null

    private val unfilteredItemEntries = observableArrayList<ItemEntry>()
    private val itemEntries = FilteredList(unfilteredItemEntries)

    private var updateToolbarJob: Job? = null
    private var synchronizeDataRequestSendingJob: Job? = null

    private val webservicesInitializedObserver: BindableObserver<Webservices?> = {
        if (it != null) {
            synchronizeData(userTriggered = false)
        }
    }

    private val itemViewModelsObserver: BindableObserver<List<ItemViewModel>> = { newUnfilteredItemViewModels ->
        // Only show non-deleted items
        val newItemViewModels = newUnfilteredItemViewModels.filter { !it.deleted }
        Logger.debug("newItemViewModels.size = ${newItemViewModels.size}")

        val newItemEntries = newItemViewModels
            .map { ItemEntry(it) }
            .sorted()

        unfilteredItemEntries.setAll(newItemEntries)

        val showEmptyScreen = newItemEntries.isEmpty()
        emptyScreenLayout?.isVisible = showEmptyScreen
    }

    private val loggedInStateStorageObserver: BindableObserver<LoggedInStateStorage?> = {
        updateToolbarSynchronizationContainer()
    }

    override fun Node.createMainContent() {
        borderpane {
            center {
                stackpane {
                    listScreenLayout = createListScreenLayout()
                    emptyScreenLayout = createEmptyScreenLayout()
                }
            }

            // Draw afterwards to apply drop shadow
            top {
                setupToolbar()
            }
        }
    }

    private fun Node.setupToolbar() {
        stackpane {
            pane {
                addClass(Theme.backgroundStyle)
                effect = bottomDropShadow()
            }

            setupToolbarContent()
        }
    }

    private fun Node.setupToolbarContent() {
        borderpane {
            padding = insets(marginM.value, marginS.value)

            left {
                vbox {
                    alignment = Pos.CENTER_LEFT

                    setupFilterTextfield()
                }
            }

            right {
                toolbarSynchronizationContainer = createToolbarSynchronizationContainer()
            }
        }
    }

    private fun Node.setupFilterTextfield() {
        textfield {
            promptText = messages["overview_search_hint"]

            textProperty().addListener { _, _, newValue ->
                val newPredicate: ((ItemEntry) -> Boolean)? = if (newValue.isNullOrEmpty()) {
                    null
                } else {
                    { it.itemViewModel.title?.contains(newValue, ignoreCase = true) ?: false }
                }

                itemEntries.setPredicate(newPredicate)
            }
        }
    }

    private fun Node.createToolbarSynchronizationContainer(): Node {
        return vbox {
            alignment = Pos.CENTER_RIGHT

            // Hidden by default
            isVisible = false

            toolbarSynchronizationIcon = smallSVGIcon(Drawables.ICON_REFRESH.svgPath) {
                onLeftClick {
                    if (viewModel.loggedInUserViewModel?.webservices?.value != null) {
                        synchronizeData(userTriggered = true)
                    }
                }
            }

            toolbarSynchronizationSubtitle = textLabelBody1 {
                paddingTop = marginXS.value
            }
        }
    }

    private fun Node.createListScreenLayout(): ListView<ItemEntry> {
        return listview {
            cellFormat { entry ->
                // TODO: Update does not work
                graphic = cache(key = entry.listItemId) {
                    createItemEntryView(entry)
                }
            }

            selectionModel.selectedItemProperty().onChange {
                Logger.debug("Selected $it")
            }
        }
    }

    private fun Node.createItemEntryView(entry: ItemEntry): Node {
        return hbox {
            alignment = Pos.CENTER_LEFT
            padding = insets(marginM.value, marginXS.value)

            smallSVGIcon(Drawables.ICON_FAVORITE.svgPath)

            vbox {
                paddingLeft = marginM.value

                textLabelHeadline(entry.itemViewModel.title ?: "")
                textLabelBody1(entry.itemViewModel.subtitle) {
                    paddingTop = marginS.value
                }
            }
        }
    }

    private fun Node.createEmptyScreenLayout(): Node {
        return vbox {
            paddingAll = marginM.value
            alignment = Pos.CENTER

            textLabelHeadline(messages["overview_empty_screen_title"])
            textLabelBody1(messages["overview_empty_screen_description"]) {
                paddingTop = marginS.value
            }

            // Obtain focus from search textfield
            onLeftClick {
                requestFocus()
            }
        }
    }

    override fun onDock() {
        super.onDock()

        listScreenLayout?.items = itemEntries

        viewModel.loggedInUserViewModel?.webservices?.addObserver(this, true, webservicesInitializedObserver)
        viewModel.loggedInUserViewModel?.itemViewModels?.addObserver(this, true, itemViewModelsObserver)
        viewModel.loggedInUserViewModel?.loggedInStateStorage?.addObserver(this, true, loggedInStateStorageObserver)

        updateToolbarJob?.cancel()
        updateToolbarJob = launch {
            while (isActive) {
                Logger.debug("Update relative time in toolbar subtitle")

                // Update relative time in toolbar every minute
                updateToolbarSubtitle()
                delay(60_000)
            }
        }
    }

    private fun updateToolbarSynchronizationContainer() {
        toolbarSynchronizationContainer?.apply {
            isVisible = viewModel.loggedInUserViewModel?.userType == UserType.REMOTE
        }
    }

    private fun updateToolbarSubtitle() {
        toolbarSynchronizationSubtitle?.text = if (viewModel.loggedInUserViewModel?.userType == UserType.REMOTE) {
            val newDate = viewModel.loggedInUserViewModel?.lastSuccessfulSyncDate

            // TODO: Use relative formatting
            val formattedLastSuccessfulSync = newDate?.formattedDateTime ?: messages["overview_last_sync_never"]
            messages["overview_last_sync_subtitle"].format(formattedLastSuccessfulSync)
        } else {
            null
        }
    }

    override fun onUndock() {
        viewModel.loggedInUserViewModel?.webservices?.removeObserver(webservicesInitializedObserver)
        viewModel.loggedInUserViewModel?.itemViewModels?.removeObserver(itemViewModelsObserver)
        viewModel.loggedInUserViewModel?.loggedInStateStorage?.removeObserver(loggedInStateStorageObserver)

        updateToolbarJob?.cancel()

        super.onUndock()
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
                    toolbarSynchronizationIcon?.isDisable = isLoading
                }
            ) {
                viewModel.synchronizeData()
            }
        } else {
            Logger.debug("The synchronize data request is already running - skip call")
        }
    }
}

class ItemEntry(val itemViewModel: ItemViewModel) : ListItemIdentifiable {
    override val listItemId: String
        get() = itemViewModel.id
}

fun List<ItemEntry>.sorted(): List<ItemEntry> {
    return sortedBy { it.itemViewModel.title }
}
