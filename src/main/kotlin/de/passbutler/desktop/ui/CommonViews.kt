package de.passbutler.desktop.ui

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.text.TextAlignment
import tornadofx.paddingAll
import tornadofx.vbox

fun Node.createEmptyScreenLayout(title: String, description: String): Node {
    return vbox {
        alignment = Pos.CENTER
        paddingAll = marginM.value
        spacing = marginS.value

        smallSVGIcon(Drawables.ICON_LIST.svgPath)

        textLabelHeadline1(title) {
            textAlignment = TextAlignment.CENTER
        }

        textLabelBody1(description) {
            textAlignment = TextAlignment.CENTER
        }
    }
}
