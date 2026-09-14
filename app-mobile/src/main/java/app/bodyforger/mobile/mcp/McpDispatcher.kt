package app.bodyforger.mobile.mcp

import org.json.JSONObject

class McpDispatcher(private val toolRegistry: McpToolRegistry) {

    suspend fun dispatch(requestJson: JSONObject): JSONObject? {
        val jsonrpc = requestJson.optString("jsonrpc", "")
        val id = requestJson.opt("id")
        val method = requestJson.optString("method", "")

        if (jsonrpc != McpProtocol.JSONRPC_VERSION) {
            return McpProtocol.buildError(id, CODE_INVALID_REQUEST, "Invalid JSON-RPC version")
        }

        return when (method) {
            McpProtocol.METHOD_INITIALIZE -> {
                McpProtocol.buildInitializeResponse(id)
            }
            McpProtocol.METHOD_NOTIFICATIONS_INITIALIZED -> {
                null
            }
            McpProtocol.METHOD_PING -> {
                JSONObject().apply {
                    put("jsonrpc", McpProtocol.JSONRPC_VERSION)
                    put("id", id ?: JSONObject.NULL)
                    put("result", JSONObject())
                }
            }
            McpProtocol.METHOD_TOOLS_LIST -> {
                McpProtocol.buildToolsListResponse(id, toolRegistry.listToolsJson())
            }
            McpProtocol.METHOD_TOOLS_CALL -> {
                val params = requestJson.optJSONObject("params")
                val toolName = params?.optString("name") ?: ""
                val arguments = params?.optJSONObject("arguments") ?: JSONObject()
                try {
                    val result = toolRegistry.executeTool(toolName, arguments)
                    val isError = result.optBoolean("error", false)
                    McpProtocol.buildToolCallResponse(id, result.toString(2), isError)
                } catch (e: Exception) {
                    McpProtocol.buildToolCallResponse(id, "Tool execution failed: ${e.message}", isError = true)
                }
            }
            else -> {
                McpProtocol.buildError(id, CODE_METHOD_NOT_FOUND, "Method not found: $method")
            }
        }
    }

    companion object {
        const val CODE_INVALID_REQUEST = -32600
        const val CODE_METHOD_NOT_FOUND = -32601
    }
}
