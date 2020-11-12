package de.passbutler.desktop

import de.passbutler.desktop.base.formattedDateTime
import de.passbutler.desktop.ui.NavigationMenuScreen
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelBody2
import de.passbutler.desktop.ui.textLabelHeadline
import javafx.scene.Node
import tornadofx.FX.Companion.messages
import tornadofx.get
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.vbox
import java.time.Instant

class AboutScreen : NavigationMenuScreen(messages["about_title"]) {

    override fun Node.createMainContent() {
        vbox {
            paddingAll = marginM.value

            textLabelHeadline(messages["about_header"])

            textLabelBody1 {
                paddingTop = marginS.value

                val versionName = BuildConfig.VERSION_NAME
                val formattedBuildTime = Instant.ofEpochMilli(BuildConfig.BUILD_TIMESTAMP).formattedDateTime
                val gitShortHash = BuildConfig.BUILD_REVISION_HASH
                text = messages["about_subheader"].format(versionName, formattedBuildTime, gitShortHash)
            }

            textLabelBody2(messages["about_passage_1"]) {
                paddingTop = marginM.value
            }
        }
    }
}
