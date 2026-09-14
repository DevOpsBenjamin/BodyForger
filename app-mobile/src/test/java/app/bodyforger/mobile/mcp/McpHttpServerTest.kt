package app.bodyforger.mobile.mcp

import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
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

    private class FakeSharedPreferences : SharedPreferences {
        val map = mutableMapOf<String, Any?>()
        override fun getAll(): Map<String, *> = map
        override fun getString(key: String?, defValue: String?): String? = (map[key] as? String) ?: defValue
        @Suppress("UNCHECKED_CAST")
        override fun getStringSet(key: String?, defValues: Set<String>?): Set<String>? = (map[key] as? Set<String>) ?: defValues
        override fun getInt(key: String?, defValue: Int): Int = (map[key] as? Int) ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = (map[key] as? Long) ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = (map[key] as? Float) ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = (map[key] as? Boolean) ?: defValue
        override fun contains(key: String?): Boolean = map.containsKey(key)
        override fun edit(): SharedPreferences.Editor = FakeEditor(this)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    }

    private class FakeEditor(private val sp: FakeSharedPreferences) : SharedPreferences.Editor {
        override fun putString(key: String?, value: String?): SharedPreferences.Editor = apply { sp.map[key!!] = value }
        override fun putStringSet(key: String?, values: Set<String>?): SharedPreferences.Editor = apply { sp.map[key!!] = values }
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = apply { sp.map[key!!] = value }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = apply { sp.map[key!!] = value }
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = apply { sp.map[key!!] = value }
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = apply { sp.map[key!!] = value }
        override fun remove(key: String?): SharedPreferences.Editor = apply { sp.map.remove(key) }
        override fun clear(): SharedPreferences.Editor = apply { sp.map.clear() }
        override fun commit(): Boolean = true
        override fun apply() {}
    }

    private lateinit var scope: CoroutineScope
    private lateinit var authManager: McpAuthManager
    private lateinit var server: McpHttpServer
    private var testPort: Int = 0

    @Before
    fun setup() {
        scope = CoroutineScope(Dispatchers.IO)
        val registry = McpToolRegistry()
        val dispatcher = McpDispatcher(registry)
        authManager = McpAuthManager(FakeSharedPreferences())
        server = McpHttpServer(dispatcher, registry, authManager)
        server.start(scope, 0)
        testPort = server.serverPort.value
    }

    @After
    fun tearDown() {
        server.stop()
        scope.cancel()
    }

    @Test
    fun defaultPortIs8049() {
        assertEquals(8049, McpHttpServer.DEFAULT_PORT)
    }

    @Test
    fun getStatus_isPublicWithoutAuth() = runBlocking {
        val url = URL("http://127.0.0.1:$testPort/status")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 3000
        conn.readTimeout = 3000
        try {
            conn.requestMethod = "GET"
            assertEquals(200, conn.responseCode)
            val body = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(body)
            assertEquals("running", json.getString("status"))
        } finally {
            conn.disconnect()
        }
    }

    @Test
    fun postMcpDirect_withoutAuth_returns401() = runBlocking {
        val url = URL("http://127.0.0.1:$testPort/mcp")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 3000
        conn.readTimeout = 3000
        try {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            OutputStreamWriter(conn.outputStream).use { it.write("""{"jsonrpc":"2.0","id":1,"method":"tools/list"}""") }
            assertEquals(401, conn.responseCode)
        } finally {
            conn.disconnect()
        }
    }

    @Test
    fun postPair_withValidCode_returnsBearerTokenAndAllowsCalls() = runBlocking {
        val pairingCode = authManager.pairingCode.value
        val pairUrl = URL("http://127.0.0.1:$testPort/auth/pair")
        val pairConn = pairUrl.openConnection() as HttpURLConnection
        pairConn.connectTimeout = 3000
        pairConn.readTimeout = 3000
        val token: String
        try {
            pairConn.requestMethod = "POST"
            pairConn.doOutput = true
            pairConn.setRequestProperty("Content-Type", "application/json")
            val payload = JSONObject().apply {
                put("code", pairingCode)
                put("client", "Test Client")
            }
            OutputStreamWriter(pairConn.outputStream).use { it.write(payload.toString()) }
            assertEquals(200, pairConn.responseCode)
            val body = pairConn.inputStream.bufferedReader().readText()
            val json = JSONObject(body)
            assertEquals("authorized", json.getString("status"))
            token = json.getString("token")
            assertTrue(token.startsWith("bf_sec_"))
        } finally {
            pairConn.disconnect()
        }

        // Now call /mcp with the Bearer token
        val mcpUrl = URL("http://127.0.0.1:$testPort/mcp")
        val mcpConn = mcpUrl.openConnection() as HttpURLConnection
        mcpConn.connectTimeout = 3000
        mcpConn.readTimeout = 3000
        try {
            mcpConn.requestMethod = "POST"
            mcpConn.doOutput = true
            mcpConn.setRequestProperty("Content-Type", "application/json")
            mcpConn.setRequestProperty("Authorization", "Bearer $token")
            val payload = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", 10)
                put("method", "tools/list")
            }
            OutputStreamWriter(mcpConn.outputStream).use { it.write(payload.toString()) }
            assertEquals(200, mcpConn.responseCode)
            val body = mcpConn.inputStream.bufferedReader().readText()
            val json = JSONObject(body)
            assertEquals("2.0", json.getString("jsonrpc"))
            assertTrue(json.getJSONObject("result").getJSONArray("tools").length() > 0)
        } finally {
            mcpConn.disconnect()
        }
    }
}
