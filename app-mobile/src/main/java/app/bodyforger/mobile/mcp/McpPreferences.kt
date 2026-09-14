package app.bodyforger.mobile.mcp

import android.content.Context
import android.content.SharedPreferences

class McpPreferences(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, DEFAULT_ENABLED)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    companion object {
        const val PREFS_NAME = "mcp_preferences"
        const val KEY_ENABLED = "mcp_enabled"
        const val DEFAULT_ENABLED = true
    }
}
