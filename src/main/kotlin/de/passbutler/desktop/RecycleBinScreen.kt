package de.passbutler.desktop

import de.passbutler.common.ItemViewModel
import de.passbutler.common.base.BindableObserver
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ItemEditingViewModelWrapper.Companion.PARAMETER_ITEM_ID
import de.passbutler.desktop.ItemListViewSetupping.ListConfiguration
import de.passbutler.desktop.ui.NavigationMenuFragment
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.addLifecycleObserver
import de.passbutler.desktop.ui.bottomDropShadow
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.createEmptyScreen
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.paneWithDropShadow
import de.passbutler.desktop.ui.showScreenUnanimated
import javafx.collections.FXCollections.observableArrayList
import javafx.collections.ListChangeListener
import javafx.collections.transformation.FilteredList
import javafx.scene.Node
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import tornadofx.FX.Companion.messages
import tornadofx.action
import tornadofx.addClass
import tornadofx.borderpane
import tornadofx.cache
import tornadofx.center
import tornadofx.contextmenu
import tornadofx.get
import tornadofx.item
import tornadofx.listview
import tornadofx.onDoubleClick
import tornadofx.selectedItem
import tornadofx.stackpane
import tornadofx.textfield
import tornadofx.top
import tornadofx.vbox

class RecycleBinScreen : NavigationMenuFragment(messages["recycle_bin_title"], navigationMenuItems = createDefaultNavigationMenu()), ItemListViewSetupping, RequestSending {

    private val viewModel by injectWithPrivateScope<RecycleBinViewModel>()

    override var listView: ListView<ItemEntry>? = null
    override var emptyScreenView: Node? = null

    override val unfilteredItemEntries = observableArrayList<ItemEntry>()
    override val filteredItemEntries = FilteredList(unfilteredItemEntries)

    override val itemViewModelsObserver: BindableObserver<List<ItemViewModel>> = { newUnfilteredItemViewModels ->
        updateItemViewModels(newUnfilteredItemViewModels, ListConfiguration.ShowOnlyDeletedItems)
    }

    init {
        setupRootView()
    }

    override fun Node.setupMainContent() {
        borderpane {
            center {
                stackpane {
                    listView = createListView()
                    emptyScreenView = createEmptyScreen(messages["recycle_bin_empty_screen_headline"], messages["recycle_bin_empty_screen_description"])
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
        vbox {
            addClass(Theme.toolbarStyle)

            // Only take as much space as needed (not match parent)
            isFillWidth = false

            setupFilterTextfield()
        }
    }

    private fun Node.setupFilterTextfield() {
        textfield {
            promptText = messages["general_search"]

            textProperty().addListener { _, _, newValue ->
                updateFilterPredicate(newValue)
            }

            shortcut("Shortcut+F") {
                requestFocus()
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
        }
    }

    private fun Node.createItemEntryView(listCell: ListCell<ItemEntry>): Node {
        return createGenericItemEntryView(listCell) {
            onDoubleClick {
                showSelectedItem()
            }

            contextmenu {
                item(messages["recycle_bin_item_context_menu_restore"]).action {
                    restoreSelectedItem()
                }
            }
        }
    }

    override fun showSelectedItem() {
        listView?.selectedItem?.let { selectedItem ->
            showScreenUnanimated(ItemDetailScreen::class, parameters = mapOf(PARAMETER_ITEM_ID to selectedItem.itemViewModel.id))
        }
    }

    private fun restoreSelectedItem() {
        listView?.selectedItem?.itemViewModel?.let { selectedItemViewModel ->
            val itemEditingViewModel = selectedItemViewModel.createEditingViewModel()

            launchRequestSending(
                handleSuccess = { showInformation(messages["itemdetail_restore_successful_message"]) },
                handleFailure = { showError(messages["itemdetail_restore_failed_general_title"]) }
            ) {
                itemEditingViewModel.restore()
            }
        }
    }

    override fun onDock() {
        super.onDock()

        viewModel.loggedInUserViewModel?.itemViewModels?.addLifecycleObserver(this, true, itemViewModelsObserver)
    }
}
