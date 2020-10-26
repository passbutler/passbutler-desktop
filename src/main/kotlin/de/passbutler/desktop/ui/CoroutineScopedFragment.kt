package de.passbutler.desktop.ui

import javafx.scene.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import tornadofx.Fragment
import tornadofx.View
import kotlin.coroutines.CoroutineContext

abstract class CoroutineScopedFragment(title: String? = null, icon: Node? = null) : Fragment(title, icon), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + coroutineJob

    private val coroutineJob = SupervisorJob()

    override fun onUndock() {
        super.onUndock()
        coroutineJob.cancel()
    }
}