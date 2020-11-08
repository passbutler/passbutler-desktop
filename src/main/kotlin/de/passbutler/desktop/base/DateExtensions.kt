package de.passbutler.desktop.base

import java.time.Instant

val Instant.formattedDateTime: String
    get() {
        // TODO: Proper formatting
        return toString()
    }
