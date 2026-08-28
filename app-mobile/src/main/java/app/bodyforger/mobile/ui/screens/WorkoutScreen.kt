package app.bodyforger.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.ui.components.LiveWorkoutRestTimerCard
import app.bodyforger.mobile.ui.components.LiveWorkoutSetsCard
import app.bodyforger.mobile.ui.components.LiveWorkoutTopBar
import app.bodyforger.mobile.ui.theme.CrimsonRed
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary
import kotlinx.coroutines.delay

data class LiveSetItem(
    val index: Int,
    var weightKg: Double,
    var reps: Int,
    val type: String = "WORK",
    var isDone: Boolean = false
)

@Composable
fun WorkoutScreen(
    onMinimize: () -> Unit = {},
    onFinishWorkout: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    var sessionSeconds by remember { mutableIntStateOf(38 * 60 + 12) }
    var currentHeartRate by remember { mutableIntStateOf(145) }
    var isResting by remember { mutableStateOf(false) }
    var restSecondsRemaining by remember { mutableIntStateOf(60) }

    val sets = remember {
        mutableStateListOf(
            LiveSetItem(1, 85.0, 10, "WORK", isDone = true),
            LiveSetItem(2, 90.0, 8, "WORK", isDone = true),
            LiveSetItem(3, 92.5, 6, "WORK", isDone = false),
            LiveSetItem(4, 75.0, 8, "DROPSET", isDone = false)
        )
    }

    LaunchedEffect(isResting) {
        if (isResting) {
            while (restSecondsRemaining > 0) {
                delay(1000)
                restSecondsRemaining--
            }
            isResting = false
            restSecondsRemaining = 60
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // 1. Barre supérieure
        LiveWorkoutTopBar(
            currentHeartRate = currentHeartRate,
            sessionSeconds = sessionSeconds,
            onMinimize = onMinimize
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Titre de l'exercice actif
        Text(
            text = "EXERCICE 1 / 5",
            color = NeonLime,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Text(
            text = "BARBELL BENCH PRESS",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
        )

        Text(
            text = "Pectoraux • Barre Olympique • RPE Cible 8.5",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Carte des séries
        LiveWorkoutSetsCard(
            sets = sets,
            onToggleSetDone = { idx ->
                val current = sets[idx]
                val newDone = !current.isDone
                sets[idx] = current.copy(isDone = newDone)
                if (newDone) {
                    isResting = true
                    restSecondsRemaining = 60
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Chrono de repos
        LiveWorkoutRestTimerCard(
            isVisible = isResting,
            restSecondsRemaining = restSecondsRemaining
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Boutons de validation et fin de séance
        Button(
            onClick = {
                val nextIncomplete = sets.indexOfFirst { !it.isDone }
                if (nextIncomplete != -1) {
                    sets[nextIncomplete] = sets[nextIncomplete].copy(isDone = true)
                    isResting = true
                    restSecondsRemaining = 60
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonLime,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "VALIDER LA SÉRIE",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onFinishWorkout,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = CrimsonRed
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "TERMINER LA SÉANCE",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
