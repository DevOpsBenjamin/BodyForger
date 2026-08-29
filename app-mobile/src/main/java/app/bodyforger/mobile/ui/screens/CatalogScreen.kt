package app.bodyforger.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import app.bodyforger.core.database.data.DefaultExercises
import app.bodyforger.core.database.entity.toDomain
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.Exercise
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.mobile.ui.components.CatalogEquipmentFilterDialog
import app.bodyforger.mobile.ui.components.CatalogExerciseCard
import app.bodyforger.mobile.ui.components.CatalogFilterBar
import app.bodyforger.mobile.ui.components.CatalogMuscleFilterDialog
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

@Composable
fun CatalogScreen(
    customExercises: List<Exercise> = emptyList(),
    isSelectionMode: Boolean = false,
    onBack: () -> Unit = {},
    onOpenCreateExercise: () -> Unit = {},
    onSelectExercise: (Exercise) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscles by remember { mutableStateOf(setOf<MuscleGroup>()) }
    var selectedEquipments by remember { mutableStateOf(setOf<EquipmentType>()) }
    var filterCustomOnly by remember { mutableStateOf(false) }
    var filterUnilateralOnly by remember { mutableStateOf(false) }

    var showingMuscleDialog by remember { mutableStateOf(false) }
    var showingEquipmentDialog by remember { mutableStateOf(false) }

    val allExercises = remember(customExercises.size) {
        customExercises + DefaultExercises.all.map { it.toDomain() }
    }

    val filteredExercises = allExercises.filter { exercise ->
        val matchesMuscle = selectedMuscles.isEmpty() ||
                selectedMuscles.contains(exercise.primaryMuscleGroup) ||
                exercise.secondaryMuscleGroups.any { selectedMuscles.contains(it) }

        val matchesEquipment = selectedEquipments.isEmpty() ||
                selectedEquipments.contains(exercise.equipment)

        val matchesCustom = !filterCustomOnly || exercise.isCustom
        val matchesUnilateral = !filterUnilateralOnly || exercise.isUnilateral
        val matchesSearch = searchQuery.isEmpty() ||
                exercise.name.contains(searchQuery, ignoreCase = true) ||
                exercise.healthConnectType.canonicalNameEn.contains(searchQuery, ignoreCase = true)

        matchesMuscle && matchesEquipment && matchesCustom && matchesUnilateral && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)
    ) {
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
                        text = "CATALOGUE",
                        color = NeonLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "EXERCICES (${filteredExercises.size})",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Bouton + CRÉER
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeonLime)
                    .clickable { onOpenCreateExercise() }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "CRÉER",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 2. BARRE DE RECHERCHE ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(text = "Rechercher un mouvement, un muscle...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedBorderColor = NeonLime,
                unfocusedBorderColor = SurfaceBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // --- 3. BARRE DE FILTRES SUR 1 SEULE LIGNE ---
        CatalogFilterBar(
            filterCustomOnly = filterCustomOnly,
            onToggleCustomOnly = { filterCustomOnly = !filterCustomOnly },
            filterUnilateralOnly = filterUnilateralOnly,
            onToggleUnilateralOnly = { filterUnilateralOnly = !filterUnilateralOnly },
            selectedMuscles = selectedMuscles,
            onOpenMuscleDialog = { showingMuscleDialog = true },
            selectedEquipments = selectedEquipments,
            onOpenEquipmentDialog = { showingEquipmentDialog = true }
        )

        // --- 4. LISTE DES EXERCICES FILTRÉS ---
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredExercises, key = { it.id }) { exercise ->
                CatalogExerciseCard(
                    exercise = exercise,
                    onClick = { onSelectExercise(exercise) }
                )
            }
        }
    }

    // --- DIALOGUES EXTRAITS ---
    if (showingMuscleDialog) {
        CatalogMuscleFilterDialog(
            selectedMuscles = selectedMuscles,
            onToggleMuscle = { muscle ->
                selectedMuscles = if (selectedMuscles.contains(muscle)) {
                    selectedMuscles - muscle
                } else {
                    selectedMuscles + muscle
                }
            },
            onReset = { selectedMuscles = emptySet() },
            onDismiss = { showingMuscleDialog = false }
        )
    }

    if (showingEquipmentDialog) {
        CatalogEquipmentFilterDialog(
            selectedEquipments = selectedEquipments,
            onToggleEquipment = { equip ->
                selectedEquipments = if (selectedEquipments.contains(equip)) {
                    selectedEquipments - equip
                } else {
                    selectedEquipments + equip
                }
            },
            onReset = { selectedEquipments = emptySet() },
            onDismiss = { showingEquipmentDialog = false }
        )
    }
}
