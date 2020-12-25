package de.passbutler.desktop

import de.passbutler.common.ItemAuthorizationEditingViewModel
import de.passbutler.common.base.BindableObserver
import de.passbutler.common.ui.ListItemIdentifiable
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.ui.Drawables
import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.addLifecycleObserver
import de.passbutler.desktop.ui.bindEnabled
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.jfxToggleButton
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.marginXS
import de.passbutler.desktop.ui.smallSVGIcon
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelHeadline1
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ListCell
import javafx.scene.text.TextAlignment
import kotlinx.coroutines.launch
import org.tinylog.kotlin.Logger
import tornadofx.FX
import tornadofx.action
import tornadofx.addClass
import tornadofx.cache
import tornadofx.fitToParentHeight
import tornadofx.get
import tornadofx.hbox
import tornadofx.insets
import tornadofx.listview
import tornadofx.paddingAll
import tornadofx.paddingLeft
import tornadofx.select
import tornadofx.vbox

class ItemAuthorizationsDetailScreen : NavigationMenuScreen(FX.messages["itemauthorizations_title"], navigationMenuItems = createDefaultNavigationMenu()), RequestSending {

    private val viewModel
        get() = viewModelWrapper.itemAuthorizationsDetailViewModel

    private val viewModelWrapper by injectWithPrivateScope<ItemAuthorizationsDetailViewModelWrapper>(params)

    private val itemAuthorizationEntries = FXCollections.observableArrayList<ItemAuthorizationEntry>()

    private val itemAuthorizationsObserver: BindableObserver<List<ItemAuthorizationEditingViewModel>> = { newItemAuthorizationEditingViewModels ->
        Logger.debug("newItemAuthorizationEditingViewModels.size = ${newItemAuthorizationEditingViewModels.size}")

        val newItemAuthorizationEntries = newItemAuthorizationEditingViewModels
            .map { ItemAuthorizationEntry(it) }
            .sorted()

        itemAuthorizationEntries.setAll(newItemAuthorizationEntries)
    }

    init {
        setupRootView()
    }

    override fun Node.setupMainContent() {
        createListScreenLayout()
    }

    private fun Node.createListScreenLayout(): Node {
        return vbox {
            // List header
            vbox {
                paddingAll = marginM.value
                spacing = marginS.value

                textLabelHeadline1(messages["itemauthorizations_header"])

                // TODO: wrapText does not work if list view is added
                textLabelBody1(messages["itemauthorizations_description"])
            }

            listview(itemAuthorizationEntries) {
                addClass(Theme.listViewStaticBackgroundStyle)

                placeholder = createEmptyScreenLayout()

                // Workaround for "hardcoded 400px height of ListView" issue
                fitToParentHeight()

                cellFormat {
                    graphic = cache {
                        createItemAuthorizationEntryView(this@cellFormat)
                    }
                }
            }

            // List footer
            vbox {
                paddingAll = marginM.value

                jfxButtonRaised(messages["itemauthorizations_save_button_title"]) {
                    isDefaultButton = true

                    bindEnabled(this@ItemAuthorizationsDetailScreen, viewModel.itemAuthorizationEditingViewModelsModified)

                    action {
                        saveClicked()
                    }
                }
            }
        }
    }

    private fun saveClicked() {
        launchRequestSending(
            handleFailure = { showError(messages["itemauthorizations_save_failed_general_title"]) }
        ) {
            viewModel.save()
        }
    }

    private fun Node.createItemAuthorizationEntryView(listCell: ListCell<ItemAuthorizationEntry>): Node {
        return vbox {
            alignment = Pos.CENTER_LEFT
            padding = insets(marginM.value, marginXS.value)

            hbox {
                smallSVGIcon(Drawables.ICON_ACCOUNT_CIRCLE.svgPath)

                vbox {
                    paddingLeft = marginM.value

                    textLabelHeadline1(listCell.itemProperty().select { it.titleProperty })

                    hbox {
                        spacing = marginS.value

                        jfxToggleButton(messages["itemauthorizations_read_switch_title"]) {
                            paddingAll = 0

                            selectedProperty().bindBidirectional(listCell.itemProperty().select { it.readSwitchProperty })
                        }

                        jfxToggleButton(messages["itemauthorizations_write_switch_title"]) {
                            paddingAll = 0

                            selectedProperty().bindBidirectional(listCell.itemProperty().select { it.writeSwitchProperty })
                        }
                    }
                }
            }
        }
    }

    private fun Node.createEmptyScreenLayout(): Node {
        return vbox {
            alignment = Pos.CENTER
            paddingAll = marginM.value
            spacing = marginS.value

            smallSVGIcon(Drawables.ICON_LIST.svgPath)

            textLabelHeadline1(messages["itemauthorizations_empty_screen_title"]) {
                textAlignment = TextAlignment.CENTER
            }

            textLabelBody1(messages["itemauthorizations_empty_screen_description"]) {
                textAlignment = TextAlignment.CENTER
            }
        }
    }

    override fun onDock() {
        super.onDock()

        viewModel.itemAuthorizationEditingViewModels.addLifecycleObserver(this, false, itemAuthorizationsObserver)

        launch {
            viewModel.initializeItemAuthorizationEditingViewModels()
        }
    }
}

class ItemAuthorizationEntry(val itemAuthorizationEditingViewModel: ItemAuthorizationEditingViewModel) : ListItemIdentifiable {
    override val listItemId: String
        get() = when (itemAuthorizationModel) {
            is ItemAuthorizationEditingViewModel.ItemAuthorizationModel.Provisional -> itemAuthorizationModel.itemAuthorizationId
            is ItemAuthorizationEditingViewModel.ItemAuthorizationModel.Existing -> itemAuthorizationModel.itemAuthorization.id
        }

    private val itemAuthorizationModel = itemAuthorizationEditingViewModel.itemAuthorizationModel

    val titleProperty = SimpleStringProperty(itemAuthorizationEditingViewModel.username)
    val readSwitchProperty = SimpleBooleanProperty(itemAuthorizationEditingViewModel.isReadAllowed.value)
    val writeSwitchProperty = SimpleBooleanProperty(itemAuthorizationEditingViewModel.isWriteAllowed.value)

    init {
        readSwitchProperty.addListener { _, _, isChecked ->
            itemAuthorizationEditingViewModel.isReadAllowed.value = isChecked

            // If no read access is given, write access is meaningless
            if (!isChecked) {
                writeSwitchProperty.value = false
            }
        }

        writeSwitchProperty.addListener { _, _, isChecked ->
            itemAuthorizationEditingViewModel.isWriteAllowed.value = isChecked

            // If write access is given, read access is implied
            if (isChecked) {
                readSwitchProperty.value = true
            }
        }
    }
}

fun List<ItemAuthorizationEntry>.sorted(): List<ItemAuthorizationEntry> {
    return sortedBy { it.itemAuthorizationEditingViewModel.username }
}