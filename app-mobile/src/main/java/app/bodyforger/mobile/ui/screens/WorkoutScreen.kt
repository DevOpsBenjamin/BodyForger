package app.bodyforger.mobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.CrimsonRed
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary
import kotlinx.coroutines.delay

data class LiveSetItem(
    val index: Int,
    var weightKg: Double,
    var reps: Int,
    val type: String = "WORK", // WORK, DROPSET, RESTPAUSE
    var isDone: Boolean = false
)

@Composable
fun WorkoutScreen(
    onMinimize: () -> Unit = {},
    onFinishWorkout: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    // États de la séance
    var sessionSeconds by remember { mutableIntStateOf(38 * 60 + 12) }
    var currentHeartRate by remember { mutableIntStateOf(145) }
    var isResting by remember { mutableStateOf(false) }
    var restSecondsRemaining by remember { mutableIntStateOf(60) }

    // Liste des séries
    val sets = remember {
        mutableStateListOf(
            LiveSetItem(1, 85.0, 10, "WORK", isDone = true),
            LiveSetItem(2, 90.0, 8, "WORK", isDone = true),
            LiveSetItem(3, 92.5, 6, "WORK", isDone = false),
            LiveSetItem(4, 75.0, 8, "DROPSET", isDone = false)
        )
    }

    // Chrono de repos dynamique
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
        // Barre supérieure avec Réduire + Cardio en direct & Durée
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, CircleShape)
                    .clickable { onMinimize() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "▼", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$currentHeartRate BPM",
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val minutes = sessionSeconds / 60
                val seconds = sessionSeconds % 60
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Titre de l'exercice actif
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

        // Liste des Séries de l'exercice
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp, start = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "SÉRIE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "CHARGE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "REPS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "STATUT", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                sets.forEachIndexed { idx, set ->
                    val isCurrent = !set.isDone && (idx == 0 || sets[idx - 1].isDone)
                    val bgColor = if (isCurrent) SurfaceElevated else Color.Transparent
                    val borderColor = if (isCurrent) NeonLime.copy(alpha = 0.5f) else Color.Transparent

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable {
                                set.isDone = !set.isDone
                                if (set.isDone) {
                                    isResting = true
                                    restSecondsRemaining = 60
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SET ${set.index}",
                                color = if (set.isDone) NeonLime else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (set.type == "DROPSET") {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AmberGold.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "DROP", color = AmberGold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Text(
                            text = "${set.weightKg} kg",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )

                        Text(
                            text = "${set.reps} reps",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (set.isDone) NeonLime else SurfaceElevated)
                                .border(1.dp, if (set.isDone) NeonLime else SurfaceBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (set.isDone) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Fait",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (idx < sets.size - 1) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Barre de Rest Timer (affichée quand actif)
        AnimatedVisibility(visible = isResting) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CHRONO DE REPOS",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${restSecondsRemaining}s restant",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { restSecondsRemaining / 60f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ElectricCyan,
                        trackColor = SurfaceElevated
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bouton d'action principal
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
            onClick = { onFinishWorkout() },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = CrimsonRed
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(CrimsonRed.copy(alpha = 0.4f))
            ),
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
