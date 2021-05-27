package de.passbutler.desktop.ui

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import tornadofx.CssRule
import tornadofx.addClass
import tornadofx.hbox
import tornadofx.insets
import tornadofx.paddingTop
import tornadofx.vbox

class AlertDialog(
    alertDialogTheme: CssRule = Theme.alertDialogThemeDefault
) : StackPane() {

    lateinit var titleLabel: Label
        private set

    lateinit var messageLabel: Label
        private set

    lateinit var positiveButton: Button
        private set

    lateinit var negativeButton: Button
        private set

    init {
        hbox(alignment = Pos.CENTER) {
            vbox(alignment = Pos.CENTER) {
                vbox(alignment = Pos.CENTER_LEFT) {
                    addClass(alertDialogTheme)

                    padding = insets(marginM.value, marginM.value, marginS.value, marginM.value)

                    titleLabel = textLabelSubtitleOrder1 {
                        addClass(Theme.alertDialogViewTextTitleStyle)
                    }

                    messageLabel = textLabelBodyOrder2 {
                        addClass(Theme.alertDialogViewTextMessageStyle)

                        paddingTop = marginS.value
                    }

                    hbox {
                        alignment = Pos.CENTER_RIGHT
                        paddingTop = marginM.value
                        spacing = marginM.value

                        negativeButton = jfxButton {
                            addClass(Theme.buttonTextStyle)
                            addClass(Theme.alertDialogViewButtonNegativeStyle)
                            isCancelButton = true
                        }

                        positiveButton = jfxButton {
                            addClass(Theme.buttonTextStyle)
                            addClass(Theme.alertDialogViewButtonPositiveStyle)
                            isDefaultButton = true
                        }
                    }
                }
            }
        }
    }
}
