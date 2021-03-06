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
import tornadofx.whenUndocked
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

interface BaseUIComponent : UIPresenting, CoroutineScope {

    var transitionType: TransitionType
    var uiPresentingDelegate: UIPresenting?

    override fun <T : UIComponent> showScreen(screenClass: KClass<T>, parameters: Map<*, Any?>?, userTriggered: Boolean, transitionType: TransitionType) {
        requireUIPresentingDelegate().showScreen(screenClass, parameters, userTriggered, transitionType)
    }

    override fun <T : UIComponent> isScreenShown(screenClass: KClass<T>): Boolean {
        return requireUIPresentingDelegate().isScreenShown(screenClass)
    }

    override fun shownScreen(): UIComponent {
        return requireUIPresentingDelegate().shownScreen()
    }

    override fun showProgress() {
        requireUIPresentingDelegate().showProgress()
    }

    override fun hideProgress() {
        requireUIPresentingDelegate().hideProgress()
    }

    override fun showInformation(message: String) {
        requireUIPresentingDelegate().showInformation(message)
    }

    override fun showError(message: String) {
        requireUIPresentingDelegate().showError(message)
    }

    override fun showDialog(dialog: Dialog) {
        requireUIPresentingDelegate().showDialog(dialog)
    }

    override fun dismissDialog() {
        requireUIPresentingDelegate().dismissDialog()
    }

    fun addUndockedObserver(observer: () -> Unit)

    private fun requireUIPresentingDelegate(): UIPresenting {
        return uiPresentingDelegate ?: throw UIPresentingDelegateUninitializedException
    }

    object UIPresentingDelegateUninitializedException : IllegalStateException("The UIPresenting delegate is not initialized!")
}

/**
 * A screen view that have only a single instance (the same instance will always be used if shown).
 */
abstract class BaseView(title: String? = null, icon: Node? = null) : View(title, icon), BaseUIComponent {

    override var transitionType = TransitionType.NONE
    override var uiPresentingDelegate: UIPresenting? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + coroutineJob

    // Do not cancel in `onUndock()` because a `View` is a singleton and the `CoroutineScope` would be unusable anymore
    private val coroutineJob = SupervisorJob()

    override fun addUndockedObserver(observer: () -> Unit) {
        whenUndocked { observer.invoke() }
    }
}

/**
 * A screen view that can have multiple instances (a new instance will always be created if shown).
 */
abstract class BaseFragment(title: String? = null, icon: Node? = null) : Fragment(title, icon), BaseUIComponent {

    override var transitionType = TransitionType.NONE
    override var uiPresentingDelegate: UIPresenting? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + coroutineJob

    private val coroutineJob = SupervisorJob()

    override fun onUndock() {
        super.onUndock()
        coroutineJob.cancel()
    }

    override fun addUndockedObserver(observer: () -> Unit) {
        whenUndocked { observer.invoke() }
    }
}

/**
 * Injects a `ViewModel` with private scope to ensure it is always a non-shared instance.
 */
inline fun <reified T> Component.injectWithPrivateScope(parameters: Map<String, Any?>? = null)
    where T : ViewModel,
          T : ScopedInstance = inject<T>(Scope(), parameters)
