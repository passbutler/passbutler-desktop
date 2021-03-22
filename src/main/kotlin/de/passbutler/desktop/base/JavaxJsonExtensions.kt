package de.passbutler.desktop.base

import javax.json.Json
import javax.json.JsonArray

fun List<String>.toJavaxJsonArray(): JsonArray {
    val stringList = this
    return Json.createArrayBuilder().apply {
        stringList.forEach {
            add(it)
        }
    }.build()
}
