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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

data class DayPlanItem(
    val dayLabel: String,
    val dayFull: String,
    val routineName: String,
    val isRest: Boolean = false,
    val exerciseCount: Int = 0,
    val durationMinutes: Int = 0
)

data class RoutineItem(
    val id: String,
    val name: String,
    val muscles: List<String>,
    val exerciseCount: Int,
    val durationMinutes: Int,
    val exercisesPreview: String
)

@Composable
fun PlannerScreen(
    onStartWorkout: (routineId: String?) -> Unit,
    onCreateNewRoutine: () -> Unit = {},
    onOpenCatalog: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var selectedDayIndex by remember { mutableIntStateOf(4) } // Vendredi par défaut

    val weekDays = remember {
        listOf(
            DayPlanItem("LUN", "Lundi", "Push Hypertrophie", isRest = false, exerciseCount = 5, durationMinutes = 50),
            DayPlanItem("MAR", "Mardi", "Pull Dos & Bras", isRest = false, exerciseCount = 6, durationMinutes = 55),
            DayPlanItem("MER", "Mercredi", "Repos & Mobilité", isRest = true),
            DayPlanItem("JEU", "Jeudi", "Legs & Abdos", isRest = false, exerciseCount = 5, durationMinutes = 50),
            DayPlanItem("VEN", "Vendredi", "Upper Body Power", isRest = false, exerciseCount = 5, durationMinutes = 50),
            DayPlanItem("SAM", "Samedi", "Repos & BIA", isRest = true),
            DayPlanItem("DIM", "Dimanche", "Repos & Récupération", isRest = true)
        )
    }

    val myRoutines = remember {
        listOf(
            RoutineItem(
                id = "r_001",
                name = "Push Hypertrophie",
                muscles = listOf("Pectoraux", "Épaules", "Triceps"),
                exerciseCount = 5,
                durationMinutes = 50,
                exercisesPreview = "Bench Press, Incline DB Press, Dips, Lateral Raises, Triceps"
            ),
            RoutineItem(
                id = "r_002",
                name = "Pull Dos & Bras",
                muscles = listOf("Grand Dorsal", "Trapèzes", "Biceps"),
                exerciseCount = 6,
                durationMinutes = 55,
                exercisesPreview = "Deadlift, Lat Pulldown, Seated Cable Row, Face Pulls, Curls"
            ),
            RoutineItem(
                id = "r_003",
                name = "Legs & Abdos",
                muscles = listOf("Quadriceps", "Ischios", "Mollets"),
                exerciseCount = 5,
                durationMinutes = 50,
                exercisesPreview = "Back Squat, Romanian Deadlift, Leg Extension, Calves, Core"
            ),
            RoutineItem(
                id = "r_004",
                name = "Upper Body Power",
                muscles = listOf("Torse Complet", "Bras"),
                exerciseCount = 5,
                durationMinutes = 50,
                exercisesPreview = "Incline Bench, Pull-ups, Overhead Press, DB Flyes, Skull Crushers"
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
        // --- 1. EN-TÊTE : Titre + Bouton Catalogue d'Exercices ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PROGRAMMES & SÉANCES",
                    color = NeonLime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "MON PLANNING",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Bouton CATALOGUE avec icône Livre
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, NeonLime.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { onOpenCatalog() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "Catalogue d'exercices",
                    tint = NeonLime,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CATALOGUE",
                    color = NeonLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. BOUTON SÉANCE VIDE (FREESTYLE) ---
        OutlinedButton(
            onClick = { onStartWorkout(null) },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextPrimary
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = NeonLime,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "DÉMARRER UNE SÉANCE VIDE (FREESTYLE)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 3. PLANNING HEBDOMADAIRE (7 CASES ALIGNÉES) ---
        Text(
            text = "PLANNING DE LA SEMAINE",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Rangée compacte des 7 jours
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            weekDays.forEachIndexed { index, day ->
                val isSelected = selectedDayIndex == index
                val isToday = index == 4 // Vendredi

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) SurfaceElevated else SurfaceDark)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) NeonLime else if (isToday) NeonLime.copy(alpha = 0.4f) else SurfaceBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedDayIndex = index }
                        .padding(vertical = 8.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.dayLabel,
                        color = if (isSelected) NeonLime else if (isToday) TextPrimary else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (day.isRest) Color(0xFF55555C) else ElectricCyan
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Carte détail du jour sélectionné
        val selectedDay = weekDays[selectedDayIndex]
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${selectedDay.dayFull} • ${if (selectedDay.isRest) "Repos" else "Séance Planifiée"}",
                        color = if (selectedDay.isRest) TextMuted else ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedDay.routineName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    if (!selectedDay.isRest) {
                        Text(
                            text = "${selectedDay.exerciseCount} exercices • ~${selectedDay.durationMinutes} min",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                if (!selectedDay.isRest) {
                    Button(
                        onClick = { onStartWorkout(null) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonLime,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LANCER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. LISTE DES ROUTINES ENREGISTRÉES ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MES ROUTINES (${myRoutines.size})",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonLime.copy(alpha = 0.15f))
                    .clickable { onCreateNewRoutine() }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = NeonLime,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "NOUVELLE",
                    color = NeonLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cartes de Routines
        myRoutines.forEach { routine ->
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = routine.name,
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Badges des muscles ciblés
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                routine.muscles.forEach { muscle ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SurfaceElevated)
                                            .border(1.dp, SurfaceBorder, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = muscle,
                                            color = TextSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = routine.exercisesPreview,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${routine.exerciseCount} exercices • ~${routine.durationMinutes} min",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Button(
                            onClick = { onStartWorkout(routine.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonLime,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DÉMARRER",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
