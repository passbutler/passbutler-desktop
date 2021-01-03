package de.passbutler.desktop.base

import de.passbutler.common.base.Failure
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tornadofx.ConfigProperties
import tornadofx.Configurable

/**
 * Wraps blocking IO `Configurable` read calls in coroutine function with correct dispatcher.
 */
suspend fun <T> Configurable.readConfigProperty(configPropertyGetter: ConfigProperties.() -> T?): Result<T> {
    return withContext(Dispatchers.IO) {
        val readValue = config.use {
            configPropertyGetter(it)
        }

        if (readValue != null) {
            Success(readValue)
        } else {
            Failure(ConfigPropertyNotFoundException)
        }
    }
}

/**
 * Wraps blocking IO `Configurable` write calls in coroutine function with correct dispatcher.
 */
suspend fun Configurable.writeConfigProperty(configPropertySetter: ConfigProperties.() -> Unit): Result<Unit> {
    return try {
        withContext(Dispatchers.IO) {
            config.use {
                configPropertySetter(it)
            }
        }

        Success(Unit)
    } catch (exception: Exception) {
        Failure(exception)
    }
}

object ConfigPropertyNotFoundException : Exception("The value was not found!")
