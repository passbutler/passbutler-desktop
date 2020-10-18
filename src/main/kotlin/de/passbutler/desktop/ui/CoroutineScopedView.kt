package de.passbutler.desktop.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import tornadofx.View
import kotlin.coroutines.CoroutineContext

abstract class CoroutineScopedView : View(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + coroutineJob

    private val coroutineJob = SupervisorJob()

    override fun onUndock() {
        super.onUndock()
        coroutineJob.cancel()
    }
}