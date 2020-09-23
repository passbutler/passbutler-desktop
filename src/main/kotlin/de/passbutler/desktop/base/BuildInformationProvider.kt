package de.passbutler.desktop.base

import de.passbutler.common.base.BuildInformationProviding
import de.passbutler.common.base.BuildType

object BuildInformationProvider : BuildInformationProviding {
    override val buildType: BuildType
        get() = when (BuildConfig.BUILD_TYPE) {
            "debug" -> BuildType.Debug
            "release" -> BuildType.Release
            else -> BuildType.Other
        }

    override val applicationIdentification: String
        get() = "${BuildConfig.APPLICATION_ID} ${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}"
}

// TODO: Equivalent to Android pendant
object BuildConfig {
    const val APPLICATION_ID = "de.passbutler.desktop.debug"
    const val BUILD_TYPE = "debug"
    const val VERSION_CODE = 1
    const val VERSION_NAME = "1.0.0"
}