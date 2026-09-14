package app.bodyforger.mobile.mcp

import android.content.SharedPreferences
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class McpPreferencesTest {

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

    private lateinit var preferences: McpPreferences
    private lateinit var fakePrefs: FakeSharedPreferences

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
        preferences = McpPreferences(fakePrefs)
    }

    @Test
    fun isEnabled_defaultsToTrue() {
        assertTrue(preferences.isEnabled)
    }

    @Test
    fun isEnabled_canBeToggled() {
        preferences.isEnabled = false
        assertFalse(preferences.isEnabled)

        preferences.isEnabled = true
        assertTrue(preferences.isEnabled)
    }
}
