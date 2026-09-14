package app.bodyforger.mobile.mcp

import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class McpProtocolTest {

    private val toolRegistry = McpToolRegistry()
    private val dispatcher = McpDispatcher(toolRegistry)

    @Test
    fun initializeReturnsExpectedServerInfoAndCapabilities() = runBlocking {
        val request = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", "initialize")
        }
        val response = dispatcher.dispatch(request)
        assertNotNull(response)
        assertEquals("2.0", response!!.getString("jsonrpc"))
        assertEquals(1, response.getInt("id"))

        val result = response.getJSONObject("result")
        assertEquals(McpProtocol.PROTOCOL_VERSION, result.getString("protocolVersion"))
        assertEquals("bodyforger-mobile", result.getJSONObject("serverInfo").getString("name"))
        assertTrue(result.getJSONObject("capabilities").has("tools"))
    }

    @Test
    fun notificationsInitializedReturnsNullResponse() = runBlocking {
        val request = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("method", "notifications/initialized")
        }
        val response = dispatcher.dispatch(request)
        assertNull(response)
    }

    @Test
    fun toolsListReturnsToolsArrayWithExpectedNames() = runBlocking {
        val request = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", 42)
            put("method", "tools/list")
        }
        val response = dispatcher.dispatch(request)
        assertNotNull(response)
        val tools = response!!.getJSONObject("result").getJSONArray("tools")
        assertTrue(tools.length() >= 5)

        val toolNames = (0 until tools.length()).map { tools.getJSONObject(it).getString("name") }
        assertEquals(15, toolNames.size)
        assertTrue(toolNames.contains("health_connect_status"))
        assertTrue(toolNames.contains("health_connect_inspect_summary"))
        assertTrue(toolNames.contains("health_connect_read_sessions"))
        assertTrue(toolNames.contains("health_connect_read_weights"))
        assertTrue(toolNames.contains("health_connect_read_heart_rates"))
        assertTrue(toolNames.contains("bodyforger_local_summary"))
        assertTrue(toolNames.contains("bodyforger_search_exercises"))
        assertTrue(toolNames.contains("bodyforger_list_exercises"))
        assertTrue(toolNames.contains("bodyforger_create_exercise"))
        assertTrue(toolNames.contains("bodyforger_create_routine"))
        assertTrue(toolNames.contains("bodyforger_list_routines"))
        assertTrue(toolNames.contains("bodyforger_get_routine"))
        assertTrue(toolNames.contains("bodyforger_insert_workout"))
        assertTrue(toolNames.contains("bodyforger_list_workouts"))
        assertTrue(toolNames.contains("bodyforger_get_workout"))
    }

    @Test
    fun toolsCallUnknownToolReturnsError() = runBlocking {
        val request = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", 3)
            put("method", "tools/call")
            put("params", JSONObject().apply {
                put("name", "non_existent_tool")
            })
        }
        val response = dispatcher.dispatch(request)
        assertNotNull(response)
        val content = response!!.getJSONObject("result").getJSONArray("content")
        assertTrue(content.getJSONObject(0).getString("text").contains("Unknown tool"))
    }

    @Test
    fun invalidMethodReturnsMethodNotFoundErrorCode() = runBlocking {
        val request = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", 99)
            put("method", "some_random_method")
        }
        val response = dispatcher.dispatch(request)
        assertNotNull(response)
        val error = response!!.getJSONObject("error")
        assertEquals(-32601, error.getInt("code"))
    }

    @Test
    fun toolsListWithoutConsentReturnsOnlyBodyForgerToolsAndStatus() = runBlocking<Unit> {
        val registry = McpToolRegistry().apply { consentOverride = false }
        val noConsentDispatcher = McpDispatcher(registry)
        val request = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", 100)
            put("method", "tools/list")
        }
        val response = noConsentDispatcher.dispatch(request)
        assertNotNull(response)
        val tools = response!!.getJSONObject("result").getJSONArray("tools")
        assertEquals(11, tools.length())

        val toolNames = (0 until tools.length()).map { tools.getJSONObject(it).getString("name") }
        assertTrue(toolNames.contains("health_connect_status"))
        assertTrue(toolNames.contains("bodyforger_local_summary"))
        assertTrue(toolNames.contains("bodyforger_search_exercises"))
        assertFalse(toolNames.contains("health_connect_read_sessions"))
        assertFalse(toolNames.contains("health_connect_inspect_summary"))
        assertFalse(toolNames.contains("health_connect_read_weights"))
        assertFalse(toolNames.contains("health_connect_read_heart_rates"))
    }

    @Test
    fun healthToolCallWithoutConsentIsRefused() = runBlocking<Unit> {
        val registry = McpToolRegistry().apply { consentOverride = false }
        val noConsentDispatcher = McpDispatcher(registry)
        val request = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", 101)
            put("method", "tools/call")
            put("params", JSONObject().apply {
                put("name", "health_connect_read_sessions")
            })
        }
        val response = noConsentDispatcher.dispatch(request)
        assertNotNull(response)
        val result = response!!.getJSONObject("result")
        assertTrue(result.getBoolean("isError"))
        val text = result.getJSONArray("content").getJSONObject(0).getString("text")
        assertTrue(text.contains("Google Health Connect consent has not been granted"))
    }

    @Test
    fun statusToolWithoutConsentReportsConsentFalse() = runBlocking<Unit> {
        val registry = McpToolRegistry().apply { consentOverride = false }
        val noConsentDispatcher = McpDispatcher(registry)
        val request = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", 102)
            put("method", "tools/call")
            put("params", JSONObject().apply {
                put("name", "health_connect_status")
            })
        }
        val response = noConsentDispatcher.dispatch(request)
        assertNotNull(response)
        val text = response!!.getJSONObject("result").getJSONArray("content").getJSONObject(0).getString("text")
        val json = JSONObject(text)
        assertFalse(json.getBoolean("consentGranted"))
        assertTrue(json.getString("message").contains("Only bodyforger_* tools are active"))
    }
}
