package com.xplozder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xplozder.data.network.PExchangePriceHistoryModel
import com.xplozder.ui.theme.CyanElectric
import com.xplozder.ui.theme.CyanGlow
import com.xplozder.ui.theme.ProfitGreen
import com.xplozder.ui.theme.SpaceCardBg
import com.xplozder.ui.theme.SpaceCardBorder
import com.xplozder.ui.theme.SpaceSurfaceLight
import com.xplozder.ui.theme.TextMuted
import com.xplozder.ui.theme.TextPrimary
import com.xplozder.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun PriceHistoryChart(
    history: List<PExchangePriceHistoryModel>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) {
        Surface(
            color = SpaceCardBg,
            shape = RoundedCornerShape(10.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No 30-day historical trades recorded for this item yet.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
        return
    }

    // Sort chronologically (oldest to newest for plotting)
    val sortedHistory = history.sortedBy { it.date }
    val pricesDollars = sortedHistory.map { it.avgPrice / 100.0 }
    val minPrice = pricesDollars.minOrNull() ?: 0.0
    val maxPrice = pricesDollars.maxOrNull() ?: 1.0
    val priceRange = if (maxPrice - minPrice > 0.001) maxPrice - minPrice else 1.0

    val latestPrice = pricesDollars.lastOrNull() ?: 0.0
    val firstPrice = pricesDollars.firstOrNull() ?: 0.0
    val priceDiff = latestPrice - firstPrice
    val pctDiff = if (firstPrice > 0) (priceDiff / firstPrice) * 100 else 0.0

    Surface(
        color = SpaceCardBg,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "30-DAY EXCHANGE TREND",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = String.format(Locale.US, "$%.2f", latestPrice),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (priceDiff >= 0) Color(0x2200E676) else Color(0x22FF5252)
                ) {
                    Text(
                        text = String.format(Locale.US, "%s%.1f%% (%+.2f$)", if (priceDiff >= 0) "+" else "", pctDiff, priceDiff),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (priceDiff >= 0) ProfitGreen else Color(0xFFFF5252),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Line Chart
            val chartLineColor = CyanElectric
            val chartGlowColor = CyanGlow
            val isDarkTheme = com.xplozder.ui.theme.LocalIsDarkTheme.current
            val gridLineColor = if (isDarkTheme) Color(0x18FFFFFF) else Color(0x12000000)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val paddingBottom = 16f
                    val chartHeight = h - paddingBottom

                    // Draw grid reference lines
                    val lineCount = 3
                    for (i in 0..lineCount) {
                        val y = chartHeight * (i.toFloat() / lineCount)
                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    if (pricesDollars.size < 2) {
                        val y = chartHeight / 2
                        drawLine(
                            color = chartLineColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                        return@Canvas
                    }

                    val stepX = w / (pricesDollars.size - 1)
                    val points = pricesDollars.mapIndexed { index, price ->
                        val normalized = ((price - minPrice) / priceRange).toFloat().coerceIn(0f, 1f)
                        val y = chartHeight - (normalized * chartHeight)
                        Offset(index * stepX, y)
                    }

                    // Fill Path (gradient under curve)
                    val fillPath = Path().apply {
                        moveTo(points.first().x, chartHeight)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, chartHeight)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                chartLineColor.copy(alpha = 0.35f),
                                chartLineColor.copy(alpha = 0.0f)
                            ),
                            startY = 0f,
                            endY = chartHeight
                        )
                    )

                    // Stroke Path
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
                        color = chartLineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw dot at latest point
                    drawCircle(
                        color = chartGlowColor,
                        radius = 4.dp.toPx(),
                        center = points.last()
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Chart Range Footnote
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = sortedHistory.firstOrNull()?.date ?: "",
                    fontSize = 9.sp,
                    color = TextMuted
                )
                Text(
                    text = String.format(Locale.US, "Low: $%.2f • High: $%.2f", minPrice, maxPrice),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Text(
                    text = sortedHistory.lastOrNull()?.date ?: "Today",
                    fontSize = 9.sp,
                    color = TextMuted
                )
            }
        }
    }
}
