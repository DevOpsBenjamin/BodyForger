package app.bodyforger.mobile.mcp

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class McpHttpServer(
    private val dispatcher: McpDispatcher,
    private val toolRegistry: McpToolRegistry
) {
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private var serverScope: CoroutineScope? = null
    private val handler = CoroutineExceptionHandler { _, _ -> }
    private val sseSessions = ConcurrentHashMap<String, OutputStream>()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _serverPort = MutableStateFlow(DEFAULT_PORT)
    val serverPort: StateFlow<Int> = _serverPort.asStateFlow()

    fun start(scope: CoroutineScope, port: Int = DEFAULT_PORT) {
        if (_isRunning.value) return
        val customScope = CoroutineScope(Dispatchers.IO + SupervisorJob(scope.coroutineContext[Job]) + handler)
        serverScope = customScope
        val socket = try {
            ServerSocket().apply { reuseAddress = true; bind(InetSocketAddress(port)) }
        } catch (_: Throwable) { return }
        serverSocket = socket
        _serverPort.value = socket.localPort
        _isRunning.value = true

        serverJob = customScope.launch {
            try {
                while (isActive && !socket.isClosed) {
                    try {
                        val client = socket.accept()
                        launch(SupervisorJob() + handler) { handleClient(client) }
                    } catch (_: Throwable) { break }
                }
            } finally {
                try { socket.close() } catch (_: Throwable) {}
                _isRunning.value = false
            }
        }
    }

    fun stop() {
        try { serverSocket?.close() } catch (_: Throwable) {}
        serverJob?.cancel()
        serverScope?.cancel()
        serverSocket = null
        serverScope = null
        sseSessions.clear()
        _isRunning.value = false
    }

    private suspend fun handleClient(socket: Socket) {
        withContext(Dispatchers.IO) {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val out = socket.getOutputStream()

                val requestLine = reader.readLine() ?: run { socket.close(); return@withContext }
                val parts = requestLine.split(" ")
                if (parts.size < 2) { socket.close(); return@withContext }
                val method = parts[0]
                val uri = parts[1]

                var contentLength = 0
                var line = reader.readLine()
                while (!line.isNullOrEmpty()) {
                    if (line.startsWith("Content-Length:", ignoreCase = true)) {
                        contentLength = line.substringAfter(":").trim().toIntOrNull() ?: 0
                    }
                    line = reader.readLine()
                }

                val body = if (contentLength > 0) {
                    val buffer = CharArray(contentLength)
                    var read = 0
                    while (read < contentLength) {
                        val count = reader.read(buffer, read, contentLength - read)
                        if (count == -1) break
                        read += count
                    }
                    String(buffer, 0, read)
                } else ""

                val path = uri.substringBefore("?")
                val queryParams = parseQueryParams(uri)

                when {
                    method.equals("OPTIONS", ignoreCase = true) -> {
                        sendResponse(out, 204, "No Content", "text/plain", "")
                    }
                    method.equals("GET", ignoreCase = true) && (path == "/" || path == "/status") -> {
                        val json = JSONObject().apply {
                            put("status", "running")
                            put("server", McpProtocol.SERVER_NAME)
                            put("version", McpProtocol.SERVER_VERSION)
                            put("port", _serverPort.value)
                        }
                        sendResponse(out, 200, "OK", "application/json", json.toString())
                    }
                    method.equals("GET", ignoreCase = true) && path == "/health/status" -> {
                        val result = toolRegistry.executeTool(McpToolRegistry.TOOL_HEALTH_STATUS, JSONObject())
                        sendResponse(out, 200, "OK", "application/json", result.toString())
                    }
                    method.equals("GET", ignoreCase = true) && (path == "/health/summary" || path == "/health/sessions" || path == "/health/weights") -> {
                        val tool = when (path) {
                            "/health/sessions" -> McpToolRegistry.TOOL_HEALTH_READ_SESSIONS
                            "/health/weights" -> McpToolRegistry.TOOL_HEALTH_READ_WEIGHTS
                            else -> McpToolRegistry.TOOL_HEALTH_INSPECT_SUMMARY
                        }
                        val months = queryParams["months"]?.toIntOrNull() ?: if (path == "/health/summary") 12 else 6
                        val result = toolRegistry.executeTool(tool, JSONObject().apply { put("monthsBack", months) })
                        sendResponse(out, 200, "OK", "application/json", result.toString())
                    }
                    method.equals("GET", ignoreCase = true) && path == "/health/heart-rates" -> {
                        val args = JSONObject().apply {
                            put("daysBack", queryParams["days"]?.toIntOrNull() ?: 1)
                            put("limit", queryParams["limit"]?.toIntOrNull() ?: 50)
                            put("includeSamples", queryParams["samples"]?.toBoolean() ?: false)
                        }
                        val result = toolRegistry.executeTool(McpToolRegistry.TOOL_HEALTH_READ_HEART_RATES, args)
                        sendResponse(out, 200, "OK", "application/json", result.toString())
                    }
                    method.equals("GET", ignoreCase = true) && path.startsWith("/bodyforger/") -> {
                        val tool = when (path) {
                            "/bodyforger/summary" -> McpToolRegistry.TOOL_BODYFORGER_LOCAL_SUMMARY
                            "/bodyforger/exercises" -> McpToolRegistry.TOOL_LIST_EXERCISES
                            "/bodyforger/routines" -> McpToolRegistry.TOOL_LIST_ROUTINES
                            "/bodyforger/workouts" -> McpToolRegistry.TOOL_LIST_WORKOUTS
                            else -> null
                        }
                        if (tool != null) {
                            val result = toolRegistry.executeTool(tool, JSONObject())
                            sendResponse(out, 200, "OK", "application/json", result.toString())
                        } else {
                            sendResponse(out, 404, "Not Found", "application/json", """{"error":"Not Found"}""")
                        }
                    }
                    method.equals("GET", ignoreCase = true) && path == "/sse" -> {
                        handleSseConnection(out)
                        return@withContext
                    }
                    method.equals("POST", ignoreCase = true) && path == "/messages" -> {
                        handleSseMessage(out, queryParams["sessionId"] ?: "", body)
                    }
                    method.equals("POST", ignoreCase = true) && (path == "/mcp" || path == "/rpc") -> {
                        handleDirectMcp(out, body)
                    }
                    else -> sendResponse(out, 404, "Not Found", "application/json", """{"error":"Not Found"}""")
                }
                socket.close()
            } catch (t: Throwable) {
                try {
                    sendResponse(socket.getOutputStream(), 500, "Server Error", "application/json", """{"error":"${t.message?.replace("\"", "'") ?: "Error"}"}""")
                } catch (_: Throwable) {}
                try { socket.close() } catch (_: Throwable) {}
            }
        }
    }

    private fun handleSseConnection(out: OutputStream) {
        val sessionId = UUID.randomUUID().toString()
        val headers = "HTTP/1.1 200 OK\r\nContent-Type: text/event-stream; charset=utf-8\r\nCache-Control: no-cache\r\nConnection: keep-alive\r\nAccess-Control-Allow-Origin: *\r\n\r\n"
        out.write(headers.toByteArray())
        out.write("event: endpoint\r\ndata: /messages?sessionId=$sessionId\r\n\r\n".toByteArray())
        out.flush()
        sseSessions[sessionId] = out
    }

    private suspend fun handleSseMessage(out: OutputStream, sessionId: String, body: String) {
        sendResponse(out, 202, "Accepted", "text/plain", "Accepted")
        val sseOut = sseSessions[sessionId] ?: return
        try {
            val responseJson = dispatcher.dispatch(JSONObject(body))
            if (responseJson != null) sendSseEvent(sseOut, responseJson)
        } catch (t: Throwable) {
            sendSseEvent(sseOut, McpProtocol.buildError(null, -32700, "Parse error: ${t.message}"))
        }
    }

    private fun sendSseEvent(sseOut: OutputStream, data: JSONObject) {
        synchronized(sseOut) {
            sseOut.write("event: message\r\ndata: $data\r\n\r\n".toByteArray())
            sseOut.flush()
        }
    }

    private suspend fun handleDirectMcp(out: OutputStream, body: String) {
        try {
            val response = dispatcher.dispatch(JSONObject(body))
            if (response != null) sendResponse(out, 200, "OK", "application/json", response.toString())
            else sendResponse(out, 204, "No Content", "application/json", "")
        } catch (t: Throwable) {
            val err = McpProtocol.buildError(null, -32700, "Error: ${t.message}")
            sendResponse(out, 400, "Bad Request", "application/json", err.toString())
        }
    }

    private fun sendResponse(out: OutputStream, code: Int, reason: String, contentType: String, body: String) {
        val bytes = body.toByteArray(Charsets.UTF_8)
        val headers = "HTTP/1.1 $code $reason\r\nContent-Type: $contentType\r\nContent-Length: ${bytes.size}\r\nAccess-Control-Allow-Origin: *\r\nAccess-Control-Allow-Methods: GET, POST, OPTIONS\r\nAccess-Control-Allow-Headers: Content-Type\r\n\r\n"
        out.write(headers.toByteArray())
        out.write(bytes)
        out.flush()
    }

    private fun parseQueryParams(uri: String): Map<String, String> =
        uri.substringAfter("?", "").takeIf { it.isNotEmpty() }?.split("&")?.mapNotNull { pair ->
            val eq = pair.indexOf('=')
            if (eq > 0) pair.substring(0, eq) to pair.substring(eq + 1) else null
        }?.toMap() ?: emptyMap()

    companion object {
        const val DEFAULT_PORT = 8049
    }
}
