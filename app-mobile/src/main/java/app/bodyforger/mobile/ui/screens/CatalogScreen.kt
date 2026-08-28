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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatalogScreen(
    customExercises: List<Exercise> = emptyList(),
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

    // Liste complète : Exercices personnalisés en premier, puis catalogue d'élite par défaut
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
        // --- 1. EN-TÊTE : Bouton Retour + Titre + Bouton Créer ---
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

        // --- 3. BARRE DE FILTRES ULTRA COMPACTE SUR 1 SEULE LIGNE ---
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            // Filtre 1 : 👤 Perso
            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (filterCustomOnly) NeonLime else SurfaceElevated)
                        .border(1.dp, if (filterCustomOnly) NeonLime else SurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { filterCustomOnly = !filterCustomOnly }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (filterCustomOnly) Color.Black else NeonLime,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Perso",
                            color = if (filterCustomOnly) Color.Black else TextPrimary,
                            fontWeight = if (filterCustomOnly) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Filtre 2 : ⇄ 1 Côté (Unilatéral)
            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (filterUnilateralOnly) AmberGold else SurfaceElevated)
                        .border(1.dp, if (filterUnilateralOnly) AmberGold else SurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { filterUnilateralOnly = !filterUnilateralOnly }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = if (filterUnilateralOnly) Color.Black else AmberGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "1 Côté",
                            color = if (filterUnilateralOnly) Color.Black else TextPrimary,
                            fontWeight = if (filterUnilateralOnly) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Filtre 3 : 🎯 Menu Déroulant Muscles (Multi-sélection)
            item {
                val hasMuscleFilter = selectedMuscles.isNotEmpty()
                val muscleLabel = when {
                    selectedMuscles.isEmpty() -> "Muscles : Tous"
                    selectedMuscles.size == 1 -> "Muscle : ${selectedMuscles.first().displayName}"
                    selectedMuscles.size == 2 -> "${selectedMuscles.elementAt(0).displayName}, ${selectedMuscles.elementAt(1).displayName}"
                    else -> "Muscles (${selectedMuscles.size})"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (hasMuscleFilter) NeonLime.copy(alpha = 0.2f) else SurfaceElevated)
                        .border(1.dp, if (hasMuscleFilter) NeonLime else SurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { showingMuscleDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = muscleLabel,
                            color = if (hasMuscleFilter) NeonLime else TextPrimary,
                            fontWeight = if (hasMuscleFilter) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (hasMuscleFilter) NeonLime else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Filtre 4 : 🏋️ Menu Déroulant Matériel (Multi-sélection)
            item {
                val hasEquipFilter = selectedEquipments.isNotEmpty()
                val equipLabel = when {
                    selectedEquipments.isEmpty() -> "Matériel : Tout"
                    selectedEquipments.size == 1 -> "Matériel : ${selectedEquipments.first().displayName}"
                    selectedEquipments.size == 2 -> "${selectedEquipments.elementAt(0).displayName}, ${selectedEquipments.elementAt(1).displayName}"
                    else -> "Matériel (${selectedEquipments.size})"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (hasEquipFilter) ElectricCyan.copy(alpha = 0.2f) else SurfaceElevated)
                        .border(1.dp, if (hasEquipFilter) ElectricCyan else SurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { showingEquipmentDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = equipLabel,
                            color = if (hasEquipFilter) ElectricCyan else TextPrimary,
                            fontWeight = if (hasEquipFilter) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (hasEquipFilter) ElectricCyan else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // --- 4. LISTE DES EXERCICES FILTRÉS (Espace d'affichage maximal à 85%) ---
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredExercises, key = { it.id }) { exercise ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (exercise.isCustom) NeonLime.copy(alpha = 0.5f) else SurfaceBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectExercise(exercise) },
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Ligne 1 : Nom de l'exercice
                            Text(
                                text = exercise.name,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Ligne 2 : Métadonnées et Icônes pures (Muscle • Matériel + 👤 / ⇄)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = exercise.primaryMuscleGroup.displayName,
                                    color = ElectricCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(text = "•", color = TextMuted, fontSize = 11.sp)

                                Text(
                                    text = exercise.equipment.displayName,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )

                                // Icône Pure : 👤 Perso
                                if (exercise.isCustom) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(NeonLime.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Exercice Personnalisé",
                                            tint = NeonLime,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }

                                // Icône Pure : ⇄ 1 Côté
                                if (exercise.isUnilateral) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(AmberGold.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = "Exercice Unilatéral",
                                            tint = AmberGold,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Ajouter",
                                tint = NeonLime,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGUE MULTI-SÉLECTION MUSCLES ---
    if (showingMuscleDialog) {
        AlertDialog(
            onDismissRequest = { showingMuscleDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "🎯 FILTRER PAR MUSCLES",
                    color = NeonLime,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column {
                    Text(
                        text = "Sélectionnez un ou plusieurs muscles (recherche en muscle principal et secondaire) :",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MuscleGroup.entries.filter { it != MuscleGroup.FULL_BODY }.forEach { muscle ->
                            val isSelected = selectedMuscles.contains(muscle)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NeonLime else SurfaceElevated)
                                    .border(1.dp, if (isSelected) NeonLime else SurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedMuscles = if (isSelected) {
                                            selectedMuscles - muscle
                                        } else {
                                            selectedMuscles + muscle
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = muscle.displayName,
                                        color = if (isSelected) Color.Black else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showingMuscleDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (selectedMuscles.isEmpty()) "Tous les muscles" else "Appliquer (${selectedMuscles.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMuscles = emptySet() }) {
                    Text(text = "Réinitialiser", color = TextSecondary, fontSize = 12.sp)
                }
            }
        )
    }

    // --- DIALOGUE MULTI-SÉLECTION MATÉRIEL ---
    if (showingEquipmentDialog) {
        AlertDialog(
            onDismissRequest = { showingEquipmentDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "🏋️ FILTRER PAR MATÉRIEL",
                    color = ElectricCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column {
                    Text(
                        text = "Sélectionnez le matériel disponible (idéal salle ou home gym) :",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EquipmentType.entries.filter { it != EquipmentType.OTHER }.forEach { equip ->
                            val isSelected = selectedEquipments.contains(equip)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ElectricCyan else SurfaceElevated)
                                    .border(1.dp, if (isSelected) ElectricCyan else SurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedEquipments = if (isSelected) {
                                            selectedEquipments - equip
                                        } else {
                                            selectedEquipments + equip
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = equip.displayName,
                                        color = if (isSelected) Color.Black else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showingEquipmentDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (selectedEquipments.isEmpty()) "Tout matériel" else "Appliquer (${selectedEquipments.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedEquipments = emptySet() }) {
                    Text(text = "Réinitialiser", color = TextSecondary, fontSize = 12.sp)
                }
            }
        )
    }
}
