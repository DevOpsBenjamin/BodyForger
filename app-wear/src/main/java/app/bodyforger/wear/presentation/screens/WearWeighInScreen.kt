package app.bodyforger.wear.presentation.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import app.bodyforger.wear.R
import app.bodyforger.wear.presentation.theme.ElectricCyan
import app.bodyforger.wear.presentation.theme.NeonLime
import app.bodyforger.wear.presentation.theme.Obsidian
import kotlinx.coroutines.delay

enum class WearWeighInStep {
    SCANNING,
    AUTHENTICATING,
    STEP_ON,
    MEASURING,
    COMPLETED
}

@Composable
fun WearWeighInScreen(
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(WearWeighInStep.SCANNING) }

    // Simulation progressive du flux de pesée BLE
    LaunchedEffect(step) {
        when (step) {
            WearWeighInStep.SCANNING -> {
                delay(2000)
                step = WearWeighInStep.AUTHENTICATING
            }
            WearWeighInStep.AUTHENTICATING -> {
                delay(1500)
                step = WearWeighInStep.STEP_ON
            }
            WearWeighInStep.STEP_ON -> {
                delay(2500)
                step = WearWeighInStep.MEASURING
            }
            WearWeighInStep.MEASURING -> {
                delay(3000)
                step = WearWeighInStep.COMPLETED
            }
            WearWeighInStep.COMPLETED -> {
                // Reste sur l'écran des résultats
            }
        }
    }

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
            when (step) {
                WearWeighInStep.SCANNING -> {
                    Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            indicatorColor = ElectricCyan,
                            trackColor = Color(0xFF1A1A20),
                            strokeWidth = 4.dp
                        )
                        Text(text = "📡", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.weigh_in_state_scanning),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                WearWeighInStep.AUTHENTICATING -> {
                    Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            indicatorColor = NeonLime,
                            trackColor = Color(0xFF1A1A20),
                            strokeWidth = 4.dp
                        )
                        Text(text = "🔐", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.weigh_in_state_authenticating),
                        color = NeonLime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                WearWeighInStep.STEP_ON -> {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2024)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⚖️", fontSize = 26.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.weigh_in_state_step_on),
                        color = NeonLime,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }

                WearWeighInStep.MEASURING -> {
                    Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            indicatorColor = ElectricCyan,
                            trackColor = Color(0xFF1A1A20),
                            strokeWidth = 4.dp
                        )
                        Text(text = "🧬", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.weigh_in_state_measuring),
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                WearWeighInStep.COMPLETED -> {
                    Text(
                        text = "82.4 kg",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "FFM 68.2 kg", color = NeonLime, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Gras 17.2%", color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = NeonLime,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_back_to_menu),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
