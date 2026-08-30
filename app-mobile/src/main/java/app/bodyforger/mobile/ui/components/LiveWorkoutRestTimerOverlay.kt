package app.bodyforger.mobile.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.R
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary

@Composable
fun LiveWorkoutRestTimerOverlay(
    isVisible: Boolean,
    secondsRemaining: Int,
    totalSeconds: Int,
    onAddSeconds: (Int) -> Unit,
    onSkipRest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.workout_live_rest_timer),
                            color = ElectricCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    val mins = secondsRemaining / 60
                    val secs = secondsRemaining % 60
                    Text(
                        text = String.format("%02d:%02d", mins, secs),
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val progress = if (totalSeconds > 0) (secondsRemaining.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f) else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ElectricCyan,
                    trackColor = SurfaceElevated
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Bouton -15s
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                                .clickable { onAddSeconds(-15) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = stringResource(R.string.workout_live_minus_15s), color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Bouton +30s
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                                .clickable { onAddSeconds(30) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = stringResource(R.string.workout_live_add_30s), color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Bouton PASSER
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan)
                            .clickable { onSkipRest() }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.workout_live_skip_rest),
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
