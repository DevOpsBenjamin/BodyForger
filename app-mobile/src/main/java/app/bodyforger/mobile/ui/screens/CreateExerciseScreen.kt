package app.bodyforger.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import app.bodyforger.mobile.ui.components.CreateExerciseTopBar
import app.bodyforger.mobile.ui.components.EquipmentSelectorFlow
import app.bodyforger.mobile.ui.components.HealthConnectTypeSelectorFlow
import app.bodyforger.mobile.ui.components.MuscleSelectorFlow
import app.bodyforger.mobile.ui.components.SecondaryMuscleSelectorFlow
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
import java.util.UUID

@Composable
fun CreateExerciseScreen(
    onBack: () -> Unit,
    onExerciseCreated: (Exercise) -> Unit
) {
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf(MuscleGroup.CHEST) }
    var selectedSecondaryMuscles by remember { mutableStateOf(setOf<MuscleGroup>()) }
    var selectedEquipment by remember { mutableStateOf(EquipmentType.BARBELL) }
    var selectedHealthConnectType by remember { mutableStateOf(HealthConnectExerciseType.BENCH_PRESS) }
    var isUnilateral by remember { mutableStateOf(false) }

    val availableHealthConnectTypes = remember(selectedMuscle) {
        val matching = HealthConnectExerciseType.entries.filter {
            it.primaryMuscleGroup == selectedMuscle && it != HealthConnectExerciseType.REST
        }
        if (matching.isEmpty()) {
            listOf(HealthConnectExerciseType.OTHER_WORKOUT)
        } else {
            matching + HealthConnectExerciseType.OTHER_WORKOUT
        }
    }

    val handleSave = {
        if (name.isNotBlank()) {
            val newExercise = Exercise(
                id = "custom_${UUID.randomUUID().toString().take(8)}",
                name = name.trim(),
                healthConnectType = selectedHealthConnectType,
                primaryMuscleGroup = selectedMuscle,
                secondaryMuscleGroups = selectedSecondaryMuscles.toList(),
                equipment = selectedEquipment,
                isUnilateral = isUnilateral,
                isCustom = true
            )
            onExerciseCreated(newExercise)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .statusBarsPadding()
            .imePadding()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)
    ) {
        CreateExerciseTopBar(
            isSaveEnabled = name.isNotBlank(),
            onBack = onBack,
            onSave = handleSave
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Contenu du formulaire
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Text(text = "NOM DU MOUVEMENT", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(text = "Ex: Développé Incliné Prise Neutre", color = TextMuted, fontSize = 13.sp) },
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(text = "🎯 MUSCLE PRINCIPAL (Cible 100%)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            MuscleSelectorFlow(
                selectedMuscle = selectedMuscle,
                onMuscleSelected = { muscle ->
                    selectedMuscle = muscle
                    selectedSecondaryMuscles = selectedSecondaryMuscles - muscle
                    val firstMatching = HealthConnectExerciseType.entries.firstOrNull { it.primaryMuscleGroup == muscle }
                    selectedHealthConnectType = firstMatching ?: HealthConnectExerciseType.OTHER_WORKOUT
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(text = "⚡ MUSCLES SECONDAIRES (Optionnel - 50% Fatigue)", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryMuscleSelectorFlow(
                excludedMuscle = selectedMuscle,
                selectedSecondaryMuscles = selectedSecondaryMuscles,
                onToggleMuscle = { muscle ->
                    selectedSecondaryMuscles = if (selectedSecondaryMuscles.contains(muscle)) {
                        selectedSecondaryMuscles - muscle
                    } else {
                        selectedSecondaryMuscles + muscle
                    }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(text = "🏋️ MATÉRIEL REQUIS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            EquipmentSelectorFlow(
                selectedEquipment = selectedEquipment,
                onEquipmentSelected = { selectedEquipment = it }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = AmberGold, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "SYNCHRO GOOGLE / SAMSUNG HEALTH", color = AmberGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            HealthConnectTypeSelectorFlow(
                availableTypes = availableHealthConnectTypes,
                selectedType = selectedHealthConnectType,
                onTypeSelected = { selectedHealthConnectType = it }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Mouvement Unilatéral (1 Bras / 1 Jambe)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Enchaînement strict Côté Gauche ➡️ Côté Droit", color = TextMuted, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isUnilateral,
                    onCheckedChange = { isUnilateral = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = NeonLime,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = handleSave,
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonLime,
                    contentColor = Color.Black,
                    disabledContainerColor = SurfaceElevated,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "ENREGISTRER CET EXERCICE", fontWeight = FontWeight.Black, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
