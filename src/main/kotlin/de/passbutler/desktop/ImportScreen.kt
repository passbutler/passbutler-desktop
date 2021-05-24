package de.passbutler.desktop

import de.passbutler.common.base.Result
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.base.PathProvider
import de.passbutler.desktop.ui.NavigationMenuFragment
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.marginL
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.textLabelBodyOrder1
import de.passbutler.desktop.ui.textLabelBodyOrder2
import de.passbutler.desktop.ui.textLabelHeadlineOrder1
import de.passbutler.desktop.ui.textLabelHeadlineOrder2
import javafx.scene.Node
import javafx.stage.FileChooser
import tornadofx.FX.Companion.messages
import tornadofx.FileChooserMode
import tornadofx.action
import tornadofx.addClass
import tornadofx.chooseFile
import tornadofx.get
import tornadofx.hbox
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.putString
import tornadofx.vbox
import java.io.File

class ImportScreen : NavigationMenuFragment(messages["import_title"], navigationMenuItems = createDefaultNavigationMenu()), RequestSending {

    private val viewModel by injectWithPrivateScope<ImportViewModel>()

    init {
        setupRootView()
    }

    override fun Node.setupMainContent() {
        vbox {
            paddingAll = marginM.value
            spacing = marginL.value

            setupHeaderSection()
            setupKeePassX2Section()
            setupKeePass2Section()
        }
    }

    private fun Node.setupHeaderSection() {
        vbox {
            spacing = marginS.value

            textLabelHeadlineOrder1(messages["import_header"])
            textLabelBodyOrder1(messages["import_description"])
        }
    }

    private fun Node.setupKeePassX2Section() {
        vbox {
            textLabelHeadlineOrder2(messages["import_keepassx2_header"])

            textLabelBodyOrder1 {
                paddingTop = marginS.value
                text = messages["import_keepassx2_description"]
            }

            val recycleBinName = KeePassX2ImportProvider.RECYCLE_BIN_NAME

            textLabelBodyOrder2 {
                paddingTop = marginS.value
                text = messages["import_keepassx2_language_hint"].format(recycleBinName)
            }

            hbox {
                paddingTop = marginM.value
                spacing = marginM.value

                setupImportButton(messages["import_keepassx2_button_text"], messages["import_keepassx2_file_extension_description"]) { chosenFile ->
                    viewModel.importKeePass2X(chosenFile)
                }

                jfxButtonRaised(messages["general_copy_button_title"].format(recycleBinName)) {
                    addClass(Theme.buttonSecondaryStyle)

                    action {
                        clipboard.putString(recycleBinName)
                        showInformation(messages["general_copy_to_clipboard_successful_message"])
                    }
                }
            }
        }
    }

    private fun Node.setupKeePass2Section() {
        vbox {
            textLabelHeadlineOrder2(messages["import_keepass2_header"])

            textLabelBodyOrder1 {
                paddingTop = marginS.value
                text = messages["import_keepass2_description"]
            }

            textLabelBodyOrder2 {
                paddingTop = marginS.value
                text = messages["import_keepass2_export_format_hint"]
            }

            vbox {
                paddingTop = marginM.value

                setupImportButton(messages["import_keepass2_button_text"], messages["import_keepass2_file_extension_description"]) { chosenFile ->
                    viewModel.importKeePass2(chosenFile)
                }
            }
        }
    }

    private fun Node.setupImportButton(buttonText: String, fileExtensionDescription: String, importBlock: suspend (File) -> Result<Int>) {
        jfxButtonRaised(buttonText) {
            action {
                val extensionFilter = createFileChooserExtensionFilter(fileExtensionDescription)

                showImportFileChooser(buttonText, extensionFilter) { chosenFile ->
                    importChosenFile(chosenFile, importBlock)
                }
            }
        }
    }

    private fun createFileChooserExtensionFilter(userFacingFileExtensionDescription: String): Array<FileChooser.ExtensionFilter> {
        val userFacingFileExtensionPattern = "*.csv"
        val extensionFilter = FileChooser.ExtensionFilter("$userFacingFileExtensionDescription ($userFacingFileExtensionPattern)", userFacingFileExtensionPattern)
        return arrayOf(extensionFilter)
    }

    private fun showImportFileChooser(title: String, extensionFilter: Array<FileChooser.ExtensionFilter>, chosenFileBlock: (File) -> Unit) {
        val homeDirectory = PathProvider.obtainDirectoryBlocking { homeDirectory }

        chooseFile(title, extensionFilter, initialDirectory = homeDirectory, mode = FileChooserMode.Single).firstOrNull()?.let {
            chosenFileBlock(it)
        }
    }

    private fun importChosenFile(chosenFile: File, importBlock: suspend (File) -> Result<Int>) {
        launchRequestSending(
            handleSuccess = { showInformation(messages["import_successful_message"].format(it)) },
            handleFailure = {
                val errorStringResourceId = when (it) {
                    is ImportFileEmptyException -> "import_failed_empty_import_file_title"
                    else -> "import_failed_general_title"
                }

                showError(messages[errorStringResourceId])
            }
        ) {
            importBlock(chosenFile)
        }
    }
}
