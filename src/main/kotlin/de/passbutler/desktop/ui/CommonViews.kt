package de.passbutler.desktop.ui

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import tornadofx.FX
import tornadofx.addClass
import tornadofx.get
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.style
import tornadofx.vbox

fun Node.createEmptyScreen(title: String, description: String): Node {
    return vbox {
        alignment = Pos.CENTER
        paddingAll = marginM.value

        // Hidden by default to avoid initial flickering
        isVisible = false

        vectorDrawableIcon(Drawables.ICON_LIST)

        textLabelHeadlineOrder1(title) {
            paddingTop = marginM.value
            textAlignment = TextAlignment.CENTER
        }

        textLabelBodyOrder1(description) {
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
