package de.passbutler.desktop.ui

import de.passbutler.common.ui.RequestSending
import de.passbutler.desktop.AboutScreen
import de.passbutler.desktop.OverviewScreen
import javafx.scene.Node
import javafx.scene.layout.Pane
import tornadofx.addClass
import tornadofx.borderpane
import tornadofx.center
import tornadofx.label
import tornadofx.left
import tornadofx.onLeftClick
import tornadofx.paddingAll
import tornadofx.px
import tornadofx.style
import tornadofx.useMaxWidth
import tornadofx.vbox

abstract class NavigationMenuScreen(title: String? = null, icon: Node? = null) : BaseFragment(title, icon), RequestSending {

    final override val root = borderpane {
        left {
            vbox {
                addClass(BaseTheme.abstractBackgroundStyle)

                style {
                    prefWidth = 200.px
                    paddingAll = marginM.value
                }


                label("Vault") {
                    onLeftClick {
                        if (!isScreenShown(OverviewScreen::class)) {
                            showScreenFaded(OverviewScreen::class)
                        }
                    }
                }

                label("About") {
                    onLeftClick {
                        if (!isScreenShown(AboutScreen::class)) {
                            showScreenFaded(AboutScreen::class)
                        }
                    }
                }
            }
        }

        center {
            vbox {
                style {
                    paddingAll = marginM.value
                }

                useMaxWidth = true

                createMainContent()
            }
        }
    }

    abstract fun Pane.createMainContent()
}

