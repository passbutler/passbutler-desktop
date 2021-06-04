package de.passbutler.desktop.ui

import de.passbutler.common.crypto.PasswordGenerator
import de.passbutler.common.crypto.PasswordGenerator.CharacterType.Digits
import de.passbutler.common.crypto.PasswordGenerator.CharacterType.Lowercase
import de.passbutler.common.crypto.PasswordGenerator.CharacterType.Symbols
import de.passbutler.common.crypto.PasswordGenerator.CharacterType.Uppercase
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.tinylog.kotlin.Logger
import tornadofx.action
import tornadofx.addClass
import tornadofx.get
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.paddingTop
import tornadofx.region
import tornadofx.vbox
import kotlin.math.round

class PasswordGeneratorDialog(
    private val presentingFragment: BaseFragment,
    private val positiveClickAction: (newPassword: String) -> Unit,
    private val negativeClickAction: (() -> Unit)? = null
) : StackPane(),
    CoroutineScope by presentingFragment,
    CharacterTypeExtensions {

    private lateinit var generatedPasswordTextLabel: Label
    private lateinit var lengthSlider: Slider
    private lateinit var lowercaseCheckBox: CheckBox
    private lateinit var uppercaseCheckBox: CheckBox
    private lateinit var digitsCheckBox: CheckBox
    private lateinit var symbolsCheckBox: CheckBox
    private lateinit var positiveButton: Button
    private lateinit var negativeButton: Button

    private var generatePasswordJob: Job? = null
    private var generatePassword: String? = null

    init {
        hbox(alignment = Pos.CENTER) {
            vbox(alignment = Pos.CENTER) {
                vbox(alignment = Pos.CENTER_LEFT) {
                    addClass(Theme.alertDialogPasswordGeneratorTheme)

                    setupHeader()
                    setupLengthSlider()
                    setupCharacterTypesSelection()
                    setupButtonSection()

                    // Initially generate password
                    regeneratePassword()
                }
            }
        }
    }

    private fun Node.setupHeader() {
        textLabelBodyOrder1(presentingFragment.messages["passwordgenerator_dialog_title"])

        hbox {
            alignment = Pos.CENTER_LEFT
            spacing = marginM.value

            generatedPasswordTextLabel = textLabelHeadlineOrder2 {
                paddingTop = marginXS.value
            }

            region {
                hgrow = Priority.ALWAYS
            }

            vectorDrawableIcon(Drawables.ICON_REFRESH) {
                addClass(Theme.backgroundPressableStyle)

                onLeftClickIgnoringCount {
                    regeneratePassword()
                }
            }
        }
    }

    private fun Node.setupLengthSlider() {
        textLabelBodyOrder2(presentingFragment.messages["passwordgenerator_dialog_length_title"]) {
            paddingTop = marginM.value
        }

        lengthSlider = jfxSlider(PASSWORD_LENGTH_RANGE, PASSWORD_LENGTH_DEFAULT) {
            paddingTop = marginS.value

            valueProperty().addListener { _, oldValue, newValue ->
                val oldValueRounded = round(oldValue.toDouble())
                val newValueRounded = round(newValue.toDouble())

                if (oldValueRounded != newValueRounded) {
                    regeneratePassword()
                }
            }
        }
    }

    private fun Node.setupCharacterTypesSelection() {
        textLabelBodyOrder2(presentingFragment.messages["passwordgenerator_dialog_character_types_title"]) {
            paddingTop = marginS.value
        }

        vbox {
            paddingTop = marginS.value
            spacing = marginS.value

            lowercaseCheckBox = characterTypeCheckBox(Lowercase)
            uppercaseCheckBox = characterTypeCheckBox(Uppercase)
            digitsCheckBox = characterTypeCheckBox(Digits)
            symbolsCheckBox = characterTypeCheckBox(Symbols)
        }
    }

    private fun Node.characterTypeCheckBox(characterType: PasswordGenerator.CharacterType): CheckBox {
        return jfxCheckBox(characterType.userfacingText(presentingFragment)) {
            isSelected = characterType.isDefaultSelected

            action {
                regeneratePassword()
            }
        }
    }

    private fun Node.setupButtonSection() {
        hbox {
            alignment = Pos.CENTER_RIGHT
            paddingTop = marginM.value
            spacing = marginM.value

            negativeButton = jfxButton(presentingFragment.messages["general_cancel"]) {
                addClass(Theme.buttonTextStyle)
                addClass(Theme.alertDialogViewButtonNegativeStyle)
                isCancelButton = true

                action {
                    negativeClickAction?.invoke()
                }
            }

            positiveButton = jfxButton(presentingFragment.messages["general_accept"]) {
                addClass(Theme.buttonTextStyle)
                addClass(Theme.alertDialogViewButtonPositiveStyle)
                isDefaultButton = true

                action {
                    val generatePassword = generatePassword

                    if (generatePassword != null) {
                        positiveClickAction.invoke(generatePassword)
                    } else {
                        Logger.warn("The generated password is null!")
                    }
                }
            }
        }
    }

    private fun regeneratePassword() {
        generatePasswordJob?.cancel()
        generatePasswordJob = launch {
            val passwordLength = lengthSlider.value.toInt().takeIf { it > 0 }
            val characterTypes = setOfNotNull(
                Lowercase.takeIf { lowercaseCheckBox.isSelected },
                Uppercase.takeIf { uppercaseCheckBox.isSelected },
                Digits.takeIf { digitsCheckBox.isSelected },
                Symbols.takeIf { symbolsCheckBox.isSelected }
            ).takeIf { it.isNotEmpty() }

            if (passwordLength != null && characterTypes != null) {
                val newGeneratePassword = PasswordGenerator.generatePassword(
                    length = passwordLength,
                    characterTypes = characterTypes
                )

                generatePassword = newGeneratePassword

                generatedPasswordTextLabel.text = newGeneratePassword
                generatedPasswordTextLabel.textFill = ThemeManager.themeColors.textColorPrimary

                positiveButton.isEnabled = true

            } else {
                generatePassword = null

                generatedPasswordTextLabel.text = presentingFragment.messages["passwordgenerator_dialog_missing_character_types_error"]
                generatedPasswordTextLabel.textFill = ThemeManager.themeColors.colorError

                positiveButton.isEnabled = false
            }
        }
    }

    companion object {
        private val PASSWORD_LENGTH_RANGE = 4..64
        private const val PASSWORD_LENGTH_DEFAULT = 18
    }
}

interface CharacterTypeExtensions {
    fun PasswordGenerator.CharacterType.userfacingText(presentingFragment: BaseFragment): String {
        val stringKey = when (this) {
            Lowercase -> "passwordgenerator_dialog_character_type_lowercase"
            Uppercase -> "passwordgenerator_dialog_character_type_uppercase"
            Digits -> "passwordgenerator_dialog_character_type_digits"
            Symbols -> "passwordgenerator_dialog_character_type_symbols"
        }

        return presentingFragment.messages[stringKey]
    }

    val PasswordGenerator.CharacterType.isDefaultSelected: Boolean
        get() = when (this) {
            Lowercase -> true
            Uppercase -> true
            Digits -> true
            Symbols -> false
        }
}
