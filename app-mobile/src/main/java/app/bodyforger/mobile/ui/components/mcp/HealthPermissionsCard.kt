package app.bodyforger.mobile.ui.components.mcp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.bodyforger.mobile.R

@Composable
fun HealthPermissionsCard(
    isAvailable: Boolean,
    hasHistoryPermission: Boolean,
    missingCount: Int,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.mcp_health_permissions_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val availText = if (isAvailable) {
                stringResource(R.string.mcp_health_available)
            } else {
                stringResource(R.string.mcp_health_unavailable)
            }
            val availColor = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

            Text(
                text = availText,
                style = MaterialTheme.typography.bodyMedium,
                color = availColor
            )

            if (isAvailable) {
                Spacer(modifier = Modifier.height(8.dp))

                val historyColor = if (hasHistoryPermission) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
                val historyText = if (hasHistoryPermission) {
                    stringResource(R.string.mcp_health_history_granted)
                } else {
                    stringResource(R.string.mcp_health_history_missing)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = historyText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = historyColor,
                        modifier = Modifier
                            .background(
                                color = historyColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.mcp_health_request_permissions))
                }
            }
        }
    }
}
