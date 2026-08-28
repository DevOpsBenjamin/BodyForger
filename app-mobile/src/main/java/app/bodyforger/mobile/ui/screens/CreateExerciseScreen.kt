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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@OptIn(ExperimentalLayoutApi::class)
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

    // Liste des types Google pertinents pour le muscle principal sélectionné
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .statusBarsPadding()
            .imePadding()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)
    ) {
        // --- 1. EN-TÊTE : Bouton Retour + Titre + Bouton Enregistrer ---
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
                        text = "PERSONNALISATION",
                        color = NeonLime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "CRÉER UN EXERCICE",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Bouton Enregistrer en haut
            IconButton(
                onClick = {
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
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (name.isNotBlank()) NeonLime else SurfaceElevated)
                    .border(1.dp, if (name.isNotBlank()) NeonLime else SurfaceBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Valider",
                    tint = if (name.isNotBlank()) Color.Black else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- CONTENU DÉROULANT VERTICAL DU FORMULAIRE ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 1. Nom de l'exercice
            Text(
                text = "NOM DU MOUVEMENT",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(text = "Ex: Développé Incliné Prise Neutre", color = TextMuted, fontSize = 14.sp) },
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Groupe Musculaire Principal (FlowRow : tout visible d'un coup)
            Text(
                text = "🎯 GROUPE MUSCULAIRE PRINCIPAL (Cible 100%)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MuscleGroup.entries.filter { it != MuscleGroup.FULL_BODY }.forEach { muscle ->
                    val isSelected = selectedMuscle == muscle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NeonLime else SurfaceElevated)
                            .border(1.dp, if (isSelected) NeonLime else SurfaceBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                selectedMuscle = muscle
                                selectedSecondaryMuscles = selectedSecondaryMuscles - muscle // Retirer des secondaires si sélectionné en principal
                                val firstMatching = HealthConnectExerciseType.entries.firstOrNull { it.primaryMuscleGroup == muscle }
                                selectedHealthConnectType = firstMatching ?: HealthConnectExerciseType.OTHER_WORKOUT
                            }
                            .padding(horizontal = 13.dp, vertical = 8.dp)
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

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Muscles Secondaires / Synergistes (Multi-sélection facultative en FlowRow)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⚡ MUSCLES SECONDAIRES / SYNERGISTES (Optionnel)",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = "Comptabilisés à 50% du volume dans votre Heatmap et vos stats de récupération",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MuscleGroup.entries.filter { it != MuscleGroup.FULL_BODY && it != selectedMuscle }.forEach { muscle ->
                    val isSelected = selectedSecondaryMuscles.contains(muscle)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ElectricCyan.copy(alpha = 0.25f) else SurfaceElevated)
                            .border(1.dp, if (isSelected) ElectricCyan else SurfaceBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                selectedSecondaryMuscles = if (isSelected) {
                                    selectedSecondaryMuscles - muscle
                                } else {
                                    selectedSecondaryMuscles + muscle
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = muscle.displayName,
                                color = if (isSelected) ElectricCyan else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Matériel / Équipement (FlowRow)
            Text(
                text = "🏋️ MATÉRIEL & ÉQUIPEMENT REQUIS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                EquipmentType.entries.filter { it != EquipmentType.OTHER }.forEach { equip ->
                    val isSelected = selectedEquipment == equip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ElectricCyan else SurfaceElevated)
                            .border(1.dp, if (isSelected) ElectricCyan else SurfaceBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedEquipment = equip }
                            .padding(horizontal = 13.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = equip.displayName,
                            color = if (isSelected) Color.Black else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Synchronisation Google Health Connect (FlowRow avec noms français clairs)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SYNCHRONISATION GOOGLE HEALTH CONNECT",
                    color = AmberGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = "Mouvement reconnu lors de la synchronisation vers Google Fit & Samsung Health",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                availableHealthConnectTypes.forEach { hcType ->
                    val isSelected = selectedHealthConnectType == hcType
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) AmberGold else SurfaceElevated)
                            .border(1.dp, if (isSelected) AmberGold else SurfaceBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedHealthConnectType = hcType }
                            .padding(horizontal = 13.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = hcType.canonicalNameFr,
                            color = if (isSelected) Color.Black else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 6. Switch Exercice Unilatéral
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mouvement Unilatéral (1 Bras / 1 Jambe)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Active l'enchaînement strict Côté Gauche ➡️ Côté Droit",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

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

            Spacer(modifier = Modifier.height(26.dp))

            // 7. Bouton Principal du bas
            Button(
                onClick = {
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
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonLime,
                    contentColor = Color.Black,
                    disabledContainerColor = SurfaceElevated,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "ENREGISTRER CET EXERCICE",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
