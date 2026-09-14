package app.bodyforger.mobile.mcp

import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class McpAuthManagerTest {

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

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var authManager: McpAuthManager
    private val testScope = TestScope()

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
        authManager = McpAuthManager(fakePrefs)
    }

    @Test
    fun pairingCode_isSixDigits() {
        val code = authManager.pairingCode.value
        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })
    }

    @Test
    fun validateAndPair_withCorrectCode_succeedsAndRegeneratesCode() {
        val originalCode = authManager.pairingCode.value
        val device = authManager.validateAndPair(originalCode, "Claude Desktop")

        assertNotNull(device)
        assertEquals("Claude Desktop", device?.name)
        assertTrue(device!!.token.startsWith("bf_sec_"))
        assertEquals(1, authManager.pairedDevices.value.size)
        assertTrue(authManager.isAuthorized(device.token))
        assertTrue(authManager.isAuthorized("Bearer ${device.token}"))

        // Original code should now be invalidated
        assertNull(authManager.validateAndPair(originalCode, "Another Client"))
    }

    @Test
    fun validateAndPair_withWrongCode_fails() {
        val device = authManager.validateAndPair("000000", "Bad Client")
        assertNull(device)
        assertEquals(0, authManager.pairedDevices.value.size)
    }

    @Test
    fun revokeDevice_invalidatesToken() {
        val code = authManager.pairingCode.value
        val device = authManager.validateAndPair(code, "Test Device")!!

        assertTrue(authManager.isAuthorized(device.token))
        val revoked = authManager.revokeDevice(device.id)
        assertTrue(revoked)
        assertFalse(authManager.isAuthorized(device.token))
        assertEquals(0, authManager.pairedDevices.value.size)
    }

    @Test
    fun isAuthorized_rejectsInvalidOrBlank() {
        assertFalse(authManager.isAuthorized(null))
        assertFalse(authManager.isAuthorized(""))
        assertFalse(authManager.isAuthorized("Bearer invalid_token"))
    }
}
