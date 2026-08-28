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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.Routine
import app.bodyforger.mobile.ui.components.AssignRoutineDialog
import app.bodyforger.mobile.ui.components.DeleteRoutineDialog
import app.bodyforger.mobile.ui.components.PlannerDayCard
import app.bodyforger.mobile.ui.components.PlannerRoutineCard
import app.bodyforger.mobile.ui.components.ShareRoutineJsonDialog
import app.bodyforger.mobile.ui.components.WeekDaySelector
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

@Composable
fun PlannerScreen(
    routines: List<Routine> = emptyList(),
    onStartWorkout: (routineId: String?) -> Unit,
    onCreateNewRoutine: () -> Unit = {},
    onEditRoutine: (Routine) -> Unit = {},
    onDuplicateRoutine: (Routine) -> Unit = {},
    onDeleteRoutine: (Routine) -> Unit = {},
    onToggleRoutineDay: (routineId: String, dayInt: Int) -> Unit = { _, _ -> },
    onOpenCatalog: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var selectedDayIndex by remember { mutableIntStateOf(1) } // 1 = Lundi par défaut
    var routineToShareJson by remember { mutableStateOf<Routine?>(null) }
    var routineToDelete by remember { mutableStateOf<Routine?>(null) }
    var showingAssignDialog by remember { mutableStateOf(false) }

    val daysOfWeek = listOf(
        1 to Pair("LUN", "Lundi"),
        2 to Pair("MAR", "Mardi"),
        3 to Pair("MER", "Mercredi"),
        4 to Pair("JEU", "Jeudi"),
        5 to Pair("VEN", "Vendredi"),
        6 to Pair("SAM", "Samedi"),
        7 to Pair("DIM", "Dimanche")
    )

    val currentDayRoutines = routines.filter { it.assignedDays.contains(selectedDayIndex) }
    val selectedDayPair = daysOfWeek.firstOrNull { it.first == selectedDayIndex }?.second ?: Pair("LUN", "Lundi")

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
                    text = "Planning Hebdo",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Bouton CATALOGUE D'EXERCICES
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                    .clickable { onOpenCatalog() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "Catalogue",
                    tint = NeonLime,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(text = "CATALOGUE", color = NeonLime, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                    Text(text = "D'EXERCICES", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 2. SÉLECTEUR DE SEMAINE ---
        WeekDaySelector(
            selectedDayIndex = selectedDayIndex,
            daysOfWeek = daysOfWeek,
            routines = routines,
            onDaySelected = { selectedDayIndex = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. CARTE DU JOUR SÉLECTIONNÉ ---
        PlannerDayCard(
            dayName = selectedDayPair.second,
            dayIndex = selectedDayIndex,
            dayRoutines = currentDayRoutines,
            onOpenAssignDialog = { showingAssignDialog = true },
            onRemoveRoutineFromDay = onToggleRoutineDay
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. LISTE DES ROUTINES ENREGISTRÉES ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MES ROUTINES (${routines.size})",
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
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = NeonLime, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "NOUVELLE", color = NeonLime, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (routines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Aucune routine créée", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Cliquez sur + NOUVELLE pour créer votre premier entraînement !", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            routines.forEach { routine ->
                PlannerRoutineCard(
                    routine = routine,
                    onStartWorkout = { onStartWorkout(it) },
                    onEditRoutine = onEditRoutine,
                    onDuplicateRoutine = onDuplicateRoutine,
                    onShareRoutineJson = { routineToShareJson = it },
                    onDeleteRoutine = { routineToDelete = it }
                )
            }
        }
    }

    // --- MODALES EXTRAITES ---
    if (showingAssignDialog) {
        AssignRoutineDialog(
            dayName = selectedDayPair.second,
            dayIndex = selectedDayIndex,
            routines = routines,
            onToggleRoutineDay = onToggleRoutineDay,
            onDismiss = { showingAssignDialog = false }
        )
    }

    routineToShareJson?.let { routine ->
        ShareRoutineJsonDialog(
            routine = routine,
            onDismiss = { routineToShareJson = null }
        )
    }

    routineToDelete?.let { routine ->
        DeleteRoutineDialog(
            routine = routine,
            onConfirmDelete = {
                onDeleteRoutine(routine)
                routineToDelete = null
            },
            onDismiss = { routineToDelete = null }
        )
    }
}
