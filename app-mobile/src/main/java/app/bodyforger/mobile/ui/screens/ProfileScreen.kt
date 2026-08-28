package app.bodyforger.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.ui.components.ActivityHeatmapCard
import app.bodyforger.mobile.ui.components.HistoryWorkoutCard
import app.bodyforger.mobile.ui.components.ProfileTotalStatCard
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

data class HistoryWorkoutItem(
    val id: String,
    val title: String,
    val dateDisplay: String,
    val durationDisplay: String,
    val avgBpm: Int,
    val totalTonnageKg: Double,
    val exerciseSummary: String,
    val personalRecordHighlight: String? = null
)

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    val workoutHistory = remember {
        listOf(
            HistoryWorkoutItem(
                id = "h_001",
                title = "Push Hypertrophie",
                dateDisplay = "Hier • 17:45",
                durationDisplay = "52 min",
                avgBpm = 142,
                totalTonnageKg = 6850.0,
                exerciseSummary = "Bench Press, Incline DB, Dips, Lateral Raises, Triceps",
                personalRecordHighlight = "Nouveau record : Bench 90.0 kg × 8 reps"
            ),
            HistoryWorkoutItem(
                id = "h_002",
                title = "Pull Dos & Biceps",
                dateDisplay = "Mercredi 26 Août • 18:10",
                durationDisplay = "58 min",
                avgBpm = 138,
                totalTonnageKg = 7420.0,
                exerciseSummary = "Deadlift, Lat Pulldown, Cable Row, Face Pulls, Curls",
                personalRecordHighlight = "Nouveau record : Deadlift 150.0 kg × 5 reps"
            ),
            HistoryWorkoutItem(
                id = "h_003",
                title = "Legs & Abdos Power",
                dateDisplay = "Lundi 24 Août • 12:30",
                durationDisplay = "48 min",
                avgBpm = 149,
                totalTonnageKg = 8100.0,
                exerciseSummary = "Back Squat, Romanian Deadlift, Leg Extension, Calves",
                personalRecordHighlight = null
            ),
            HistoryWorkoutItem(
                id = "h_004",
                title = "Upper Body Heavy",
                dateDisplay = "Vendredi 21 Août • 17:30",
                durationDisplay = "55 min",
                avgBpm = 140,
                totalTonnageKg = 7200.0,
                exerciseSummary = "Incline Bench, Pull-ups, OHP, DB Flyes, Skull Crushers",
                personalRecordHighlight = null
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // --- 1. EN-TÊTE PROFIL ATHLÈTE ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                        .border(1.5.dp, NeonLime, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = NeonLime,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "BENJAMIN D.",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "🔥 14 semaines consécutives",
                            color = AmberGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Paramètres",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- 2. CHIFFRES CLÉS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileTotalStatCard(modifier = Modifier.weight(1f), label = "Séances", value = "48", color = NeonLime)
            ProfileTotalStatCard(modifier = Modifier.weight(1f), label = "Tonnage Total", value = "124 T", color = ElectricCyan)
            ProfileTotalStatCard(modifier = Modifier.weight(1f), label = "Heures Gym", value = "44 h", color = AmberGold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 3. HEATMAP D'ACTIVITÉ ---
        ActivityHeatmapCard()

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. HISTORIQUE DES SÉANCES ---
        Text(
            text = "HISTORIQUE DES ACTIVITÉS",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        workoutHistory.forEach { item ->
            HistoryWorkoutCard(item = item)
        }
    }
}
