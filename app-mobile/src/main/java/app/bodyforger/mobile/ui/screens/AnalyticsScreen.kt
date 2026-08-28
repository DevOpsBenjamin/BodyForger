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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.bia.DexaBiaCalculator
import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.BiologicalSex
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
import kotlinx.coroutines.launch

@Composable
fun AnalyticsScreen() {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .padding(top = 20.dp)
    ) {
        // En-tête avec Sélecteur à 2 Onglets (Swipable)
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "STATISTIQUES & ANALYSES",
                color = if (pagerState.currentPage == 0) NeonLime else ElectricCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = if (pagerState.currentPage == 0) "PERFORMANCE & MUSCU" else "BIOMÉTRIE & BIA DEXA",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
            )

            // Sélecteur Swipable Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (pagerState.currentPage == 0) NeonLime else Color.Transparent)
                        .clickable {
                            coroutineScope.launch { pagerState.animateScrollToPage(0) }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏋️ Performance",
                        color = if (pagerState.currentPage == 0) Color.Black else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (pagerState.currentPage == 0) FontWeight.Black else FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (pagerState.currentPage == 1) ElectricCyan else Color.Transparent)
                        .clickable {
                            coroutineScope.launch { pagerState.animateScrollToPage(1) }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🧬 Biométrie BIA",
                        color = if (pagerState.currentPage == 1) Color.Black else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (pagerState.currentPage == 1) FontWeight.Black else FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // HorizontalPager permettant le SWIPE GESTURE fluide
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> PerformanceView()
                1 -> BiometricsBiaView()
            }
        }
    }
}

/**
 * VUE 1 : PERFORMANCE & MUSCULATION (openGym style)
 */
@Composable
fun PerformanceView() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        // 1. Tonnage Total Hebdomadaire & Séances
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, NeonLime.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = NeonLime, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Tonnage Hebdo", color = TextMuted, fontSize = 11.sp)
                    Text(text = "24.8 T", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, AmberGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Calories Actives", color = TextMuted, fontSize = 11.sp)
                    Text(text = "2 450 kcal", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Heatmap & Volume par Muscle (openGym Style)
        Text(
            text = "VOLUME PAR GROUPE MUSCULAIRE (SÉRIES)",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                MuscleVolumeRow(muscle = "Pectoraux", completed = 14, target = 16, color = NeonLime)
                MuscleVolumeRow(muscle = "Grand Dorsal / Dos", completed = 16, target = 16, color = NeonLime)
                MuscleVolumeRow(muscle = "Épaules (Deltoïdes)", completed = 10, target = 12, color = AmberGold)
                MuscleVolumeRow(muscle = "Quadriceps & Ischios", completed = 12, target = 16, color = AmberGold)
                MuscleVolumeRow(muscle = "Bras (Biceps / Triceps)", completed = 12, target = 12, color = NeonLime, isLast = true)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Records Personnels & 1RM
        Text(
            text = "ESTIMATION DES 1RM & RECORDS (PRS)",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                PRRow(exercise = "Barbell Bench Press", oneRM = "110.0 kg", repRecord = "90 kg × 8 reps", diff = "+5 kg")
                PRRow(exercise = "Barbell Deadlift", oneRM = "175.0 kg", repRecord = "150 kg × 5 reps", diff = "+7.5 kg")
                PRRow(exercise = "Barbell Back Squat", oneRM = "140.0 kg", repRecord = "120 kg × 6 reps", diff = "+2.5 kg", isLast = true)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * VUE 2 : BIOMÉTRIE & BIA DEXA (SimpleBodyGraph style)
 */
@Composable
fun BiometricsBiaView() {
    val scrollState = rememberScrollState()

    var userMassKg by remember { mutableStateOf(78.5) }
    val profile = remember {
        BiaProfile(
            sex = BiologicalSex.MALE,
            ageYears = 29,
            heightCm = 182.0
        )
    }

    val report = remember(userMassKg) {
        DexaBiaCalculator.calculate(massKg = userMassKg, profile = profile)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        // Carte Principale : Poids & Masse Grasse
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(text = "MASSE TOTALE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$userMassKg kg",
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "TAUX DE GRAS (BF)", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${report.bodyFatPercentage}%",
                                color = NeonLime,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.TrendingDown, contentDescription = null, tint = NeonLime, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val fatMass = ((userMassKg - report.fatFreeMassKg) * 10).toInt() / 10.0
                val leanMass = report.fatFreeMassKg
                val leanRatio = (leanMass / userMassKg).toFloat().coerceIn(0f, 1f)

                Text(
                    text = "Masse Maigre : ${leanMass} kg (${(leanRatio * 100).toInt()}%) | Masse Grasse : ${fatMass} kg",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LinearProgressIndicator(
                    progress = { leanRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ElectricCyan,
                    trackColor = AmberGold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Répartition Segmentaire 5 Zones
        Text(
            text = "ANALYSE SEGMENTAIRE 5 ZONES",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        val totalMuscle = report.skeletalMuscleMassKg
        val trunkMuscle = ((totalMuscle * 0.46) * 10).toInt() / 10.0
        val armMuscle = ((totalMuscle * 0.09) * 10).toInt() / 10.0
        val legMuscle = ((totalMuscle * 0.18) * 10).toInt() / 10.0

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SegmentalAnalyticsRow(label = "Tronc (Torse & Abdomen)", value = "$trunkMuscle kg", percentage = "46%")
                SegmentalAnalyticsRow(label = "Bras Droit", value = "$armMuscle kg", percentage = "9%")
                SegmentalAnalyticsRow(label = "Bras Gauche", value = "$armMuscle kg", percentage = "9%")
                SegmentalAnalyticsRow(label = "Jambe Droite", value = "$legMuscle kg", percentage = "18%")
                SegmentalAnalyticsRow(label = "Jambe Gauche", value = "$legMuscle kg", percentage = "18%", isLast = true)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Compartiments Hydriques
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(imageVector = Icons.Default.Opacity, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Eau Totale (TBW)", color = TextMuted, fontSize = 11.sp)
                    Text(text = "${report.totalBodyWaterLiters} L", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, tint = NeonLime, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Muscle Squelettique", color = TextMuted, fontSize = 11.sp)
                    Text(text = "${report.skeletalMuscleMassKg} kg", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Bouton Pesée BLE Huawei Scale 3
        Button(
            onClick = {
                userMassKg = if (userMassKg == 78.5) 78.2 else 78.5
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricCyan,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(imageVector = Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PESER VIA HUAWEI SCALE 3",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun MuscleVolumeRow(muscle: String, completed: Int, target: Int, color: Color, isLast: Boolean = false) {
    val ratio = (completed.toFloat() / target).coerceIn(0f, 1f)
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = muscle, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "$completed / $target séries", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(2.5.dp)),
            color = color,
            trackColor = SurfaceElevated
        )
    }
    if (!isLast) {
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun PRRow(exercise: String, oneRM: String, repRecord: String, diff: String, isLast: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = exercise, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = "Record : $repRecord", color = TextMuted, fontSize = 11.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "1RM: $oneRM", color = NeonLime, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text(text = diff, color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
    if (!isLast) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SurfaceElevated)
        )
    }
}

@Composable
fun SegmentalAnalyticsRow(label: String, value: String, percentage: String, isLast: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(text = "$percentage de la masse musculaire", color = TextMuted, fontSize = 10.sp)
        }
        Text(text = value, color = ElectricCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
    if (!isLast) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SurfaceElevated)
        )
    }
}
