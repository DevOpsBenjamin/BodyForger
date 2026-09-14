package app.bodyforger.mobile.mcp

import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import java.util.UUID

class McpAuthManager(
    private val preferences: SharedPreferences
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val secureRandom = SecureRandom()

    private val _pairingCode = MutableStateFlow(generateRandomCode())
    val pairingCode: StateFlow<String> = _pairingCode.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(CODE_LIFETIME_SECONDS)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _pairedDevices = MutableStateFlow(loadPairedDevices())
    val pairedDevices: StateFlow<List<McpPairedDevice>> = _pairedDevices.asStateFlow()

    init {
        scope.launch {
            while (isActive) {
                delay(1000L)
                val nextSeconds = _remainingSeconds.value - 1
                if (nextSeconds <= 0) {
                    _pairingCode.value = generateRandomCode()
                    _remainingSeconds.value = CODE_LIFETIME_SECONDS
                } else {
                    _remainingSeconds.value = nextSeconds
                }
            }
        }
    }

    fun refreshPairingCode() {
        _pairingCode.value = generateRandomCode()
        _remainingSeconds.value = CODE_LIFETIME_SECONDS
    }

    fun validateAndPair(code: String, clientName: String?): McpPairedDevice? {
        val cleanCode = code.trim().replace(" ", "")
        if (cleanCode != _pairingCode.value) return null

        val token = TOKEN_PREFIX + UUID.randomUUID().toString().replace("-", "")
        val device = McpPairedDevice(
            id = UUID.randomUUID().toString(),
            name = clientName?.trim()?.takeIf { it.isNotEmpty() } ?: DEFAULT_CLIENT_NAME,
            token = token,
            createdAtEpochMs = System.currentTimeMillis()
        )

        val updated = _pairedDevices.value + device
        _pairedDevices.value = updated
        persistPairedDevices(updated)

        refreshPairingCode()
        return device
    }

    fun isAuthorized(token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        val cleanToken = token.trim().removePrefix(BEARER_PREFIX).trim()
        return _pairedDevices.value.any { it.token == cleanToken }
    }

    fun revokeDevice(deviceId: String): Boolean {
        val current = _pairedDevices.value
        val updated = current.filterNot { it.id == deviceId }
        return if (updated.size != current.size) {
            _pairedDevices.value = updated
            persistPairedDevices(updated)
            true
        } else {
            false
        }
    }

    fun revokeAll() {
        _pairedDevices.value = emptyList()
        persistPairedDevices(emptyList())
    }

    private fun generateRandomCode(): String {
        val num = 100_000 + secureRandom.nextInt(900_000)
        return num.toString()
    }

    private fun loadPairedDevices(): List<McpPairedDevice> {
        val raw = preferences.getString(KEY_PAIRED_DEVICES, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { idx ->
                val obj = array.getJSONObject(idx)
                McpPairedDevice(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    token = obj.getString("token"),
                    createdAtEpochMs = obj.optLong("createdAtEpochMs", 0L)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun persistPairedDevices(devices: List<McpPairedDevice>) {
        val array = JSONArray()
        devices.forEach { device ->
            array.put(JSONObject().apply {
                put("id", device.id)
                put("name", device.name)
                put("token", device.token)
                put("createdAtEpochMs", device.createdAtEpochMs)
            })
        }
        preferences.edit().putString(KEY_PAIRED_DEVICES, array.toString()).apply()
    }

    companion object {
        const val CODE_LIFETIME_SECONDS = 30
        const val KEY_PAIRED_DEVICES = "mcp_paired_devices_json"
        const val DEFAULT_CLIENT_NAME = "Unknown Client"
        const val TOKEN_PREFIX = "bf_sec_"
        const val BEARER_PREFIX = "Bearer "
    }
}
