package app.bodyforger.mobile.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
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
        // --- 1. EN-TÊTE PROFIL ATHLÈTE & BOUTON SETTINGS ---
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

        // --- 2. CHIFFRES CLÉS (STATS TOTALES) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TotalStatCard(modifier = Modifier.weight(1f), label = "Séances", value = "48", color = NeonLime)
            TotalStatCard(modifier = Modifier.weight(1f), label = "Tonnage Total", value = "124 T", color = ElectricCyan)
            TotalStatCard(modifier = Modifier.weight(1f), label = "Heures Gym", value = "44 h", color = AmberGold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 3. HEATMAP D'ACTIVITÉ (Style GitHub / Hevy) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RÉGULARITÉ & ENTRAÎNEMENT",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "4 séances cette semaine",
                        color = NeonLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Grille de carrés d'activité (12 semaines passées)
                ActivityHeatmapGrid()

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Moins", color = TextMuted, fontSize = 9.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(SurfaceElevated))
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(NeonLime.copy(alpha = 0.4f)))
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(NeonLime))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Plus", color = TextMuted, fontSize = 9.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. HISTORIQUE DES SÉANCES (Workout Feed) ---
        Text(
            text = "HISTORIQUE DES ACTIVITÉS",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        workoutHistory.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = item.title,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.dateDisplay,
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${item.avgBpm} bpm", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = item.exerciseSummary,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )

                    // Badge Record Personnel si battu pendant la séance
                    if (item.personalRecordHighlight != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AmberGold.copy(alpha = 0.15f))
                                .border(1.dp, AmberGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.personalRecordHighlight,
                                color = AmberGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = item.durationDisplay, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${(item.totalTonnageKg / 1000.0 * 10).toInt() / 10.0} T", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Voir détail",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TotalStatCard(modifier: Modifier = Modifier, label: String, value: String, color: Color) {
    Card(
        modifier = modifier.border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}

/**
 * Grille de régularité GitHub / Hevy style (14 colonnes de 7 jours)
 */
@Composable
fun ActivityHeatmapGrid() {
    val activeDays = remember {
        setOf(
            1, 3, 5, 8, 10, 12, 15, 17, 19, 22, 24, 26, 29, 31, 33, 36, 38, 40,
            43, 45, 47, 50, 52, 54, 57, 59, 61, 64, 66, 68, 71, 73, 75, 78, 80, 82
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (col in 0 until 14) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0 until 5) {
                    val dayIndex = col * 5 + row
                    val isActive = activeDays.contains(dayIndex)
                    val isHeavy = dayIndex % 3 == 0

                    val boxColor = when {
                        !isActive -> SurfaceElevated
                        isHeavy -> NeonLime
                        else -> NeonLime.copy(alpha = 0.5f)
                    }

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(boxColor)
                    )
                }
            }
        }
    }
}
