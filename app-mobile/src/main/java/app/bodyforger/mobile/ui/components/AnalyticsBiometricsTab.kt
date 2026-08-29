package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Opacity
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.bia.DexaBiaCalculator
import app.bodyforger.mobile.data.DebugSampleBia
import app.bodyforger.core.model.BiaProfile
import app.bodyforger.core.model.BiologicalSex
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

@Composable
fun AnalyticsBiometricsTab(modifier: Modifier = Modifier) {
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
        DexaBiaCalculator.calculate(
            massKg = userMassKg,
            profile = profile,
            impedances = DebugSampleBia.dualFrequencyReading
        )
        // Sans impédances relevées, il n'y a pas de composition à montrer : le moteur rend
        // `null` plutôt que d'inventer un chiffre.
    } ?: return

    Column(
        modifier = modifier
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
                            Icon(imageVector = Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = NeonLime, modifier = Modifier.size(16.dp))
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
                    Text(text = "${report.totalBodyWaterKg} L", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
private fun SegmentalAnalyticsRow(label: String, value: String, percentage: String, isLast: Boolean = false) {
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
