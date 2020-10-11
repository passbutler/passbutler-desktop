package de.passbutler.desktop.ui

import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.pt

class Styles : Stylesheet() {
    companion object {
        val STYLE_LOGIN_SCREEN by cssclass()
    }

    init {
        STYLE_LOGIN_SCREEN {
            padding = box(15.pt)
            vgap = 7.pt
            hgap = 10.pt
        }
    }
}