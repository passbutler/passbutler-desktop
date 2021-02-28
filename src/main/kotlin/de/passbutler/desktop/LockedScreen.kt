package de.passbutler.desktop

import de.passbutler.common.DecryptMasterEncryptionKeyFailedException
import de.passbutler.common.base.BuildType
import de.passbutler.common.base.MutableBindable
import de.passbutler.common.ui.RequestSending
import de.passbutler.common.ui.launchRequestSending
import de.passbutler.desktop.base.BuildInformationProvider
import de.passbutler.desktop.base.DebugConstants
import de.passbutler.desktop.ui.BaseFragment
import de.passbutler.desktop.ui.FormFieldValidatorRule
import de.passbutler.desktop.ui.FormValidating
import de.passbutler.desktop.ui.LONGPRESS_DURATION
import de.passbutler.desktop.ui.Theme
import de.passbutler.desktop.ui.bindInputOptional
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxButtonRaised
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.passwordFieldMaskable
import de.passbutler.desktop.ui.textLabelBody1
import de.passbutler.desktop.ui.textLabelHeadline1
import de.passbutler.desktop.ui.validateWithRules
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.text.TextAlignment
import tornadofx.FX.Companion.messages
import tornadofx.Fieldset
import tornadofx.ValidationContext
import tornadofx.action
import tornadofx.addClass
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.hbox
import tornadofx.imageview
import tornadofx.longpress
import tornadofx.onLeftClick
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.pane
import tornadofx.px
import tornadofx.stackpane
import tornadofx.useMaxWidth
import tornadofx.vbox

class LockedScreen : BaseFragment(messages["locked_screen_title"]), FormValidating, RequestSending {

    override val root = stackpane()

    override val validationContext = ValidationContext()

    private val viewModel by injectWithPrivateScope<LockedScreenViewModel>()

    private val masterPassword = MutableBindable<String?>(null)

    init {
        with(root) {
            setupContentView()
        }
    }

    private fun Node.setupContentView() {
        stackpane {
            pane {
                addClass(Theme.abstractBackgroundStyle)
            }

            pane {
                addClass(Theme.abstractBackgroundOverlayStyle)
            }

            onLeftClick {
                requestFocus()
            }

            hbox(alignment = Pos.CENTER) {
                vbox(alignment = Pos.CENTER) {
                    setupCardViewContent()
                }
            }
        }
    }

    private fun Node.setupCardViewContent() {
        form {
            addClass(Theme.cardViewBackgroundStyle)

            alignment = Pos.CENTER
            paddingAll = marginM.value
            prefWidth = 320.px.value

            imageview(Image("/drawables/logo_elevated.png", 120.px.value, 0.px.value, true, true)) {
                setupDebugPresetsButton()
            }

            textLabelHeadline1(messages["locked_screen_header_normal"]) {
                paddingTop = marginM.value
                textAlignment = TextAlignment.CENTER
            }

            textLabelBody1(messages["locked_screen_description"]) {
                paddingTop = marginS.value
                textAlignment = TextAlignment.CENTER
            }

            fieldset(labelPosition = Orientation.VERTICAL) {
                paddingTop = marginM.value
                setupPasswordUrlField()
            }

            vbox {
                paddingTop = marginS.value
                setupUnlockWithPasswordButton()
            }
        }
    }

    private fun ImageView.setupDebugPresetsButton() {
        if (BuildInformationProvider.buildType == BuildType.Debug) {
            longpress(LONGPRESS_DURATION) {
                masterPassword.value = DebugConstants.TEST_PASSWORD
            }
        }
    }

    private fun Fieldset.setupPasswordUrlField() {
        field(messages["locked_screen_master_password_hint"], orientation = Orientation.VERTICAL) {
            passwordFieldMaskable {
                bindInputOptional(this@LockedScreen, masterPassword)

                validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["form_master_password_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Node.setupUnlockWithPasswordButton() {
        jfxButtonRaised(messages["locked_screen_button_password_text"]) {
            useMaxWidth = true
            isDefaultButton = true

            action {
                unlockWithPasswordClicked()
            }
        }
    }

    private fun unlockWithPasswordClicked() {
        validationContext.validate()

        val masterPasswordValue = masterPassword.value

        if (validationContext.isValid && masterPasswordValue != null) {
            unlockWithPassword(masterPasswordValue)
        }
    }

    private fun unlockWithPassword(masterPassword: String) {
        launchRequestSending(
            handleFailure = {
                val errorStringResourceId = when (it) {
                    is DecryptMasterEncryptionKeyFailedException -> "locked_screen_unlock_failed_wrong_master_password_title"
                    else -> "locked_screen_unlock_failed_general_title"
                }

                showError(messages[errorStringResourceId])
            },
            isCancellable = false
        ) {
            viewModel.unlockScreenWithPassword(masterPassword)
        }
    }
}
