package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted

@Composable
fun ActivityHeatmapCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RÉGULARITÉ & ENTRAÎNEMENT",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "4 séances cette semaine",
                    color = NeonLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ActivityHeatmapGrid()

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Moins", color = TextMuted, fontSize = 9.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(SurfaceElevated))
                Spacer(modifier = Modifier.width(3.dp))
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(NeonLime.copy(alpha = 0.4f)))
                Spacer(modifier = Modifier.width(3.dp))
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(NeonLime))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Plus", color = TextMuted, fontSize = 9.sp)
            }
        }
    }
}

@Composable
fun ActivityHeatmapGrid() {
    val activeDays = remember {
        setOf(
            1, 3, 5, 8, 10, 12, 15, 17, 19, 22, 24, 26, 29, 31, 33, 36, 38, 40,
            43, 45, 47, 50, 52, 54, 57, 59, 61, 64, 66, 68, 71, 73, 75, 78, 80, 82
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (col in 0 until 14) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0 until 5) {
                    val dayIndex = col * 5 + row
                    val isActive = activeDays.contains(dayIndex)
                    val isHeavy = dayIndex % 3 == 0

                    val boxColor = when {
                        !isActive -> SurfaceElevated
                        isHeavy -> NeonLime
                        else -> NeonLime.copy(alpha = 0.5f)
                    }

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(boxColor)
                    )
                }
            }
        }
    }
}
