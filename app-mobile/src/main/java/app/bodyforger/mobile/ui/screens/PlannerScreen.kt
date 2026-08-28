package app.bodyforger.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import app.bodyforger.core.model.Routine
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlannerScreen(
    routines: List<Routine> = emptyList(),
    onStartWorkout: (routineId: String?) -> Unit,
    onCreateNewRoutine: () -> Unit = {},
    onEditRoutine: (Routine) -> Unit = {},
    onDuplicateRoutine: (Routine) -> Unit = {},
    onDeleteRoutine: (Routine) -> Unit = {},
    onOpenCatalog: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var selectedDayIndex by remember { mutableIntStateOf(1) } // 1 = Lundi par défaut
    var routineToShareJson by remember { mutableStateOf<Routine?>(null) }
    var routineToDelete by remember { mutableStateOf<Routine?>(null) }

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

            // Bouton CATALOGUE D'EXERCICES (sur 2 lignes avec icône livre ouvert)
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
                    Text(
                        text = "CATALOGUE",
                        color = NeonLime,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "D'EXERCICES",
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 2. SELECTEUR DE SEMAINE (LUN - DIM) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { (dayInt, dayLabels) ->
                val (shortLabel, _) = dayLabels
                val isSelected = selectedDayIndex == dayInt
                val hasRoutines = routines.any { it.assignedDays.contains(dayInt) }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { selectedDayIndex = dayInt }
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isSelected -> NeonLime
                                    hasRoutines -> SurfaceElevated
                                    else -> SurfaceDark
                                }
                            )
                            .border(
                                1.dp,
                                when {
                                    isSelected -> NeonLime
                                    hasRoutines -> ElectricCyan.copy(alpha = 0.5f)
                                    else -> SurfaceBorder
                                },
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shortLabel,
                            color = when {
                                isSelected -> Color.Black
                                hasRoutines -> TextPrimary
                                else -> TextMuted
                            },
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    hasRoutines -> ElectricCyan
                                    else -> Color.Transparent
                                }
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. CARTE DU JOUR SÉLECTIONNÉ ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${selectedDayPair.second} • ${if (currentDayRoutines.isEmpty()) "Jour de Repos" else "${currentDayRoutines.size} séance(s) planifiée(s)"}",
                    color = if (currentDayRoutines.isEmpty()) TextMuted else ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                if (currentDayRoutines.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Repos & Récupération Musculaire",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Idéal pour une pesée BIA ou de la mobilité douce.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                } else {
                    currentDayRoutines.forEachIndexed { idx, routine ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = routine.name,
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "${routine.exercises.size} exercices • ~${routine.exercises.size * 10} min",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = { onStartWorkout(routine.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "LANCER", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
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
                var menuExpanded by remember { mutableStateOf(false) }

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
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                // Badges des muscles
                                val muscles = routine.exercises.map { it.primaryMuscle.displayName }.distinct()
                                if (muscles.isNotEmpty()) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        muscles.forEach { muscle ->
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
                            }

                            // Menu 3-points
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    modifier = Modifier.background(SurfaceElevated)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("✏️ Modifier la routine", color = TextPrimary) },
                                        onClick = {
                                            menuExpanded = false
                                            onEditRoutine(routine)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("📋 Dupliquer", color = ElectricCyan) },
                                        onClick = {
                                            menuExpanded = false
                                            onDuplicateRoutine(routine)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("📤 Partager (JSON)", color = AmberGold) },
                                        onClick = {
                                            menuExpanded = false
                                            routineToShareJson = routine
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🗑️ Supprimer", color = CrimsonRed) },
                                        onClick = {
                                            menuExpanded = false
                                            routineToDelete = routine
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Aperçu des exercices
                        if (routine.exercises.isNotEmpty()) {
                            Text(
                                text = routine.exercises.joinToString(", ") { it.exerciseName },
                                color = TextMuted,
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        } else {
                            Text(
                                text = "Aucun exercice ajouté pour l'instant",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bouton Lancer la séance
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${routine.exercises.size} exercices • ~${routine.exercises.size * 10} min",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Button(
                                onClick = { onStartWorkout(routine.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "LANCER", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- MODALE EXPORT / PARTAGE JSON ---
    routineToShareJson?.let { routine ->
        val jsonString = """
            {
              "id": "${routine.id}",
              "name": "${routine.name}",
              "notes": "${routine.notes}",
              "assignedDays": [${routine.assignedDays.joinToString(",")}],
              "exercises": [
                ${routine.exercises.joinToString(",\n    ") { ex ->
            """{"name": "${ex.exerciseName}", "sets": ${ex.sets.size}, "rest": ${ex.restTimeSeconds}}"""
        }}
              ]
            }
        """.trimIndent()

        AlertDialog(
            onDismissRequest = { routineToShareJson = null },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "📤 PARTAGE DE ROUTINE (JSON)",
                    color = AmberGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column {
                    Text(text = "Routine : ${routine.name}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevated)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = jsonString,
                            color = ElectricCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { routineToShareJson = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Fermer", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- MODALE CONFIRMATION SUPPRESSION ---
    routineToDelete?.let { routine ->
        AlertDialog(
            onDismissRequest = { routineToDelete = null },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "🗑️ SUPPRIMER LA ROUTINE",
                    color = CrimsonRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text(
                    text = "Êtes-vous sûr de vouloir supprimer la routine « ${routine.name} » ? Cette action est irréversible.",
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteRoutine(routine)
                        routineToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Supprimer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { routineToDelete = null }) {
                    Text(text = "Annuler", color = TextSecondary)
                }
            }
        )
    }
}
