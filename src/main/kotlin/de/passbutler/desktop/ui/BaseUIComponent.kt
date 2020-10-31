package de.passbutler.desktop.ui

import de.passbutler.common.ui.TransitionType
import javafx.scene.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import tornadofx.Fragment
import tornadofx.UIComponent
import tornadofx.View
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

interface BaseUIComponent : UIPresenting {

    var transitionType: TransitionType
    var uiPresentingDelegate: UIPresenting?

    override fun <T : UIComponent> showScreen(screenClass: KClass<T>, debounce: Boolean, transitionType: TransitionType) {
        uiPresentingDelegate?.showScreen(screenClass, debounce, transitionType)
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