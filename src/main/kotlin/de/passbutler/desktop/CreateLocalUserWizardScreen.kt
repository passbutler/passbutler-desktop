package de.passbutler.desktop

import de.passbutler.common.base.BuildType
import de.passbutler.common.base.Failure
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
import de.passbutler.desktop.ui.createHeaderView
import de.passbutler.desktop.ui.createTransparentSectionedLayout
import de.passbutler.desktop.ui.injectWithPrivateScope
import de.passbutler.desktop.ui.jfxButton
import de.passbutler.desktop.ui.marginM
import de.passbutler.desktop.ui.marginS
import de.passbutler.desktop.ui.showFadeInOutAnimation
import de.passbutler.desktop.ui.showSaveVaultFileChooser
import de.passbutler.desktop.ui.showScreenFaded
import de.passbutler.desktop.ui.textLabelBase
import de.passbutler.desktop.ui.textLabelCaption
import de.passbutler.desktop.ui.textLabelHeadlineOrder2
import de.passbutler.desktop.ui.unmaskablePasswordField
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.VBox
import tornadofx.FX.Companion.messages
import tornadofx.Fieldset
import tornadofx.ValidationContext
import tornadofx.action
import tornadofx.addClass
import tornadofx.box
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.longpress
import tornadofx.paddingAll
import tornadofx.paddingTop
import tornadofx.px
import tornadofx.stackpane
import tornadofx.style
import tornadofx.textfield
import tornadofx.useMaxWidth
import tornadofx.vbox

class CreateLocalUserWizardScreen : BaseFragment(messages["create_local_user_step_title"]), FormValidating, RequestSending {

    override val root = stackpane()

    private val viewModel by injectWithPrivateScope<CreateLocalUserWizardViewModel>()

    private val username = MutableBindable<String?>(null)
    private val masterPassword = MutableBindable<String?>(null)

    private val validationContextStep1 = ValidationContext()
    private val validationContextStep2 = ValidationContext()

    private var step1View: Node? = null
    private var step2View: Node? = null
    private var step3View: Node? = null

    private var currentStep: Step = Step.Username

    init {
        with(root) {
            setupRootView()
        }

        shortcut("ESC") {
            showPreviousScreen()
        }

        showStep(currentStep, false)
    }

    private fun showPreviousScreen() {
        when (currentStep) {
            Step.Username -> {
                showScreenFaded(IntroductionScreen::class)
            }
            Step.MasterPassword -> {
                showStep(Step.Username)
            }
            Step.VaultFileChoose -> {
                showStep(Step.MasterPassword)
            }
        }
    }

    private fun Node.setupRootView() {
        createTransparentSectionedLayout(
            topSetup = {
                setupHeader()
            },
            centerSetup = {
                setupContent()
            },
            bottomSetup = {
                setupFooter()
            }
        )
    }

    private fun Node.setupHeader() {
        createHeaderView {
            paddingAll = marginM.value

            setupDebugPresetsButton()
        }
    }

    private fun Node.setupDebugPresetsButton() {
        if (BuildInformationProvider.buildType == BuildType.Debug) {
            longpress(LONGPRESS_DURATION) {
                username.value = DebugConstants.TEST_USERNAME
                masterPassword.value = DebugConstants.TEST_PASSWORD
            }
        }
    }

    private fun Node.setupContent() {
        stackpane {
            paddingAll = marginM.value

            step1View = createStep1().apply {
                isVisible = false
            }

            step2View = createStep2().apply {
                isVisible = false
            }

            step3View = createStep3().apply {
                isVisible = false
            }
        }
    }

    private fun Node.createStep1(): VBox {
        return vbox(alignment = Pos.CENTER_LEFT) {
            textLabelBase(messages["create_local_user_step_headline"].format(1, 3)) {
                addClass(Theme.textHeadline4Style)
            }

            textLabelHeadlineOrder2(messages["create_local_user_step_username_headline"]) {
                paddingTop = marginS.value
            }

            form {
                // Reset the default padding from theme style
                style {
                    padding = box(0.px)
                }

                fieldset(labelPosition = Orientation.VERTICAL) {
                    paddingTop = marginM.value
                    spacing = marginS.value

                    setupUsernameField()
                }

                textLabelCaption(messages["create_local_user_step_username_hint_text"]) {
                    paddingTop = marginS.value
                }

                vbox {
                    paddingTop = marginM.value
                    setupStep1Button()
                }
            }
        }
    }

