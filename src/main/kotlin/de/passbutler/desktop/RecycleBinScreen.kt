package de.passbutler.desktop

import de.passbutler.common.ItemViewModel
import de.passbutler.common.base.BindableObserver
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.Drawables
import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.addLifecycleObserver
import de.passbutler.desktop.ui.bottomDropShadow
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.marginXS
import de.passbutler.desktop.ui.smallSVGIcon
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelHeadline1
import javafx.collections.FXCollections.observableArrayList
import javafx.collections.transformation.FilteredList
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.text.TextAlignment
import org.tinylog.kotlin.Logger
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
import tornadofx.listview
import tornadofx.paddingAll
import tornadofx.paddingLeft
import tornadofx.paddingTop
import tornadofx.pane
import tornadofx.select
import tornadofx.selectedItem
import tornadofx.stackpane
import tornadofx.textfield
import tornadofx.top
import tornadofx.vbox
import java.util.*

class RecycleBinScreen : NavigationMenuScreen(messages["recycle_bin_title"], navigationMenuItems = createDefaultNavigationMenu()), RequestSending {

    private val viewModel by injectWithPrivateScope<RecycleBinViewModel>()

    private var listScreenLayout: ListView<ItemEntry>? = null
    private var emptyScreenLayout: Node? = null

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
        emptyScreenLayout?.isVisible = showEmptyScreen
    }

    init {
        setupRootView()
    }

    override fun Node.setupMainContent() {
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
        vbox {
            addClass(Theme.toolbarStyle)
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

            shortcut("Ctrl+F") {
                requestFocus()
            }
        }
    }

    private fun Node.createListScreenLayout(): ListView<ItemEntry> {
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
        return hbox {
            alignment = Pos.CENTER_LEFT
            padding = insets(marginM.value, marginXS.value)

            smallSVGIcon(Drawables.ICON_FAVORITE.svgPath)

            vbox {
                paddingLeft = marginM.value

                textLabelHeadline1(listCell.itemProperty().select { it.titleProperty })
                textLabelBody1(listCell.itemProperty().select { it.subtitleProperty }) {
                    paddingTop = marginS.value
                }
            }

            contextmenu {
                item(messages["recycle_bin_item_context_menu_restore"]).action {
                    restoreSelectedItem()
                }
            }
        }
    }

    private fun restoreSelectedItem() {
        listScreenLayout?.selectedItem?.itemViewModel?.let { selectedItemViewModel ->
            val itemEditingViewModel = selectedItemViewModel.createEditingViewModel()

            launchRequestSending(
                handleSuccess = {
                    showInformation(messages["recycle_bin_restore_successful_message"])
                },
                handleFailure = { showError(messages["recycle_bin_restore_failed_general_title"]) }
            ) {
                itemEditingViewModel.restore()
            }
        }
    }

    private fun Node.createEmptyScreenLayout(): Node {
        return vbox {
            alignment = Pos.CENTER
            paddingAll = marginM.value
            spacing = marginS.value

            smallSVGIcon(Drawables.ICON_LIST.svgPath)

            textLabelHeadline1(messages["recycle_bin_empty_screen_title"]) {
                textAlignment = TextAlignment.CENTER
            }

            textLabelBody1(messages["recycle_bin_empty_screen_description"]) {
                textAlignment = TextAlignment.CENTER
            }
        }
    }

    override fun onDock() {
        super.onDock()

        viewModel.loggedInUserViewModel?.itemViewModels?.addLifecycleObserver(this, true, itemViewModelsObserver)
    }
}
