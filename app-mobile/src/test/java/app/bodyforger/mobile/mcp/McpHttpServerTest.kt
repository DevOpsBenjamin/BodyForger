package app.bodyforger.mobile.mcp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class McpHttpServerTest {

    private val testPort = 18080
    private lateinit var scope: CoroutineScope
    private lateinit var server: McpHttpServer

    @Before
    fun setup() {
        scope = CoroutineScope(Dispatchers.IO)
        val registry = McpToolRegistry()
        val dispatcher = McpDispatcher(registry)
        server = McpHttpServer(dispatcher, registry)
        server.start(scope, testPort)
    }

    @After
    fun tearDown() {
        server.stop()
        scope.cancel()
    }

    @Test
    fun getStatusReturnsRunningJson() = runBlocking {
        delay(100)
        val url = URL("http://127.0.0.1:$testPort/status")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        assertEquals(200, conn.responseCode)
        val body = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(body)
        assertEquals("running", json.getString("status"))
        assertEquals("bodyforger-mobile", json.getString("server"))
    }

    @Test
    fun postMcpDirectReturnsJsonRpcResponse() = runBlocking {
        delay(100)
        val url = URL("http://127.0.0.1:$testPort/mcp")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")

        val payload = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", 10)
            put("method", "tools/list")
        }

        OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
        assertEquals(200, conn.responseCode)

        val responseBody = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(responseBody)
        assertEquals("2.0", json.getString("jsonrpc"))
        assertEquals(10, json.getInt("id"))
        assertTrue(json.getJSONObject("result").getJSONArray("tools").length() > 0)
    }
}