    private fun Fieldset.setupUsernameField() {
        field(messages["general_username_hint"], orientation = Orientation.VERTICAL) {
            textfield {
                bindInputOptional(this@CreateLocalUserWizardScreen, username)

                validationContextStep1.validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["form_username_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Node.setupStep1Button() {
        jfxButton(messages["general_next"]) {
            addClass(Theme.buttonPrimaryStyle)

            useMaxWidth = true

            action {
                confirmStep1ButtonClicked()
            }
        }
    }

    private fun confirmStep1ButtonClicked() {
        validationContextStep1.validate()

        if (validationContextStep1.isValid) {
            showStep(Step.MasterPassword)
        }
    }

    private fun Node.createStep2(): VBox {
        return vbox(alignment = Pos.CENTER_LEFT) {
            textLabelBase(messages["create_local_user_step_headline"].format(2, 3)) {
                addClass(Theme.textHeadline4Style)
            }

            textLabelHeadlineOrder2(messages["create_local_user_step_master_password_headline"]) {
                paddingTop = marginS.value
            }

            form {
                // Reset the default padding from theme style
                style {
                    padding = box(0.px)
                }

                fieldset(labelPosition = Orientation.VERTICAL) {
                    paddingTop = marginM.value
                    spacing = marginS.value

                    setupMasterPasswordField()
                    setupMasterPasswordConfirmField()
                }

                textLabelCaption(messages["create_local_user_step_master_password_hint_text"]) {
                    paddingTop = marginS.value
                }

                vbox {
                    paddingTop = marginM.value
                    setupStep2Button()
                }
            }
        }
    }

    private fun Fieldset.setupMasterPasswordField() {
        field(messages["general_master_password_hint"], orientation = Orientation.VERTICAL) {
            unmaskablePasswordField {
                bindInputOptional(this@CreateLocalUserWizardScreen, masterPassword)

                validationContextStep2.validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it.isNullOrEmpty() }, messages["form_master_password_validation_error_empty"])
                    )
                }
            }
        }
    }

    private fun Fieldset.setupMasterPasswordConfirmField() {
        field(messages["general_master_password_confirmation_hint"], orientation = Orientation.VERTICAL) {
            unmaskablePasswordField {
                validationContextStep2.validateWithRules(this) {
                    listOf(
                        FormFieldValidatorRule({ it != masterPassword.value }, messages["create_local_user_step_master_password_confirm_validation_error_different"])
                    )
                }
            }
        }
    }

    private fun Node.setupStep2Button() {
        jfxButton(messages["general_next"]) {
            addClass(Theme.buttonPrimaryStyle)

            useMaxWidth = true

            action {
                confirmStep2ButtonClicked()
            }
        }
    }

    private fun confirmStep2ButtonClicked() {
        validationContextStep2.validate()

        if (validationContextStep2.isValid) {
            showStep(Step.VaultFileChoose)
        }
    }

    private fun Node.createStep3(): VBox {
        return vbox(alignment = Pos.CENTER_LEFT) {
            textLabelBase(messages["create_local_user_step_headline"].format(3, 3)) {
                addClass(Theme.textHeadline4Style)
            }

            textLabelHeadlineOrder2(messages["create_local_user_step_vault_file_headline"]) {
                paddingTop = marginS.value
            }

            vbox {
                paddingTop = marginM.value
                setupStep3Button()
            }
        }
    }

    private fun Node.setupStep3Button() {
        jfxButton(messages["create_local_user_step_vault_file_button_text"]) {
            addClass(Theme.buttonPrimaryStyle)

            useMaxWidth = false

            action {
                confirmStep3ButtonClicked()
            }
        }
    }

    private fun confirmStep3ButtonClicked() {
        showSaveVaultFileChooser(messages["create_local_user_step_vault_file_button_text"]) { chosenFile ->
            launchRequestSending(
                handleFailure = {
                    val errorStringResourceId = when (it) {
                        is VaultFileAlreadyExistsException -> "general_create_vault_failed_already_existing_title"
                        else -> "general_create_vault_failed_title"
                    }

                    showError(messages[errorStringResourceId])
                },
                isCancellable = false
            ) {
                val usernameValue = username.value
                val masterPasswordValue = masterPassword.value

                if (usernameValue != null && masterPasswordValue != null) {
                    viewModel.createVault(usernameValue, masterPasswordValue, chosenFile)
                } else {
                    Failure(IllegalArgumentException("The username or master password is null!"))
                }
            }
        }
    }

    private fun Node.setupFooter() {
        vbox(alignment = Pos.CENTER_LEFT) {
            jfxButton(messages["general_back"]) {
                addClass(Theme.buttonTextOnSurfaceStyle)

                action {
                    showPreviousScreen()
                }
            }
        }
    }

    private fun showStep(step: Step, animated: Boolean = true) {
        val stepViewMapping = mapOf(
            Step.Username to step1View,
            Step.MasterPassword to step2View,
            Step.VaultFileChoose to step3View,
        )

        if (animated) {
            stepViewMapping[step]?.showFadeInOutAnimation(true)
            stepViewMapping.keys.minus(step).forEach { stepViewMapping[it]?.showFadeInOutAnimation(false) }
        } else {
            stepViewMapping[step]?.isVisible = true
            stepViewMapping.keys.minus(step).forEach { stepViewMapping[it]?.isVisible = false }
        }

        currentStep = step
    }

    private enum class Step {
        Username,
        MasterPassword,
        VaultFileChoose
    }
}
