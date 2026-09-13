package app.bodyforger.wear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.healthconnect.HealthConnectManager
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import app.bodyforger.wear.R
import app.bodyforger.wear.presentation.theme.ElectricCyan
import app.bodyforger.wear.presentation.theme.NeonLime
import app.bodyforger.wear.presentation.theme.Obsidian

/**
 * Probe: does a Health Connect provider exist on the watch itself?
 *
 * The documentation contradicts itself on this — the answer decides whether the watch can
 * export on its own or has to hand its sessions to the phone. Read it off a real device,
 * then delete this.
 */
private fun healthConnectStatus(context: android.content.Context): String =
    "HC: " + HealthConnectManager(context).availability().name

@Composable
fun WearHomeScreen(
    onStartWorkout: () -> Unit,
    onStartWeighIn: () -> Unit
) {
    val probe = healthConnectStatus(LocalContext.current)
    Scaffold(
        timeText = { TimeText() },
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.tile_header),
                color = ElectricCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            // Probe, not a feature: remove once the answer is written down.
            Text(
                text = probe,
                color = NeonLime,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onStartWorkout,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = NeonLime,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(42.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🏋️ ",
                        fontSize = 12.sp
                    )
                    Text(
                        text = stringResource(R.string.action_start_workout),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onStartWeighIn,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF1E2024),
                    contentColor = ElectricCyan
                ),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(36.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "⚖️ ",
                        fontSize = 11.sp
                    )
                    Text(
                        text = stringResource(R.string.action_start_weigh_in),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
            }
        }
    }
}
