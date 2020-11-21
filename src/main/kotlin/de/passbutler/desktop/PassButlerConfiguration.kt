package de.passbutler.desktop

import de.passbutler.common.base.JSONSerializable
import de.passbutler.common.base.JSONSerializableDeserializer
import org.json.JSONException
import org.json.JSONObject

// TODO: Add unit tests
// TODO: Should be a set
data class PassButlerConfiguration(
    val recentVaultFiles: List<String>
) : JSONSerializable {

    override fun serialize(): JSONObject {
        return JSONObject().apply {
            putStringList(SERIALIZATION_RECENT_VAULT_FILES, recentVaultFiles)
        }
    }

    object Deserializer : JSONSerializableDeserializer<PassButlerConfiguration>() {
        @Throws(JSONException::class)
        override fun deserialize(jsonObject: JSONObject): PassButlerConfiguration {
            return PassButlerConfiguration(
                recentVaultFiles = jsonObject.getStringListOrNull(SERIALIZATION_RECENT_VAULT_FILES) ?: emptyList()
            )
        }
    }

    companion object {
        private const val SERIALIZATION_RECENT_VAULT_FILES = "recentVaultFiles"
    }
}

fun JSONObject.putStringList(key: String, value: List<String>?): JSONObject {
    return put(key, value)
}

fun JSONObject.getStringListOrNull(key: String): List<String>? {
    val jsonArray = getJSONArray(key)

    return with(jsonArray) {
        (0 until length()).asSequence().mapNotNull { get(it) as? String }.toList()
    }
}
