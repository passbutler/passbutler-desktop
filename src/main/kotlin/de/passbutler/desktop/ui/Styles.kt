package de.passbutler.desktop.ui

import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import javafx.scene.layout.BackgroundSize.AUTO
import javafx.scene.paint.Color
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.pt
import java.net.URI

class Styles : Stylesheet() {
    companion object {
        val STYLE_LOGIN_SCREEN by cssclass()
    }

    init {
        STYLE_LOGIN_SCREEN {
            padding = box(marginM)
//            vgap = 7.pt
//            hgap = 10.pt
            backgroundImage += URI("/drawables/background.jpg")
            backgroundSize += BackgroundSize(AUTO, AUTO, true, true, false, true)
            backgroundRepeat += BackgroundRepeat.NO_REPEAT to BackgroundRepeat.NO_REPEAT
        }
    }
}