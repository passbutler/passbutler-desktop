package de.passbutler.desktop.ui

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import tornadofx.FX
import tornadofx.addClass
import tornadofx.addStylesheet
import tornadofx.borderpane
import tornadofx.bottom
import tornadofx.center
import tornadofx.get
import tornadofx.hbox
import tornadofx.imageview
import tornadofx.onLeftClick
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.pane
import tornadofx.px
import tornadofx.stackpane
import tornadofx.style
import tornadofx.top
import tornadofx.vbox

fun Node.createEmptyScreen(title: String, description: String): Node {
    return vbox {
        alignment = Pos.CENTER
        paddingAll = marginM.value

        // Hidden by default to avoid initial flickering
        isVisible = false

        vectorDrawableIcon(Drawables.ICON_LIST) {
            addClass(Theme.vectorDrawableIconAccent)
        }

        textLabelHeadlineOrder1(title) {
            paddingTop = marginM.value
            textAlignment = TextAlignment.CENTER
        }

        textLabelSubtitleOrder1(description) {
            paddingTop = marginS.value
            textAlignment = TextAlignment.CENTER
        }
    }
}

fun Node.createCancelButton(onAction: () -> Unit): Button {
    return jfxButton(FX.messages["general_cancel"]) {
        addClass(Theme.buttonSecondaryStyle)

        setOnAction {
            onAction()
        }
    }
}

fun Node.createInformationView(title: String, valueSetup: Label.() -> Unit): VBox {
    return vbox {
        textLabelBodyOrder1(title) {
            style {
                fontWeight = FontWeight.BOLD
            }
        }

        textLabelBodyOrder2 {
            paddingTop = marginXS.value

            valueSetup.invoke(this)
        }
    }
}

fun Node.createTransparentSectionedLayout(
    topSetup: BorderPane.() -> Unit = {},
    centerSetup: BorderPane.() -> Unit = {},
    bottomSetup: BorderPane.() -> Unit = {}
): StackPane {
    return stackpane {
        addStylesheet(DarkTheme::class)

        pane {
            addClass(Theme.backgroundAbstractStyle)
        }

        pane {
            addClass(Theme.backgroundOverlayStyle)
        }

        onLeftClick {
            requestFocus()
        }

        hbox(alignment = Pos.TOP_CENTER) {
            borderpane {
                prefWidth = 450.px.value

                top(topSetup)
                center(centerSetup)
                bottom(bottomSetup)
            }
        }
    }
}

fun Node.createHeaderView(op: HBox.() -> Unit = {}): HBox {
    return hbox {
        spacing = marginS.value
        paddingTop = marginM.value

        imageview(Image("/drawables/logo_outlined.png", 48.px.value, 0.px.value, true, true))

        vbox {
            textLabelHeadlineOrder1(FX.messages["general_app_name"])
            textLabelSubtitleOrder1(FX.messages["general_app_description"])
        }

        op.invoke(this)
    }
}
