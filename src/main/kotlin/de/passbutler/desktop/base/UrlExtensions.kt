package de.passbutler.desktop.base

import org.tinylog.kotlin.Logger
import java.net.URI

object UrlExtensions {
    fun isNetworkUrl(url: String?): Boolean {
        return isHttpUrl(url) || isHttpsUrl(url)
    }

    fun isHttpUrl(url: String?): Boolean {
        val scheme = obtainScheme(url)
        return scheme != null && scheme.equals(URL_SCHEME_HTTP, true)
    }

    fun isHttpsUrl(url: String?): Boolean {
        val scheme = obtainScheme(url)
        return scheme != null && scheme.equals(URL_SCHEME_HTTPS, true)
    }

    fun obtainScheme(url: String?): String? {
        return url?.let {
            try {
                URI.create(it).scheme
            } catch (exception: Exception) {
                Logger.info("The given URL is not valid: ${exception.message}")
                null
            }
        }
    }

    private const val URL_SCHEME_HTTP = "http"
    private const val URL_SCHEME_HTTPS = "https"
}
