package app.bodyforger.mobile.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.bodyforger.core.healthconnect.HealthConnectPermissions
import app.bodyforger.mobile.R
import app.bodyforger.mobile.mcp.McpViewModel
import app.bodyforger.mobile.ui.components.mcp.HealthInspectionCard
import app.bodyforger.mobile.ui.components.mcp.HealthPermissionsCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: McpViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) {
        viewModel.refreshHealthStatus()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_health_connect_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.setup_previous)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HealthPermissionsCard(
                isAvailable = uiState.isHealthConnectAvailable,
                hasHistoryPermission = uiState.hasHistoryPermission,
                missingCount = uiState.missingPermissions.size,
                onRequestPermissions = {
                    permissionLauncher.launch(HealthConnectPermissions.CORE_READ_PERMISSIONS)
                }
            )

            HealthInspectionCard(
                isScanning = uiState.isScanning,
                overview = uiState.inspectionOverview,
                errorMessage = uiState.scanError,
                onScan = { viewModel.scanHealthHistory(monthsBack = 12) }
            )
        }
    }
}
