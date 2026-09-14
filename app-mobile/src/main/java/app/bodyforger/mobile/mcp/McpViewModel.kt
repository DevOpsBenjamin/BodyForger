package app.bodyforger.mobile.mcp

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bodyforger.core.healthconnect.HealthConnectManager
import app.bodyforger.core.healthconnect.HealthConnectPermissions
import app.bodyforger.core.healthconnect.HealthConnectReader
import app.bodyforger.core.healthconnect.HealthInspectionOverview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class McpUiState(
    val isServerRunning: Boolean = false,
    val serverPort: Int = McpHttpServer.DEFAULT_PORT,
    val pairingCode: String = "",
    val pairingRemainingSeconds: Int = McpAuthManager.CODE_LIFETIME_SECONDS,
    val pairedDevices: List<McpPairedDevice> = emptyList(),
    val isHealthConnectAvailable: Boolean = false,
    val hasHistoryPermission: Boolean = false,
    val grantedPermissions: Set<String> = emptySet(),
    val missingPermissions: Set<String> = emptySet(),
    val isScanning: Boolean = false,
    val inspectionOverview: HealthInspectionOverview? = null,
    val scanError: String? = null
)

class McpViewModel(
    private val application: Application,
    private val mcpServer: McpHttpServer,
    private val healthConnectManager: HealthConnectManager,
    private val mcpPreferences: McpPreferences,
    private val mcpAuthManager: McpAuthManager? = null
) : ViewModel() {

    private val healthConnectReader get() = healthConnectManager.getReaderOrNull()

    private val _uiState = MutableStateFlow(McpUiState())
    val uiState: StateFlow<McpUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            mcpServer.isRunning.collect { running ->
                _uiState.update { it.copy(isServerRunning = running) }
            }
        }
        viewModelScope.launch {
            mcpServer.serverPort.collect { port ->
                _uiState.update { it.copy(serverPort = port) }
            }
        }
        if (mcpAuthManager != null) {
            viewModelScope.launch {
                mcpAuthManager.pairingCode.collect { code ->
                    _uiState.update { it.copy(pairingCode = code) }
                }
            }
            viewModelScope.launch {
                mcpAuthManager.remainingSeconds.collect { secs ->
                    _uiState.update { it.copy(pairingRemainingSeconds = secs) }
                }
            }
            viewModelScope.launch {
                mcpAuthManager.pairedDevices.collect { devices ->
                    _uiState.update { it.copy(pairedDevices = devices) }
                }
            }
        }
        refreshHealthStatus()
    }

    fun setServerEnabled(enabled: Boolean) {
        mcpPreferences.isEnabled = enabled
        if (enabled) {
            McpService.start(application)
        } else {
            McpService.stop(application)
            mcpServer.stop()
        }
    }

    fun toggleServer() {
        setServerEnabled(!_uiState.value.isServerRunning)
    }

    fun refreshPairingCode() {
        mcpAuthManager?.refreshPairingCode()
    }

    fun revokeDevice(deviceId: String) {
        mcpAuthManager?.revokeDevice(deviceId)
    }

    fun revokeAllDevices() {
        mcpAuthManager?.revokeAll()
    }

    fun refreshHealthStatus() {
        val isAvailable = healthConnectManager.isAvailable()
        _uiState.update { it.copy(isHealthConnectAvailable = isAvailable) }
        if (!isAvailable) return

        val client = healthConnectManager.getClientOrNull() ?: return
        viewModelScope.launch {
            try {
                val granted = HealthConnectPermissions.getGrantedPermissions(client)
                val missing = HealthConnectPermissions.CORE_READ_PERMISSIONS.filterNot { granted.contains(it) }.toSet()
                val hasHistory = HealthConnectPermissions.hasHistoryPermission(granted)
                _uiState.update {
                    it.copy(
                        hasHistoryPermission = hasHistory,
                        grantedPermissions = granted,
                        missingPermissions = missing
                    )
                }
            } catch (_: Exception) {
            }
        }
    }

    fun scanHealthHistory(monthsBack: Int = 12) {
        val reader = healthConnectReader ?: return
        _uiState.update { it.copy(isScanning = true, scanError = null) }
        viewModelScope.launch {
            try {
                val overview = reader.inspectOverview(monthsBack = monthsBack)
                _uiState.update { it.copy(isScanning = false, inspectionOverview = overview) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false, scanError = e.message ?: "Scan failed") }
            }
        }
    }
}
