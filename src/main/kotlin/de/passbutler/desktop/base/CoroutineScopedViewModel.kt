package de.passbutler.desktop.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import tornadofx.ViewModel
import kotlin.coroutines.CoroutineContext

open class CoroutineScopedViewModel : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + coroutineJob

    private val coroutineJob = SupervisorJob()

    fun cancelJobs() {
        coroutineJob.cancel()
    }
}