package de.passbutler.desktop.ui

import de.passbutler.common.ui.TransitionType
import javafx.scene.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import tornadofx.Component
import tornadofx.Fragment
import tornadofx.Scope
import tornadofx.ScopedInstance
import tornadofx.UIComponent
import tornadofx.View
import tornadofx.ViewModel
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

interface BaseUIComponent : UIPresenting {

    var transitionType: TransitionType
    var uiPresentingDelegate: UIPresenting?

    override fun <T : UIComponent> showScreen(screenClass: KClass<T>, debounce: Boolean, transitionType: TransitionType) {
        uiPresentingDelegate?.showScreen(screenClass, debounce, transitionType)
    }

    override fun <T : UIComponent> isScreenShown(screenClass: KClass<T>): Boolean {
        return uiPresentingDelegate?.isScreenShown(screenClass) ?: false
    }

    override fun showProgress() {
        uiPresentingDelegate?.showProgress()
    }

    override fun hideProgress() {
        uiPresentingDelegate?.hideProgress()
    }

    override fun showInformation(message: String) {
        uiPresentingDelegate?.showInformation(message)
    }

    override fun showError(message: String) {
        uiPresentingDelegate?.showError(message)
    }
}

/**
 * A screen view that have only a single instance (the same instance will be used always if shown).
 */
abstract class BaseView(title: String? = null, icon: Node? = null) : View(title, icon), BaseUIComponent, CoroutineScope {

    override var transitionType = TransitionType.NONE
    override var uiPresentingDelegate: UIPresenting? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + coroutineJob

    private val coroutineJob = SupervisorJob()

    override fun onUndock() {
        super.onUndock()
        coroutineJob.cancel()
    }
}

/**
 * A screen view that can have multiple instances (a new instance will be created always if shown).
 */
abstract class BaseFragment(title: String? = null, icon: Node? = null) : Fragment(title, icon), BaseUIComponent, CoroutineScope {

    override var transitionType = TransitionType.NONE
    override var uiPresentingDelegate: UIPresenting? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + coroutineJob

    private val coroutineJob = SupervisorJob()

    override fun onUndock() {
        super.onUndock()
        coroutineJob.cancel()
    }
}

/**
 * Injects a `ViewModel` with private scope to ensure it is always a non-shared instance.
 */
inline fun <reified T> Component.injectWithPrivateScope()
    where T : ViewModel,
          T : ScopedInstance = inject<T>(Scope())
