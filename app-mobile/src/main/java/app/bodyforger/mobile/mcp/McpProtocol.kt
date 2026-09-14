package app.bodyforger.mobile.mcp

import org.json.JSONArray
import org.json.JSONObject

object McpProtocol {
    const val JSONRPC_VERSION = "2.0"
    const val PROTOCOL_VERSION = "2024-11-05"
    const val SERVER_NAME = "bodyforger-mobile"
    const val SERVER_VERSION = "1.0.0"

    const val METHOD_INITIALIZE = "initialize"
    const val METHOD_NOTIFICATIONS_INITIALIZED = "notifications/initialized"
    const val METHOD_PING = "ping"
    const val METHOD_TOOLS_LIST = "tools/list"
    const val METHOD_TOOLS_CALL = "tools/call"

    fun buildInitializeResponse(id: Any?): JSONObject {
        val result = JSONObject().apply {
            put("protocolVersion", PROTOCOL_VERSION)
            put("capabilities", JSONObject().apply {
                put("tools", JSONObject())
            })
            put("serverInfo", JSONObject().apply {
                put("name", SERVER_NAME)
                put("version", SERVER_VERSION)
            })
        }
        return wrapSuccess(id, result)
    }

    fun buildToolsListResponse(id: Any?, tools: JSONArray): JSONObject {
        val result = JSONObject().apply {
            put("tools", tools)
        }
        return wrapSuccess(id, result)
    }

    fun buildToolCallResponse(id: Any?, textContent: String, isError: Boolean = false): JSONObject {
        val contentArray = JSONArray().apply {
            put(JSONObject().apply {
                put("type", "text")
                put("text", textContent)
            })
        }
        val result = JSONObject().apply {
            put("content", contentArray)
            put("isError", isError)
        }
        return wrapSuccess(id, result)
    }

    fun buildError(id: Any?, code: Int, message: String): JSONObject {
        return JSONObject().apply {
            put("jsonrpc", JSONRPC_VERSION)
            put("id", id ?: JSONObject.NULL)
            put("error", JSONObject().apply {
                put("code", code)
                put("message", message)
            })
        }
    }

    private fun wrapSuccess(id: Any?, result: JSONObject): JSONObject {
        return JSONObject().apply {
            put("jsonrpc", JSONRPC_VERSION)
            put("id", id ?: JSONObject.NULL)
            put("result", result)
        }
    }
}
