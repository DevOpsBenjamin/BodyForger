package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.Routine
import app.bodyforger.mobile.R
import app.bodyforger.mobile.stats.TrainingStats
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

@Composable
fun HomeActionCards(
    onNavigateToWorkout: () -> Unit,
    onNavigateToBiometrics: () -> Unit,
    isScaleReady: Boolean,
    onWeighIn: () -> Unit,
    onConfigureScale: () -> Unit,
    /** The routine the planner assigned to today, or `null` on a rest day. */
    todaysRoutine: Routine? = null,
    /** The name the paired scale advertised, or `null` when none is paired. */
    scaleName: String? = null,
    modifier: Modifier = Modifier
) {
    // Both cards stand to the height of the taller: side by side, one shorter than the other
    // reads as an unfinished layout rather than as less content.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(1.dp, NeonLime.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                // Filling the height is what gives SpaceBetween something to spread: without
                // it the column wraps its content and the button floats wherever the text ends.
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonLime.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.home_today),
                            color = NeonLime,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = todaysRoutine?.name ?: stringResource(R.string.home_rest_day),
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = todaysRoutine?.let { routine ->
                            val minutes = TrainingStats.estimatedRoutineMinutes(routine)
                            if (minutes == null) {
                                stringResource(R.string.home_session_exercises, routine.exercises.size)
                            } else {
                                stringResource(
                                    R.string.home_session_exercises_and_duration,
                                    routine.exercises.size,
                                    minutes
                                )
                            }
                        } ?: stringResource(R.string.home_rest_day_detail),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )
                }

                Button(
                    onClick = onNavigateToWorkout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonLime,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.home_start),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                // Filling the height is what gives SpaceBetween something to spread: without
                // it the column wraps its content and the button floats wherever the text ends.
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.home_measure),
                            color = ElectricCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.home_bia_weigh_in),
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = scaleName ?: stringResource(R.string.home_scale_none),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )
                }

                // With no paired scale and no profile, weighing in is meaningless: the button
                // leads to where that is set up rather than failing once pressed.
                Button(
                    onClick = if (isScaleReady) onWeighIn else onConfigureScale,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isScaleReady) ElectricCyan else SurfaceBorder,
                        contentColor = if (isScaleReady) Color.Black else TextMuted
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(
                            if (isScaleReady) R.string.home_scale_measure
                            else R.string.home_scale_not_configured
                        ),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
