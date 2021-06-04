package de.passbutler.desktop.base

object UrlExtensions {
    fun isNetworkUrl(url: String?): Boolean {
        return isHttpUrl(url) || isHttpsUrl(url)
    }

    fun isHttpUrl(url: String?): Boolean {
        return url != null && url.length > 6 && url.substring(0, 7).equals("http://", true)
    }

    fun isHttpsUrl(url: String?): Boolean {
        return url != null && url.length > 7 && url.substring(0, 8).equals("https://", true)
    }
}
