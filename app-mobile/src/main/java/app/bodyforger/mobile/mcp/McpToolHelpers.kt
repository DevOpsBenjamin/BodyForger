package app.bodyforger.mobile.mcp

import org.json.JSONArray
import org.json.JSONObject

internal object McpToolHelpers {

    fun toolDescriptor(
        name: String,
        description: String,
        properties: JSONObject = JSONObject(),
        required: List<String> = emptyList()
    ): JSONObject = JSONObject().apply {
        put("name", name)
        put("description", description)
        put("inputSchema", JSONObject().apply {
            put("type", "object")
            put("properties", properties)
            if (required.isNotEmpty()) {
                put("required", JSONArray(required))
            }
        })
    }

    fun intProp(desc: String): JSONObject = JSONObject().apply {
        put("type", "integer")
        put("description", desc)
    }

    fun numProp(desc: String): JSONObject = JSONObject().apply {
        put("type", "number")
        put("description", desc)
    }

    fun boolProp(desc: String): JSONObject = JSONObject().apply {
        put("type", "boolean")
        put("description", desc)
    }

    fun stringProp(desc: String): JSONObject = JSONObject().apply {
        put("type", "string")
        put("description", desc)
    }

    fun arrayProp(desc: String): JSONObject = JSONObject().apply {
        put("type", "array")
        put("description", desc)
    }

    fun errorJson(message: String): JSONObject = JSONObject().apply {
        put("error", true)
        put("message", message)
    }
}
