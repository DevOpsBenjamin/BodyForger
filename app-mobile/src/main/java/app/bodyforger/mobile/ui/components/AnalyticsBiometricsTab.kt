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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.WeightUnit
import app.bodyforger.mobile.R
import app.bodyforger.mobile.profile.BiometricsViewModel
import app.bodyforger.mobile.ui.components.BiometricsEmptyState
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary
import kotlin.math.roundToInt
import org.koin.androidx.compose.koinViewModel

@Composable
fun AnalyticsBiometricsTab(
    modifier: Modifier = Modifier,
    onOpenScale: () -> Unit = {},
    viewModel: BiometricsViewModel = koinViewModel()
) {
    val scrollState = rememberScrollState()
    val state by viewModel.state.collectAsState()
    val unit by viewModel.weightUnit.collectAsState()

    val lastLog = state.lastLog
    val report = state.report
    if (lastLog == null || report == null) {
        BiometricsEmptyState(isProfileComplete = state.profile.isComplete, onOpenScale = onOpenScale)
        return
    }
    val userMassKg = lastLog.massKg

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        // Main card: mass & body fat
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
                        Text(text = stringResource(R.string.bio_total_mass), color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = unit.formatWithSymbol(userMassKg),
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = stringResource(R.string.bio_body_fat), color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = lastLog.bodyFatPercentage?.let { "${formatMeasure(it)}%" }
                                    ?: stringResource(R.string.bio_body_fat_absent),
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

                val fatMass = userMassKg - report.fatFreeMassKg
                val leanMass = report.fatFreeMassKg
                val leanRatio = (leanMass / userMassKg).toFloat().coerceIn(0f, 1f)

                Text(
                    text = stringResource(
                        R.string.bio_lean_fat_summary,
                        unit.formatWithSymbol(leanMass),
                        (leanRatio * 100).toInt(),
                        unit.formatWithSymbol(fatMass)
                    ),
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

        Text(
            text = stringResource(R.string.bio_segmental_title),
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Read from the engine's own decomposition, not from a share of the total: the six
        // measured paths give each limb separately, so a left arm and a right arm differ.
        val segmental = report.segmentalMuscle
        if (segmental == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.bio_segmental_unavailable),
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            val total = segmental.totalKg
            fun share(kg: Double) = if (total > 0.0) "${(kg / total * 100).roundToInt()}%" else ""

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SegmentalAnalyticsRow(label = stringResource(R.string.bio_segment_trunk), value = unit.formatWithSymbol(segmental.trunkKg), percentage = share(segmental.trunkKg))
                    SegmentalAnalyticsRow(label = stringResource(R.string.bio_segment_right_arm), value = unit.formatWithSymbol(segmental.rightArmKg), percentage = share(segmental.rightArmKg))
                    SegmentalAnalyticsRow(label = stringResource(R.string.bio_segment_left_arm), value = unit.formatWithSymbol(segmental.leftArmKg), percentage = share(segmental.leftArmKg))
                    SegmentalAnalyticsRow(label = stringResource(R.string.bio_segment_right_leg), value = unit.formatWithSymbol(segmental.rightLegKg), percentage = share(segmental.rightLegKg))
                    SegmentalAnalyticsRow(label = stringResource(R.string.bio_segment_left_leg), value = unit.formatWithSymbol(segmental.leftLegKg), percentage = share(segmental.leftLegKg), isLast = true)
                }
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
                    Text(text = stringResource(R.string.bio_total_water), color = TextMuted, fontSize = 11.sp)
                    Text(text = "${formatMeasure(report.totalBodyWaterKg)} L", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                    Text(text = stringResource(R.string.bio_skeletal_muscle), color = TextMuted, fontSize = 11.sp)
                    Text(text = unit.formatWithSymbol(report.skeletalMuscleMassKg), color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onOpenScale,
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
                text = stringResource(R.string.bio_weigh_in_button),
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
            Text(text = stringResource(R.string.bio_segment_share, percentage), color = TextMuted, fontSize = 10.sp)
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
