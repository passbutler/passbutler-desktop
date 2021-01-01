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
import de.passbutler.desktop.ui.createEmptyScreen
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.jfxToggleButton
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.showScreenUnanimated
import de.passbutler.desktop.ui.smallSVGIcon
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelHeadline1
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections.observableArrayList
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import kotlinx.coroutines.launch
import org.tinylog.kotlin.Logger
import tornadofx.FX
import tornadofx.action
import tornadofx.addClass
import tornadofx.borderpane
import tornadofx.bottom
import tornadofx.cache
import tornadofx.center
import tornadofx.get
import tornadofx.hbox
import tornadofx.insets
import tornadofx.listview
import tornadofx.paddingAll
import tornadofx.paddingLeft
import tornadofx.paddingTop
import tornadofx.select
import tornadofx.top
import tornadofx.vbox

class ItemAuthorizationsDetailScreen : NavigationMenuScreen(FX.messages["itemauthorizations_title"], navigationMenuItems = createDefaultNavigationMenu()), RequestSending {

    private val viewModel
        get() = viewModelWrapper.itemAuthorizationsDetailViewModel

    private val viewModelWrapper by injectWithPrivateScope<ItemAuthorizationsDetailViewModelWrapper>(params)

    private val itemAuthorizationEntries = observableArrayList<ItemAuthorizationEntry>()

    private val itemAuthorizationsObserver: BindableObserver<List<ItemAuthorizationEditingViewModel>> = { newItemAuthorizationEditingViewModels ->
        Logger.debug("newItemAuthorizationEditingViewModels.size = ${newItemAuthorizationEditingViewModels.size}")

        val newItemAuthorizationEntries = newItemAuthorizationEditingViewModels
            .map { ItemAuthorizationEntry(it) }
            .sorted()

        itemAuthorizationEntries.setAll(newItemAuthorizationEntries)
    }

    init {
        setupRootView()

        shortcut("ESC") {
            showScreenUnanimated(ItemDetailScreen::class, parameters = params)
        }
    }

    override fun Node.setupMainContent() {
        borderpane {
            top {
                setupListViewHeader()
            }

            center {
                createListView()
            }

            bottom {
                setupListViewFooter()
            }
        }
    }

    private fun Node.setupListViewHeader() {
        vbox {
            paddingAll = marginM.value

            textLabelHeadline1(messages["itemauthorizations_header"])

            textLabelBody1(messages["itemauthorizations_description"]) {
                paddingTop = marginS.value
            }
        }
    }

    private fun Node.createListView(): ListView<ItemAuthorizationEntry> {
        return listview(itemAuthorizationEntries) {
            addClass(Theme.listViewVerticalDividerStyle)
            addClass(Theme.listViewPressableCellStyle)

            placeholder = createEmptyScreen(messages["itemauthorizations_empty_screen_title"], messages["itemauthorizations_empty_screen_description"])

            cellFormat {
                graphic = cache {
                    createItemAuthorizationEntryView(this@cellFormat)
                }
            }
        }
    }

    private fun Node.createItemAuthorizationEntryView(listCell: ListCell<ItemAuthorizationEntry>): Node {
        return vbox {
            alignment = Pos.CENTER_LEFT

            // No bottom padding because the toggle buttons itself have a lot of padding
            padding = insets(marginS.value, marginM.value, 0, marginM.value)

            val headlineView = textLabelHeadline1(listCell.itemProperty().select { it.titleProperty }) {
                graphic = smallSVGIcon(Drawables.ICON_ACCOUNT_CIRCLE.svgPath)
                graphicTextGap = marginM.value
            }

            hbox {
                spacing = marginM.value

                // Width of the graphic + spacing of the `headlineView`
                paddingLeft = 18.0 + headlineView.graphicTextGap

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

    private fun Node.setupListViewFooter() {
        hbox {
            paddingAll = marginM.value
            spacing = marginM.value

            setupSaveButton()
            setupCancelButton()
        }
    }

    private fun Node.setupSaveButton() {
        jfxButtonRaised(messages["itemauthorizations_save_button_title"]) {
            isDefaultButton = true

            bindEnabled(this@ItemAuthorizationsDetailScreen, viewModel.itemAuthorizationEditingViewModelsModified)

            action {
                saveClicked()
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

    private fun Node.setupCancelButton() {
        jfxButtonRaised(messages["general_cancel"]) {
            addClass(Theme.secondaryButtonStyle)

            action {
                showScreenUnanimated(ItemDetailScreen::class, parameters = params)
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

class ItemAuthorizationEntry(private val itemAuthorizationEditingViewModel: ItemAuthorizationEditingViewModel) : ListItemIdentifiable, Comparable<ItemAuthorizationEntry> {
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

    override fun compareTo(other: ItemAuthorizationEntry): Int {
        return compareValuesBy(this, other, { it.itemAuthorizationEditingViewModel.username })
    }
}
