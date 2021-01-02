package de.passbutler.desktop.ui

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.text.TextAlignment
import tornadofx.FX
import tornadofx.action
import tornadofx.addClass
import tornadofx.get
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.vbox

fun Node.createEmptyScreen(title: String, description: String): Node {
    return vbox {
        alignment = Pos.CENTER
        paddingAll = marginM.value

        // Hidden by default to avoid initial flickering
        isVisible = false

        smallSVGIcon(Drawables.ICON_LIST.svgPath)

        textLabelHeadline1(title) {
            paddingTop = marginM.value
            textAlignment = TextAlignment.CENTER
        }

        textLabelBody1(description) {
            paddingTop = marginS.value
            textAlignment = TextAlignment.CENTER
        }
    }
}

fun Node.createCancelButton(onAction: () -> Unit) {
    jfxButtonRaised(FX.messages["general_cancel"]) {
        addClass(Theme.secondaryButtonStyle)

        setOnAction {
            onAction()
        }
    }
}
