package de.passbutler.desktop

import de.passbutler.common.ItemViewModel
import de.passbutler.common.Webservices
import de.passbutler.common.base.BindableObserver
import de.passbutler.common.ui.ListItemIdentifiable
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.DarkTheme
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
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ListView
import kotlinx.coroutines.Job
import org.tinylog.kotlin.Logger
import tornadofx.FX.Companion.messages
import tornadofx.addClass
import tornadofx.addStylesheet
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
import tornadofx.right
import tornadofx.stackpane
import tornadofx.textfield
import tornadofx.top
import tornadofx.vbox

class OverviewScreen : NavigationMenuScreen(messages["overview_title"]), RequestSending {

    private val viewModel by injectWithPrivateScope<OverviewViewModel>()

    private var listScreenLayout: ListView<ItemEntry>? = null
    private var emptyScreenLayout: Node? = null
    private var syncIcon: Node? = null

    private val itemEntries = observableArrayList<ItemEntry>()

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

        itemEntries.setAll(newItemEntries)

        val showEmptyScreen = newItemEntries.isEmpty()
        emptyScreenLayout?.isVisible = showEmptyScreen
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
                borderpane {
                    // Enforce dark theme to toolbar view because it should look always dark
                    addStylesheet(DarkTheme::class)

                    addClass(Theme.backgroundStyle)

                    // TODO: Apply drop shadow only to borderpane
                    effect = bottomDropShadow()

                    padding = insets(marginM.value, marginS.value)

                    // TODO: Does not apply dark theme
                    left {
                        textfield {
                            promptText = messages["overview_search_hint"]
                        }
                    }

                    right {
                        vbox {
                            alignment = Pos.CENTER_RIGHT

                            syncIcon = smallSVGIcon(Drawables.ICON_REFRESH.svgPath) {
                                onLeftClick {
                                    if (viewModel.loggedInUserViewModel?.webservices?.value != null) {
                                        synchronizeData(userTriggered = true)
                                    }
                                }
                            }

                            textLabelBody1(messages["overview_last_sync_subtitle"].format(messages["overview_last_sync_never"])) {
                                paddingTop = marginXS.value
                            }
                        }
                    }
                }
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
        }
    }

    override fun onDock() {
        super.onDock()

        listScreenLayout?.items = itemEntries

        viewModel.loggedInUserViewModel?.webservices?.addObserver(this, true, webservicesInitializedObserver)
        viewModel.loggedInUserViewModel?.itemViewModels?.addObserver(this, true, itemViewModelsObserver)
    }

    override fun onUndock() {
        viewModel.loggedInUserViewModel?.webservices?.removeObserver(webservicesInitializedObserver)
        viewModel.loggedInUserViewModel?.itemViewModels?.removeObserver(itemViewModelsObserver)

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
                    syncIcon?.isDisable = isLoading
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