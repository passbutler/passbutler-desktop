package de.passbutler.desktop.base

import de.passbutler.common.base.Failure
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tinylog.kotlin.Logger
import tornadofx.ConfigProperties
import tornadofx.Configurable

/**
 * Wraps blocking IO `Configurable` read calls in coroutine function with correct dispatcher.
 */
suspend fun <T> Configurable.readConfigProperty(configPropertyGetter: ConfigProperties.() -> T?): Result<T> {
    return withContext(Dispatchers.IO) {
        val readValue = config.use {
            try {
                configPropertyGetter(it)
            } catch (exception: Exception) {
                Logger.warn(exception, "The configuration value could not be read!")
                null
            }
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
                try {
                    configPropertySetter(it)
                } catch (exception: Exception) {
                    Logger.warn(exception, "The configuration value could not be written!")
                }
            }
        }

        Success(Unit)
    } catch (exception: Exception) {
        Failure(exception)
    }
}

object ConfigPropertyNotFoundException : Exception("The value was not found!")
