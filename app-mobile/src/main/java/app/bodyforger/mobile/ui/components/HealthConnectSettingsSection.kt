package app.bodyforger.mobile.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.bodyforger.mobile.R
import app.bodyforger.mobile.mcp.McpUiState

/**
 * Settings section item for Google Health Connect and embedded MCP server.
 *
 * Reflects whether Health Connect permissions are granted or need attention.
 * Tapping opens the detailed HealthConnectMcpScreen.
 */
@Composable
fun HealthConnectSettingsSection(
    uiState: McpUiState,
    onClick: () -> Unit
) {
    val status = when {
        !uiState.isHealthConnectAvailable -> SectionStatus.NEUTRAL
        uiState.grantedPermissions.isNotEmpty() && uiState.missingPermissions.isEmpty() -> SectionStatus.DONE
        else -> SectionStatus.INCOMPLETE
    }
    val summary = when {
        !uiState.isHealthConnectAvailable -> stringResource(R.string.mcp_health_unavailable)
        uiState.grantedPermissions.isNotEmpty() && uiState.missingPermissions.isEmpty() ->
            stringResource(R.string.settings_health_connect_connected)
        else -> stringResource(R.string.settings_health_connect_not_connected)
    }

    SettingsSection(
        title = stringResource(R.string.settings_health_connect_mcp_title),
        status = status,
        summary = summary,
        isExpanded = false,
        onToggle = onClick
    ) {}
}
