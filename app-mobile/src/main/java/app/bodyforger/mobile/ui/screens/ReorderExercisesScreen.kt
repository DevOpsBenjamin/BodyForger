package app.bodyforger.mobile.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import app.bodyforger.core.model.RoutineExercise
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.Obsidian
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun ReorderExercisesScreen(
    initialExercises: List<RoutineExercise>,
    onConfirm: (List<RoutineExercise>) -> Unit,
    onCancel: () -> Unit
) {
    // Liste de travail isolée : si on quitte via la flèche retour, rien n'est sauvegardé !
    val workingList = remember {
        mutableStateListOf<RoutineExercise>().apply {
            addAll(initialExercises)
        }
    }

    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val itemHeightPx = with(LocalDensity.current) { 68.dp.toPx() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // --- 1. BARRE SUPÉRIEURE : Flèche Retour (Annule) + Titre Réorganiser ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Annuler",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "Réorganiser",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Espaceur invisible pour centrer le titre
            Spacer(modifier = Modifier.size(38.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. LISTE DRAG & DROP DES EXERCICES ---
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            itemsIndexed(workingList, key = { _, item -> item.id }) { index, exercise ->
                val isDragging = index == draggingIndex
                val elevation by animateFloatAsState(if (isDragging) 12f else 0f, label = "elevation")
                val scale by animateFloatAsState(if (isDragging) 1.03f else 1f, label = "scale")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .zIndex(if (isDragging) 10f else 1f)
                        .offset {
                            if (isDragging) IntOffset(0, dragOffsetY.roundToInt()) else IntOffset.Zero
                        }
                        .scale(scale)
                        .shadow(elevation.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDragging) SurfaceElevated.copy(alpha = 0.95f) else SurfaceDark)
                        .border(
                            1.dp,
                            if (isDragging) NeonLime else SurfaceBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Côté Gauche : Icône de muscle + Nom de l'exercice (Pas de bouton rouge)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = exercise.exerciseName,
                                color = if (isDragging) NeonLime else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${exercise.primaryMuscle.displayName} • ${exercise.sets.size} séries",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Côté Droit : Poignée Drag Handle (Triple ligne ≡) avec détection tactile fluide
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggingIndex = index
                                        dragOffsetY = 0f
                                    },
                                    onDragEnd = {
                                        draggingIndex = -1
                                        dragOffsetY = 0f
                                    },
                                    onDragCancel = {
                                        draggingIndex = -1
                                        dragOffsetY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetY += dragAmount.y

                                        val currentIdx = draggingIndex
                                        if (currentIdx != -1) {
                                            val threshold = itemHeightPx * 0.7f
                                            if (dragOffsetY > threshold && currentIdx < workingList.size - 1) {
                                                // Déplacement vers le bas
                                                val item = workingList.removeAt(currentIdx)
                                                workingList.add(currentIdx + 1, item)
                                                draggingIndex = currentIdx + 1
                                                dragOffsetY -= itemHeightPx
                                            } else if (dragOffsetY < -threshold && currentIdx > 0) {
                                                // Déplacement vers le haut
                                                val item = workingList.removeAt(currentIdx)
                                                workingList.add(currentIdx - 1, item)
                                                draggingIndex = currentIdx - 1
                                                dragOffsetY += itemHeightPx
                                            }
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Maintenir et glisser pour réorganiser",
                            tint = if (isDragging) NeonLime else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 3. BOUTON TERMINÉ AU BAS DE L'ÉCRAN (STYLE HEVY) ---
        Button(
            onClick = {
                onConfirm(workingList.toList())
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonLime,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Terminé",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
