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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.Exercise
import app.bodyforger.core.model.Routine
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.core.model.RoutineSet
import app.bodyforger.core.model.RoutineSetType
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
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoutineEditorScreen(
    initialRoutine: Routine? = null,
    onBack: () -> Unit = {},
    onOpenCatalogForAdd: () -> Unit = {},
    onOpenCatalogForReplace: (exerciseIndex: Int) -> Unit = {},
    onSaveRoutine: (Routine) -> Unit = {}
) {
    var routineName by remember { mutableStateOf(initialRoutine?.name ?: "") }
    var routineNotes by remember { mutableStateOf(initialRoutine?.notes ?: "") }
    val assignedDays = remember { mutableStateListOf<Int>().apply { addAll(initialRoutine?.assignedDays ?: emptySet()) } }
    val exercises = remember {
        mutableStateListOf<RoutineExercise>().apply {
            addAll(initialRoutine?.exercises ?: emptyList())
        }
    }

    var activeRestDialogExerciseIndex by remember { mutableStateOf<Int?>(null) }
    var activeSetTypeDialogIndex by remember { mutableStateOf<Pair<Int, Int>?>(null) } // exerciseIndex to setIndex

    val dayNames = listOf(
        1 to "Lun",
        2 to "Mar",
        3 to "Mer",
        4 to "Jeu",
        5 to "Ven",
        6 to "Sam",
        7 to "Dim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // --- 1. BARRE SUPÉRIEURE ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (initialRoutine == null) "NOUVELLE ROUTINE" else "MODIFIER ROUTINE",
                        color = NeonLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (routineName.isBlank()) "Sans titre" else routineName,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                }
            }

            // Bouton ENREGISTRER
            Button(
                onClick = {
                    if (routineName.isNotBlank()) {
                        val updatedRoutine = Routine(
                            id = initialRoutine?.id ?: UUID.randomUUID().toString(),
                            name = routineName.trim(),
                            notes = routineNotes.trim(),
                            assignedDays = assignedDays.toSet(),
                            exercises = exercises.mapIndexed { index, ex ->
                                ex.copy(orderIndex = index)
                            },
                            createdAtEpochMs = initialRoutine?.createdAtEpochMs ?: System.currentTimeMillis()
                        )
                        onSaveRoutine(updatedRoutine)
                    }
                },
                enabled = routineName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonLime,
                    contentColor = Color.Black,
                    disabledContainerColor = SurfaceElevated,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "ENREGISTRER", fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 2. CONTENU PRINCIPAL SCROLLABLE ---
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Section Infos Générales
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "TITRE DE LA ROUTINE",
                            color = NeonLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = routineName,
                            onValueChange = { routineName = it },
                            placeholder = { Text("Ex: Push Day, Séance Pecs/Épaules...", color = TextMuted, fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceElevated,
                                unfocusedContainerColor = SurfaceElevated,
                                focusedBorderColor = NeonLime,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "JOURS ASSIGNÉS DANS LA SEMAINE",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            dayNames.forEach { (dayInt, label) ->
                                val isSelected = assignedDays.contains(dayInt)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) ElectricCyan else SurfaceElevated)
                                        .border(1.dp, if (isSelected) ElectricCyan else SurfaceBorder, CircleShape)
                                        .clickable {
                                            if (isSelected) assignedDays.remove(dayInt) else assignedDays.add(dayInt)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.Black else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "NOTES DE ROUTINE (Optionnel)",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = routineNotes,
                            onValueChange = { routineNotes = it },
                            placeholder = { Text("Ex: Échauffement 5 min rameur + focus tempo 3-0-1-0", color = TextMuted, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceElevated,
                                unfocusedContainerColor = SurfaceElevated,
                                focusedBorderColor = NeonLime,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Section Exercices
            if (exercises.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Aucun exercice ajouté", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Appuyez ci-dessous pour composer votre séance", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                itemsIndexed(exercises, key = { _, item -> item.id }) { exIndex, exItem ->
                    var menuExpanded by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // En-tête de l'exercice
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = exItem.primaryMuscle.displayName,
                                            color = ElectricCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(text = "•", color = TextMuted, fontSize = 11.sp)
                                        Text(
                                            text = exItem.equipment.displayName,
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                        if (exItem.isUnilateral) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(AmberGold.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SwapHoriz,
                                                    contentDescription = null,
                                                    tint = AmberGold,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = exItem.exerciseName,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box {
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", tint = TextSecondary)
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                        modifier = Modifier.background(SurfaceElevated)
                                    ) {
                                        if (exIndex > 0) {
                                            DropdownMenuItem(
                                                text = { Text("⬆ Déplacer vers le haut", color = TextPrimary) },
                                                onClick = {
                                                    val item = exercises.removeAt(exIndex)
                                                    exercises.add(exIndex - 1, item)
                                                    menuExpanded = false
                                                }
                                            )
                                        }
                                        if (exIndex < exercises.size - 1) {
                                            DropdownMenuItem(
                                                text = { Text("⬇ Déplacer vers le bas", color = TextPrimary) },
                                                onClick = {
                                                    val item = exercises.removeAt(exIndex)
                                                    exercises.add(exIndex + 1, item)
                                                    menuExpanded = false
                                                }
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = { Text("🔄 Remplacer l'exercice", color = ElectricCyan) },
                                            onClick = {
                                                menuExpanded = false
                                                onOpenCatalogForReplace(exIndex)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("✕ Retirer l'exercice", color = CrimsonRed) },
                                            onClick = {
                                                exercises.removeAt(exIndex)
                                                menuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Ligne Chrono de Repos & Notes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceElevated)
                                        .clickable { activeRestDialogExerciseIndex = exIndex }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val restMin = exItem.restTimeSeconds / 60
                                    val restSec = exItem.restTimeSeconds % 60
                                    Text(
                                        text = if (restSec == 0) "Repos: ${restMin}min" else "Repos: ${restMin}min ${restSec}s",
                                        color = ElectricCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // En-tête des colonnes de séries
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SÉRIE",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(44.dp)
                                )
                                Text(
                                    text = "KG",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "RÉPÉTITIONS",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1.5f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.width(28.dp))
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Liste des séries
                            exItem.sets.forEachIndexed { setIdx, setItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Badge Type de série
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when (setItem.type) {
                                                    RoutineSetType.NORMAL -> SurfaceElevated
                                                    RoutineSetType.WARMUP -> AmberGold.copy(alpha = 0.2f)
                                                    RoutineSetType.DROPSET -> ElectricCyan.copy(alpha = 0.2f)
                                                    RoutineSetType.FAILURE -> CrimsonRed.copy(alpha = 0.2f)
                                                    RoutineSetType.REST_PAUSE -> NeonLime.copy(alpha = 0.2f)
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                when (setItem.type) {
                                                    RoutineSetType.NORMAL -> SurfaceBorder
                                                    RoutineSetType.WARMUP -> AmberGold
                                                    RoutineSetType.DROPSET -> ElectricCyan
                                                    RoutineSetType.FAILURE -> CrimsonRed
                                                    RoutineSetType.REST_PAUSE -> NeonLime
                                                },
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { activeSetTypeDialogIndex = exIndex to setIdx },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (setItem.type == RoutineSetType.NORMAL) "${setIdx + 1}" else setItem.type.shortBadge,
                                            color = when (setItem.type) {
                                                RoutineSetType.NORMAL -> TextPrimary
                                                RoutineSetType.WARMUP -> AmberGold
                                                RoutineSetType.DROPSET -> ElectricCyan
                                                RoutineSetType.FAILURE -> CrimsonRed
                                                RoutineSetType.REST_PAUSE -> NeonLime
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Poids (KG)
                                    var weightText by remember(setItem.targetWeightKg) {
                                        mutableStateOf(setItem.targetWeightKg?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
                                    }
                                    OutlinedTextField(
                                        value = weightText,
                                        onValueChange = { input ->
                                            weightText = input
                                            val parsed = input.toDoubleOrNull()
                                            val updatedSets = exItem.sets.toMutableList()
                                            updatedSets[setIdx] = setItem.copy(targetWeightKg = parsed)
                                            exercises[exIndex] = exItem.copy(sets = updatedSets)
                                        },
                                        placeholder = { Text("-", color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = SurfaceElevated,
                                            unfocusedContainerColor = SurfaceElevated,
                                            focusedBorderColor = NeonLime,
                                            unfocusedBorderColor = SurfaceBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Répétitions (Fixes ou Plage)
                                    if (!setItem.isRepsRange) {
                                        var repsText by remember(setItem.reps) {
                                            mutableStateOf(setItem.reps?.toString() ?: "10")
                                        }
                                        OutlinedTextField(
                                            value = repsText,
                                            onValueChange = { input ->
                                                repsText = input
                                                val parsed = input.toIntOrNull()
                                                val updatedSets = exItem.sets.toMutableList()
                                                updatedSets[setIdx] = setItem.copy(reps = parsed)
                                                exercises[exIndex] = exItem.copy(sets = updatedSets)
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = SurfaceElevated,
                                                unfocusedContainerColor = SurfaceElevated,
                                                focusedBorderColor = NeonLime,
                                                unfocusedBorderColor = SurfaceBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            modifier = Modifier
                                                .weight(1.5f)
                                                .height(44.dp)
                                        )
                                    } else {
                                        Row(
                                            modifier = Modifier.weight(1.5f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            var minText by remember(setItem.minReps) { mutableStateOf(setItem.minReps?.toString() ?: "8") }
                                            var maxText by remember(setItem.maxReps) { mutableStateOf(setItem.maxReps?.toString() ?: "12") }

                                            OutlinedTextField(
                                                value = minText,
                                                onValueChange = { input ->
                                                    minText = input
                                                    val parsed = input.toIntOrNull()
                                                    val updatedSets = exItem.sets.toMutableList()
                                                    updatedSets[setIdx] = setItem.copy(minReps = parsed)
                                                    exercises[exIndex] = exItem.copy(sets = updatedSets)
                                                },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = SurfaceElevated,
                                                    unfocusedContainerColor = SurfaceElevated,
                                                    focusedBorderColor = NeonLime,
                                                    unfocusedBorderColor = SurfaceBorder,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                            )
                                            Text(text = "à", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 3.dp))
                                            OutlinedTextField(
                                                value = maxText,
                                                onValueChange = { input ->
                                                    maxText = input
                                                    val parsed = input.toIntOrNull()
                                                    val updatedSets = exItem.sets.toMutableList()
                                                    updatedSets[setIdx] = setItem.copy(maxReps = parsed)
                                                    exercises[exIndex] = exItem.copy(sets = updatedSets)
                                                },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = SurfaceElevated,
                                                    unfocusedContainerColor = SurfaceElevated,
                                                    focusedBorderColor = NeonLime,
                                                    unfocusedBorderColor = SurfaceBorder,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                            )
                                        }
                                    }

                                    // Bouton Supprimer la série
                                    IconButton(
                                        onClick = {
                                            if (exItem.sets.size > 1) {
                                                val updatedSets = exItem.sets.toMutableList()
                                                updatedSets.removeAt(setIdx)
                                                exercises[exIndex] = exItem.copy(sets = updatedSets)
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Supprimer série",
                                            tint = if (exItem.sets.size > 1) TextMuted else Color.Transparent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Bouton + AJOUTER UNE SÉRIE
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceElevated)
                                    .clickable {
                                        val lastSet = exItem.sets.lastOrNull()
                                        val newSet = RoutineSet(
                                            setIndex = exItem.sets.size + 1,
                                            type = RoutineSetType.NORMAL,
                                            targetWeightKg = lastSet?.targetWeightKg,
                                            reps = lastSet?.reps ?: 10,
                                            minReps = lastSet?.minReps ?: 8,
                                            maxReps = lastSet?.maxReps ?: 12,
                                            isRepsRange = lastSet?.isRepsRange ?: false
                                        )
                                        exercises[exIndex] = exItem.copy(sets = exItem.sets + newSet)
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = NeonLime, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "AJOUTER UNE SÉRIE", color = NeonLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Bouton + AJOUTER UN EXERCICE
            item {
                Button(
                    onClick = onOpenCatalogForAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "AJOUTER UN EXERCICE", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // --- MODALE DU CHRONOMÈTRE DE REPOS ---
    activeRestDialogExerciseIndex?.let { exIdx ->
        val currentRest = exercises.getOrNull(exIdx)?.restTimeSeconds ?: 90
        val options = listOf(30, 45, 60, 75, 90, 120, 150, 180, 240, 300)

        AlertDialog(
            onDismissRequest = { activeRestDialogExerciseIndex = null },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "⏱️ TEMPS DE REPOS",
                    color = ElectricCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    options.forEach { sec ->
                        val isSelected = currentRest == sec
                        val label = if (sec < 60) "${sec}s" else if (sec % 60 == 0) "${sec / 60}min" else "${sec / 60}m ${sec % 60}s"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ElectricCyan else SurfaceElevated)
                                .border(1.dp, if (isSelected) ElectricCyan else SurfaceBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    exercises[exIdx] = exercises[exIdx].copy(restTimeSeconds = sec)
                                    activeRestDialogExerciseIndex = null
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeRestDialogExerciseIndex = null }) {
                    Text(text = "Fermer", color = TextSecondary)
                }
            }
        )
    }

    // --- MODALE DU TYPE DE SÉRIE & OPTIONS DE RÉPÉTITIONS ---
    activeSetTypeDialogIndex?.let { (exIdx, setIdx) ->
        val currentSet = exercises.getOrNull(exIdx)?.sets?.getOrNull(setIdx)
        if (currentSet != null) {
            AlertDialog(
                onDismissRequest = { activeSetTypeDialogIndex = null },
                containerColor = SurfaceDark,
                title = {
                    Text(
                        text = "⚙️ OPTIONS DE LA SÉRIE ${setIdx + 1}",
                        color = NeonLime,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "TYPE DE SÉRIE :", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        RoutineSetType.entries.forEach { type ->
                            val isSelected = currentSet.type == type
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NeonLime.copy(alpha = 0.15f) else SurfaceElevated)
                                    .border(1.dp, if (isSelected) NeonLime else SurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val updatedSets = exercises[exIdx].sets.toMutableList()
                                        updatedSets[setIdx] = currentSet.copy(type = type)
                                        exercises[exIdx] = exercises[exIdx].copy(sets = updatedSets)
                                        activeSetTypeDialogIndex = null
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = type.displayName,
                                    color = if (isSelected) NeonLime else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = type.shortBadge,
                                    color = if (isSelected) NeonLime else TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "FORMAT DES RÉPÉTITIONS :", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Option 1 : Répétitions fixes
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!currentSet.isRepsRange) ElectricCyan else SurfaceElevated)
                                    .border(1.dp, if (!currentSet.isRepsRange) ElectricCyan else SurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val updatedSets = exercises[exIdx].sets.toMutableList()
                                        updatedSets[setIdx] = currentSet.copy(isRepsRange = false)
                                        exercises[exIdx] = exercises[exIdx].copy(sets = updatedSets)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Répétitions fixes",
                                    color = if (!currentSet.isRepsRange) Color.Black else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            // Option 2 : Plage de répétitions (ex 10-15)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (currentSet.isRepsRange) ElectricCyan else SurfaceElevated)
                                    .border(1.dp, if (currentSet.isRepsRange) ElectricCyan else SurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val updatedSets = exercises[exIdx].sets.toMutableList()
                                        updatedSets[setIdx] = currentSet.copy(isRepsRange = true)
                                        exercises[exIdx] = exercises[exIdx].copy(sets = updatedSets)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Plage (ex: 8-12)",
                                    color = if (currentSet.isRepsRange) Color.Black else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeSetTypeDialogIndex = null }) {
                        Text(text = "Valider", color = NeonLime, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

// Fonction utilitaire pour convertir un Exercice du catalogue en RoutineExercise
fun Exercise.toRoutineExercise(routineId: String = ""): RoutineExercise = RoutineExercise(
    id = UUID.randomUUID().toString(),
    routineId = routineId,
    exerciseId = id,
    exerciseName = name,
    primaryMuscle = primaryMuscleGroup,
    equipment = equipment,
    isUnilateral = isUnilateral,
    orderIndex = 0,
    restTimeSeconds = 90,
    notes = "",
    sets = listOf(
        RoutineSet(setIndex = 1, type = RoutineSetType.NORMAL, reps = 10),
        RoutineSet(setIndex = 2, type = RoutineSetType.NORMAL, reps = 10),
        RoutineSet(setIndex = 3, type = RoutineSetType.NORMAL, reps = 10)
    )
)
