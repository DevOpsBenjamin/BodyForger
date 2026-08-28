package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.ui.screens.LiveSetItem
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary

@Composable
fun LiveWorkoutSetsCard(
    sets: List<LiveSetItem>,
    onToggleSetDone: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "SÉRIE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "CHARGE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "REPS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "STATUT", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            sets.forEachIndexed { idx, set ->
                val isCurrent = !set.isDone && (idx == 0 || sets[idx - 1].isDone)
                val bgColor = if (isCurrent) SurfaceElevated else Color.Transparent
                val borderColor = if (isCurrent) NeonLime.copy(alpha = 0.5f) else Color.Transparent

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { onToggleSetDone(idx) }
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SET ${set.index}",
                            color = if (set.isDone) NeonLime else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (set.type == "DROPSET") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AmberGold.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "DROP", color = AmberGold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Text(
                        text = "${set.weightKg} kg",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "${set.reps} reps",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (set.isDone) NeonLime else SurfaceElevated)
                            .border(1.dp, if (set.isDone) NeonLime else SurfaceBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (set.isDone) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Fait",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (idx < sets.size - 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}
