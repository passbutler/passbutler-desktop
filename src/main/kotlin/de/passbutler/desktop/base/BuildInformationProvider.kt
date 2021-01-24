package de.passbutler.desktop.base

import de.passbutler.common.base.BuildInformationProviding
import de.passbutler.common.base.BuildType
import de.passbutler.desktop.BuildConfig

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
