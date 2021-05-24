package de.passbutler.desktop.ui

import de.passbutler.common.ui.BannerPresenting
import de.passbutler.common.ui.DebouncedUIPresenting
import de.passbutler.common.ui.ProgressPresenting
import de.passbutler.common.ui.TransitionType
import de.passbutler.desktop.RootScreen
import javafx.scene.Node
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Pane
import org.tinylog.kotlin.Logger
import tornadofx.UIComponent
import tornadofx.add
import tornadofx.clear
import tornadofx.find
import tornadofx.getChildList
import tornadofx.replaceWith
import java.time.Instant
import kotlin.reflect.KClass

class UIPresenter(
    rootScreen: RootScreen
) : UIPresenting,
    ScreenPresenting by ScreenPresenter(rootScreen),
    ProgressPresenting by ProgressPresenter(rootScreen.progressView),
    BannerPresenting by BannerPresenter(rootScreen.bannerView),
    DialogPresenting by DialogPresenter(rootScreen.dialogContainerView, rootScreen)

class ScreenPresenter(private val rootScreen: RootScreen) : ScreenPresenting, DebouncedUIPresenting {

    override var lastViewTransactionTime: Instant? = null

    private var shownScreen: UIComponent = rootScreen

    override fun <T : UIComponent> showScreen(screenClass: KClass<T>, parameters: Map<*, Any?>?, userTriggered: Boolean, transitionType: TransitionType) {
        val debouncedViewTransactionEnsured = ensureDebouncedViewTransaction().takeIf { userTriggered } ?: true

        if (debouncedViewTransactionEnsured) {
            rootScreen.contentContainer.getChildList()?.let { contentContainerChildList ->
                val screenInstance = find(screenClass, params = parameters)
                rootScreen.titleProperty.bind(screenInstance.titleProperty)

                if (screenInstance is BaseUIComponent) {
                    screenInstance.transitionType = transitionType
                    screenInstance.uiPresentingDelegate = rootScreen.uiPresentingDelegate
                }

                val existingScreen = contentContainerChildList.lastOrNull()

                if (existingScreen != null) {
                    existingScreen.replaceWith(screenInstance.root, transitionType.createViewTransition())
                } else {
                    contentContainerChildList.add(screenInstance.root)
                }

                shownScreen = screenInstance
            }
        } else {
            Logger.warn("The view transaction was ignored because a recent transaction was already done!")
        }
    }

    override fun <T : UIComponent> isScreenShown(screenClass: KClass<T>): Boolean {
        return shownScreen.javaClass.kotlin == screenClass
    }

    override fun shownScreen(): UIComponent {
        return shownScreen
    }
}

class ProgressPresenter(private val progressView: Node) : ProgressPresenting {
    override fun showProgress() {
        progressView.showFadeInOutAnimation(true)
    }

    override fun hideProgress() {
        progressView.showFadeInOutAnimation(false)
    }
}

class BannerPresenter(private val bannerView: BannerView) : BannerPresenting {
    override fun showInformation(message: String) {
        bannerView.show(message)
    }

    override fun showError(message: String) {
        // Same as information at the moment
        showInformation(message)
    }
}

class DialogPresenter(private val dialogContainerView: Pane, private val screenPresenting: ScreenPresenting) : DialogPresenting {

    private var acceleratorsCopy: Map<KeyCombination, () -> Unit>? = null

    override fun showDialog(dialog: Dialog) {
        // If a previous dialog is still shown, restore accelerators and remove view first
        if (dialogContainerView.children.size > 0) {
            restoreAccelerators()
            dialogContainerView.clear()
        }

        snapshotAccelerators()

        dialogContainerView.add(dialog)
        dialogContainerView.showFadeInOutAnimation(true) {
            // After animation gain focus on dialog view to remove it from previous views (otherwise default/cancel button behaviour of dialog is not working)
            dialog.requestFocus()
        }
    }

    override fun dismissDialog() {
        restoreAccelerators()

        dialogContainerView.showFadeInOutAnimation(false) {
            // As recently as the animation is finished, remove dialog view from container
            dialogContainerView.clear()
        }
    }

    private fun snapshotAccelerators() {
        val presentingScreen = screenPresenting.shownScreen()

        // Deep copy the current accelerators from `UIComponent`
        acceleratorsCopy = presentingScreen.accelerators.toMap()

        presentingScreen.accelerators.clear()
    }

    private fun restoreAccelerators() {
        val presentingScreen = screenPresenting.shownScreen()

        acceleratorsCopy?.let {
            presentingScreen.accelerators.putAll(it)
        }

        acceleratorsCopy = null
    }
}
