package de.passbutler.desktop.ui

import javafx.scene.control.TextInputControl
import tornadofx.validator

data class FormFieldValidatorRule(val isInvalidValidator: (formFieldText: String?) -> Boolean, val errorString: String)

fun TextInputControl.validatorWithRules(rulesCreator: () -> List<FormFieldValidatorRule>?) {
    validator { formFieldText ->
        var formFieldError: String? = null
        val rules = rulesCreator()

        if (rules != null) {
            for (validationRule in rules) {
                val validationFailed = validationRule.isInvalidValidator.invoke(formFieldText)

                if (validationFailed) {
                    formFieldError = validationRule.errorString
                    break
                }
            }
        }

        formFieldError?.let {
            error(it)
        }
    }
}
