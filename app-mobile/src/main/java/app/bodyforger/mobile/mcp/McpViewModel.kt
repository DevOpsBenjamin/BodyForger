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
    private val healthConnectManager: HealthConnectManager
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
        refreshHealthStatus()
    }

    fun toggleServer() {
        if (_uiState.value.isServerRunning) {
            McpService.stop(application)
            mcpServer.stop()
        } else {
            McpService.start(application)
        }
    }

    fun refreshHealthStatus() {
        val isAvailable = healthConnectManager.isAvailable()
        _uiState.update { it.copy(isHealthConnectAvailable = isAvailable) }

        val reader = healthConnectReader
        if (!isAvailable || reader == null) return

        viewModelScope.launch {
            try {
                val overview = reader.inspectOverview(monthsBack = 1)
                val granted = overview.grantedPermissions.toSet()
                val missing = overview.missingPermissions.toSet()
                _uiState.update {
                    it.copy(
                        grantedPermissions = granted,
                        missingPermissions = missing,
                        hasHistoryPermission = overview.hasHistoryPermission
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(scanError = e.message) }
            }
        }
    }

    fun scanHealthHistory(monthsBack: Int = 12) {
        val reader = healthConnectReader ?: return
        _uiState.update { it.copy(isScanning = true, scanError = null) }

        viewModelScope.launch {
            try {
                val overview = reader.inspectOverview(monthsBack = monthsBack)
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        inspectionOverview = overview,
                        hasHistoryPermission = overview.hasHistoryPermission,
                        grantedPermissions = overview.grantedPermissions.toSet(),
                        missingPermissions = overview.missingPermissions.toSet()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isScanning = false, scanError = e.message)
                }
            }
        }
    }
}
