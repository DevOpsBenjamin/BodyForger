package app.bodyforger.mobile.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.bodyforger.mobile.R
import app.bodyforger.mobile.mcp.McpUiState

/**
 * Settings section item for Model Context Protocol (MCP).
 *
 * Reflects whether the local MCP server daemon is running and on which port.
 * Tapping opens the dedicated McpScreen.
 */
@Composable
fun McpSettingsSection(
    uiState: McpUiState,
    onClick: () -> Unit
) {
    val status = if (uiState.isServerRunning) SectionStatus.DONE else SectionStatus.NEUTRAL
    val summary = if (uiState.isServerRunning) {
        stringResource(R.string.settings_mcp_status_enabled, uiState.serverPort)
    } else {
        stringResource(R.string.settings_mcp_status_disabled)
    }

    SettingsSection(
        title = stringResource(R.string.settings_mcp_title),
        status = status,
        summary = summary,
        isExpanded = false,
        onToggle = onClick
    ) {}
}
