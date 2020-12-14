package de.passbutler.desktop.ui

import com.jfoenix.controls.JFXSpinner
import javafx.event.EventTarget
import tornadofx.attachTo

fun EventTarget.jfxSpinner(op: JFXSpinner.() -> Unit = {}) = JFXSpinner().attachTo(this, op)
