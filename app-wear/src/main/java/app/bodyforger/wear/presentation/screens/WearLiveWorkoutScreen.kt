package app.bodyforger.wear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import app.bodyforger.wear.R
import app.bodyforger.wear.presentation.theme.ElectricCyan
import app.bodyforger.wear.presentation.theme.NeonLime
import app.bodyforger.wear.presentation.theme.Obsidian
import kotlinx.coroutines.delay

@Composable
fun WearLiveWorkoutScreen(
    onFinishWorkout: () -> Unit
) {
    var currentSet by remember { mutableIntStateOf(1) }
    val totalSets = 4
    var currentWeight by remember { mutableStateOf("90.0") }
    var currentReps by remember { mutableIntStateOf(8) }
    var heartRate by remember { mutableIntStateOf(148) }
    var isResting by remember { mutableStateOf(false) }
    var restSecondsRemaining by remember { mutableIntStateOf(45) }

    LaunchedEffect(isResting) {
        if (isResting) {
            while (restSecondsRemaining > 0) {
                delay(1000)
                restSecondsRemaining--
            }
            isResting = false
            restSecondsRemaining = 45
            if (currentSet < totalSets) {
                currentSet++
            }
        }
    }

    Scaffold(
        timeText = { TimeText() },
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isResting) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.label_rest),
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier.size(96.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = restSecondsRemaining / 45f,
                            modifier = Modifier.fillMaxSize(),
                            indicatorColor = ElectricCyan,
                            trackColor = Color(0xFF1E1E1E),
                            strokeWidth = 5.dp
                        )

                        Text(
                            text = String.format("00:%02d", restSecondsRemaining),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            isResting = false
                            restSecondsRemaining = 45
                            if (currentSet < totalSets) currentSet++
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = NeonLime,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_skip_rest),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Badge Cardio BPM
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF111116))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "♥",
                            color = NeonLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$heartRate BPM",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "BENCH PRESS",
                        color = NeonLime,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "$currentWeight kg × $currentReps",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "${stringResource(R.string.label_set)} $currentSet / $totalSets",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (currentSet >= totalSets) {
                                    onFinishWorkout()
                                } else {
                                    isResting = true
                                    restSecondsRemaining = 45
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = NeonLime,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Text(
                                text = if (currentSet >= totalSets) stringResource(R.string.btn_finish_workout) else stringResource(R.string.btn_validate_set),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }

                        CompactButton(
                            onClick = onFinishWorkout,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF222428),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Text(text = "✕", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
