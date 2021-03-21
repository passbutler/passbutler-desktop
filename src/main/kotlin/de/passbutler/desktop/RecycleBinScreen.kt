package de.passbutler.desktop

import de.passbutler.common.ItemViewModel
import de.passbutler.common.base.BindableObserver
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.NavigationMenuFragment
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.addLifecycleObserver
import de.passbutler.desktop.ui.bottomDropShadow
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.createEmptyScreen
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.paneWithDropShadow
import javafx.collections.FXCollections.observableArrayList
import javafx.collections.transformation.FilteredList
import javafx.scene.Node
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import org.tinylog.kotlin.Logger
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
import tornadofx.selectedItem
import tornadofx.stackpane
import tornadofx.textfield
import tornadofx.top
import tornadofx.vbox
import java.util.*

class RecycleBinScreen : NavigationMenuFragment(messages["recycle_bin_title"], navigationMenuItems = createDefaultNavigationMenu()), RequestSending {

    private val viewModel by injectWithPrivateScope<RecycleBinViewModel>()

    private var listView: ListView<ItemEntry>? = null
    private var emptyScreenView: Node? = null

    private val unfilteredItemEntries = observableArrayList<ItemEntry>()
    private val itemEntries = FilteredList(unfilteredItemEntries)

    private val itemViewModelsObserver: BindableObserver<List<ItemViewModel>> = { newUnfilteredItemViewModels ->
        // Only show deleted items
        val newItemViewModels = newUnfilteredItemViewModels.filter { it.deleted }
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
    }

    override fun Node.setupMainContent() {
        borderpane {
            center {
                stackpane {
                    listView = createListView()
                    emptyScreenView = createEmptyScreen(messages["recycle_bin_empty_screen_title"], messages["recycle_bin_empty_screen_description"])
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
                val newPredicate: ((ItemEntry) -> Boolean)? = if (newValue.isNullOrEmpty()) {
                    null
                } else {
                    { it.itemViewModel.title?.contains(newValue, ignoreCase = true) ?: false }
                }

                itemEntries.setPredicate(newPredicate)
            }

            shortcut("Shortcut+F") {
                requestFocus()
            }
        }
    }

    private fun Node.createListView(): ListView<ItemEntry> {
        return listview(itemEntries) {
            addClass(Theme.listViewPressableCellStyle)

            cellFormat {
                graphic = cache {
                    createItemEntryView(this@cellFormat)
                }
            }
        }
    }

    private fun Node.createItemEntryView(listCell: ListCell<ItemEntry>): Node {
        return createGenericItemEntryView(listCell) {
            contextmenu {
                item(messages["recycle_bin_item_context_menu_restore"]).action {
                    restoreSelectedItem()
                }
            }
        }
    }

    private fun restoreSelectedItem() {
        listView?.selectedItem?.itemViewModel?.let { selectedItemViewModel ->
            val itemEditingViewModel = selectedItemViewModel.createEditingViewModel()

            launchRequestSending(
                handleSuccess = { showInformation(messages["recycle_bin_restore_successful_message"]) },
                handleFailure = { showError(messages["recycle_bin_restore_failed_general_title"]) }
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
