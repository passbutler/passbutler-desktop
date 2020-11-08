package de.passbutler.desktop

import de.passbutler.desktop.base.BuildConfig
import de.passbutler.desktop.base.formattedDateTime
import de.passbutler.desktop.ui.BaseTheme
import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.textLabelWrapped
import javafx.scene.layout.Pane
import tornadofx.FX.Companion.messages
import tornadofx.addClass
import tornadofx.get
import tornadofx.paddingTop
import java.time.Instant

class AboutScreen : NavigationMenuScreen(messages["about_title"]) {

    override fun Pane.createMainContent() {
        textLabelWrapped(messages["about_header"]) {
            addClass(BaseTheme.textHeadline1Style)
        }

        textLabelWrapped {
            addClass(BaseTheme.textBody1Style)
            paddingTop = marginS.value

            val versionName = BuildConfig.VERSION_NAME
            val formattedBuildTime = Instant.ofEpochMilli(BuildConfig.BUILD_TIMESTAMP).formattedDateTime
            val gitShortHash = BuildConfig.BUILD_REVISION_HASH
            text = messages["about_subheader"].format(versionName, formattedBuildTime, gitShortHash)
        }

        textLabelWrapped(messages["about_passage_1"]) {
            addClass(BaseTheme.textBody1Style)
            paddingTop = marginM.value
        }
    }
}
