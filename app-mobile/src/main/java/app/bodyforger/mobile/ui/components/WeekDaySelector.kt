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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.core.model.Routine
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.NeonLime
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.SurfaceElevated
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary

@Composable
fun WeekDaySelector(
    selectedDayIndex: Int,
    daysOfWeek: List<Pair<Int, Pair<String, String>>>,
    routines: List<Routine>,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        daysOfWeek.forEach { (dayInt, dayLabels) ->
            val (shortLabel, _) = dayLabels
            val isSelected = selectedDayIndex == dayInt
            val hasRoutines = routines.any { it.assignedDays.contains(dayInt) }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onDaySelected(dayInt) }
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isSelected -> NeonLime
                                hasRoutines -> SurfaceElevated
                                else -> SurfaceDark
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                isSelected -> NeonLime
                                hasRoutines -> ElectricCyan.copy(alpha = 0.5f)
                                else -> SurfaceBorder
                            },
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = shortLabel,
                        color = when {
                            isSelected -> Color.Black
                            hasRoutines -> TextPrimary
                            else -> TextMuted
                        },
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                hasRoutines -> ElectricCyan
                                else -> Color.Transparent
                            }
                        )
                )
            }
        }
    }
}
