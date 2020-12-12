package de.passbutler.desktop

import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.createDefaultNavigationMenu
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.textLabelHeadline
import javafx.scene.Node
import tornadofx.get
import tornadofx.paddingAll
import tornadofx.vbox

class ItemDetailScreen : NavigationMenuScreen(navigationMenuItems = createDefaultNavigationMenu()) {

    private val id by param<String?>(null)

    init {
        title = messages["itemdetail_title_new"]
    }

    override fun Node.createMainContent() {
        vbox {
            paddingAll = marginM.value

            textLabelHeadline(messages["itemdetail_title_new"])
        }
    }
}
