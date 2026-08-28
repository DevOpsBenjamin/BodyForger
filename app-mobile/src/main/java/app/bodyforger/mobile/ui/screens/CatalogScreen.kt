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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import app.bodyforger.core.model.EquipmentType
import app.bodyforger.core.model.Exercise
import app.bodyforger.core.model.HealthConnectExerciseType
import app.bodyforger.core.model.MuscleGroup
import app.bodyforger.mobile.ui.theme.ElectricCyan
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
    onSelectExercise: (Exercise) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf<MuscleGroup?>(null) }

    val allExercises = remember {
        HealthConnectExerciseType.entries
            .filter { it != HealthConnectExerciseType.REST && it != HealthConnectExerciseType.OTHER_WORKOUT }
            .map { type ->
                Exercise(
                    id = "hc_${type.segmentTypeId}",
                    name = type.canonicalNameFr,
                    healthConnectType = type,
                    primaryMuscleGroup = type.primaryMuscleGroup,
                    equipment = type.defaultEquipment
                )
            }
    }

    val filteredExercises = allExercises.filter { exercise ->
        (selectedMuscle == null || exercise.primaryMuscleGroup == selectedMuscle) &&
                (searchQuery.isEmpty() || exercise.name.contains(searchQuery, ignoreCase = true) || exercise.healthConnectType.canonicalNameEn.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = "CATALOGUE CANONIQUE GOOGLE",
            color = NeonLime,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Text(
            text = "EXERCICES HEALTH CONNECT",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // Barre de recherche
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(text = "Rechercher un mouvement, un muscle...", color = TextMuted, fontSize = 14.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedBorderColor = NeonLime,
                unfocusedBorderColor = SurfaceBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        )

        // Filtres par Groupe Musculaire
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            item {
                val isAllSelected = selectedMuscle == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isAllSelected) NeonLime else SurfaceElevated)
                        .border(1.dp, if (isAllSelected) NeonLime else SurfaceBorder, RoundedCornerShape(10.dp))
                        .clickable { selectedMuscle = null }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Tous (${allExercises.size})",
                        color = if (isAllSelected) Color.Black else TextSecondary,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }

            items(MuscleGroup.entries.filter { it != MuscleGroup.FULL_BODY }) { muscle ->
                val isSelected = selectedMuscle == muscle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) NeonLime else SurfaceElevated)
                        .border(1.dp, if (isSelected) NeonLime else SurfaceBorder, RoundedCornerShape(10.dp))
                        .clickable { selectedMuscle = muscle }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = muscle.displayName,
                        color = if (isSelected) Color.Black else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Liste des Exercices Canoniques
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredExercises) { exercise ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                        .clickable { onSelectExercise(exercise) },
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exercise.name,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = exercise.primaryMuscleGroup.displayName,
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(text = " • ", color = TextMuted)
                                Text(
                                    text = exercise.equipment.displayName,
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Ajouter à la séance",
                                tint = NeonLime,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
