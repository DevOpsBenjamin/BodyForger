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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.Exercise
import app.bodyforger.mobile.ui.text.label
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary

@Composable
fun CatalogExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (exercise.isCustom) NeonLime.copy(alpha = 0.5f) else SurfaceBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
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
                Text(
                    text = exercise.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = exercise.primaryMuscleGroup.label(),
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(text = "•", color = TextMuted, fontSize = 11.sp)

                    Text(
                        text = exercise.equipment.label(),
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    if (exercise.isCustom) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(NeonLime.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = NeonLime,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }

                    if (exercise.isUnilateral) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(AmberGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
