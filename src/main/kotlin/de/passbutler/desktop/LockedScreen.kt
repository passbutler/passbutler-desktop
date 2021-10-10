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
import de.passbutler.desktop.ui.jfxButton
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.textLabelBodyOrder1
import de.passbutler.desktop.ui.textLabelHeadlineOrder1
import de.passbutler.desktop.ui.unmaskablePasswordField
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
import tornadofx.addStylesheet
import tornadofx.borderpane
import tornadofx.bottom
import tornadofx.center
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

    private val validationContext = ValidationContext()

    private val viewModel by injectWithPrivateScope<LockedScreenViewModel>()

    private val masterPassword = MutableBindable<String?>(null)

    init {
        with(root) {
            setupRootView()
        }
    }

    private fun Node.setupRootView() {
        stackpane {
            pane {
                addClass(Theme.backgroundAbstractStyle)
            }

            pane {
                addClass(Theme.backgroundOverlayStyle)
            }

            onLeftClick {
                requestFocus()
            }

            hbox(alignment = Pos.CENTER) {
                setupContent()
            }
        }
    }

    private fun Node.setupContent() {
        vbox(alignment = Pos.CENTER) {
            paddingAll = marginM.value

            setupCardViewContent()
        }
    }

    private fun Node.setupCardViewContent() {
        form {
            addClass(Theme.cardTranslucentStyle)

            alignment = Pos.CENTER
            prefWidth = 320.px.value

            imageview(Image("/drawables/logo_elevated.png", 120.px.value, 0.px.value, true, true)) {
                setupDebugPresetsButton()
            }

            textLabelHeadlineOrder1(messages["locked_screen_headline_normal"]) {
                paddingTop = marginM.value
                textAlignment = TextAlignment.CENTER
            }

            textLabelBodyOrder1(messages["locked_screen_description"]) {
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
            unmaskablePasswordField {
                bindInputOptional(this@LockedScreen, masterPassword)

                validationContext.validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["form_master_password_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Node.setupUnlockWithPasswordButton() {
        jfxButton(messages["locked_screen_button_password_text"]) {
            addClass(Theme.buttonPrimaryStyle)

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
