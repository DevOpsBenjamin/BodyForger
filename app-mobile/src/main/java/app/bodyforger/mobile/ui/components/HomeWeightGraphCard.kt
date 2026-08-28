package app.bodyforger.mobile.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.bodyforger.mobile.ui.theme.AmberGold
import app.bodyforger.mobile.ui.theme.ElectricCyan
import app.bodyforger.mobile.ui.theme.SurfaceBorder
import app.bodyforger.mobile.ui.theme.SurfaceDark
import app.bodyforger.mobile.ui.theme.TextMuted
import app.bodyforger.mobile.ui.theme.TextPrimary
import app.bodyforger.mobile.ui.theme.TextSecondary

@Composable
fun HomeWeightGraphCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp)),
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
                    Text(
                        text = "COMPOSITION CORPORELLE",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "78.5",
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = " kg",
                            color = TextSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "-0.4 kg ce mois",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Médiane 7j : 78.6 kg",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            SimpleBodyGraphCanvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Pesées réelles", color = TextSecondary, fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height(2.dp)
                            .background(AmberGold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Objectif Palier 1 (75.0 kg)", color = AmberGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun SimpleBodyGraphCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height * (i.toFloat() / gridLines)
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val targetY = height * 0.75f
        drawLine(
            color = AmberGold.copy(alpha = 0.6f),
            start = Offset(0f, targetY),
            end = Offset(width, targetY),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        val points = listOf(
            Offset(width * 0.05f, height * 0.25f),
            Offset(width * 0.20f, height * 0.35f),
            Offset(width * 0.35f, height * 0.30f),
            Offset(width * 0.50f, height * 0.45f),
            Offset(width * 0.65f, height * 0.40f),
            Offset(width * 0.80f, height * 0.55f),
            Offset(width * 0.95f, height * 0.50f)
        )

        val fillPath = Path().apply {
            moveTo(points.first().x, height)
            lineTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val midX = (prev.x + curr.x) / 2
                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
            }
            lineTo(points.last().x, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    ElectricCyan.copy(alpha = 0.25f),
                    ElectricCyan.copy(alpha = 0.0f)
                ),
                startY = 0f,
                endY = height
            )
        )

        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val midX = (prev.x + curr.x) / 2
                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
            }
        }

        drawPath(
            path = strokePath,
            color = ElectricCyan,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        points.forEach { point ->
            drawCircle(
                color = ElectricCyan,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = Color.Black,
                radius = 2.dp.toPx(),
                center = point
            )
        }
    }
}
