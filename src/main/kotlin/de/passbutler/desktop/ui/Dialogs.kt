package de.passbutler.desktop.ui

import tornadofx.CssRule
import tornadofx.FX
import tornadofx.action
import tornadofx.get

fun DialogPresenting.showConfirmDialog(
    title: String,
    message: String? = null,
    positiveActionTitle: String,
    positiveClickAction: () -> Unit,
    negativeClickAction: (() -> Unit)? = null
) {
    val confirmDialog = createDialog(Theme.alertDialogThemeDefault, title, message, positiveActionTitle, positiveClickAction, negativeClickAction)
    showDialog(confirmDialog)
}

fun DialogPresenting.showDangerousConfirmDialog(
    title: String,
    message: String? = null,
    positiveActionTitle: String,
    positiveClickAction: () -> Unit,
    negativeClickAction: (() -> Unit)? = null
) {
    val confirmDialog = createDialog(Theme.alertDialogThemeDangerous, title, message, positiveActionTitle, positiveClickAction, negativeClickAction)
    showDialog(confirmDialog)
}

private fun DialogPresenting.createDialog(
    alertDialogTheme: CssRule,
    title: String,
    message: String?,
    positiveActionTitle: String,
    positiveClickAction: () -> Unit,
    negativeClickAction: (() -> Unit)?
): AlertDialog {
    return AlertDialog(alertDialogTheme).apply {
        titleLabel.text = title

        if (message != null) {
            messageLabel.text = message
        } else {
            messageLabel.isVisible = false
            messageLabel.isManaged = false
        }

        positiveButton.text = positiveActionTitle
        positiveButton.action {
            positiveClickAction.invoke()
            dismissDialog()
        }

        negativeButton.text = FX.messages["general_cancel"]
        negativeButton.action {
            negativeClickAction?.invoke()
            dismissDialog()
        }
    }
}
