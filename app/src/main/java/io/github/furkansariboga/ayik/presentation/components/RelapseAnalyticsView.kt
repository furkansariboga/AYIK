/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.furkansariboga.ayik.R
import io.github.furkansariboga.ayik.domain.model.Relapse
import java.util.*

@Composable
fun RelapseAnalyticsView(
    relapses: List<Relapse>,
    modifier: Modifier = Modifier
) {
    if (relapses.isEmpty()) return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Hour of day chart
        Text(
            text = stringResource(R.string.relapses_by_hour),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        HourChart(relapses)

        Spacer(Modifier.height(8.dp))

        // Day of week chart
        Text(
            text = stringResource(R.string.relapses_by_day),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        DayOfWeekChart(relapses)
    }
}

@Composable
private fun HourChart(relapses: List<Relapse>) {
    val hourCounts = remember(relapses) {
        val counts = IntArray(24)
        relapses.forEach { r ->
            val hour = if (r.hourOfDay >= 0) r.hourOfDay else {
                Calendar.getInstance().apply { timeInMillis = r.timestamp }.get(Calendar.HOUR_OF_DAY)
            }
            counts[hour]++
        }
        counts.toList()
    }
    val maxCount = hourCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val barColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                val barWidth = size.width / 24f
                val maxHeight = size.height
                hourCounts.forEachIndexed { i, count ->
                    val barHeight = if (maxCount > 0) (count.toFloat() / maxCount) * maxHeight * 0.9f else 0f
                    drawRoundRect(
                        color = if (count > 0) barColor else bgColor,
                        topLeft = Offset(i * barWidth + 1f, maxHeight - barHeight),
                        size = Size(barWidth - 2f, barHeight.coerceAtLeast(2f)),
                        cornerRadius = CornerRadius(2f)
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("6", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("12", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("18", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("23", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DayOfWeekChart(relapses: List<Relapse>) {
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dayCounts = remember(relapses) {
        val counts = IntArray(7)
        relapses.forEach { r ->
            val dow = if (r.dayOfWeek in 1..7) r.dayOfWeek - 1 else {
                val calDow = Calendar.getInstance().apply { timeInMillis = r.timestamp }.get(Calendar.DAY_OF_WEEK)
                // Convert Calendar.DAY_OF_WEEK (Sun=1..Sat=7) to Mon=0..Sun=6
                when (calDow) { Calendar.MONDAY -> 0; Calendar.TUESDAY -> 1; Calendar.WEDNESDAY -> 2; Calendar.THURSDAY -> 3; Calendar.FRIDAY -> 4; Calendar.SATURDAY -> 5; else -> 6 }
            }
            counts[dow]++
        }
        counts.toList()
    }
    val maxCount = dayCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val barColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            dayCounts.forEachIndexed { i, count ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.width(24.dp).height(60.dp), contentAlignment = Alignment.BottomCenter) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val barHeight = if (maxCount > 0) (count.toFloat() / maxCount) * size.height * 0.9f else 0f
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(2f, size.height - barHeight),
                                size = Size(size.width - 4f, barHeight.coerceAtLeast(2f)),
                                cornerRadius = CornerRadius(3f)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(dayNames[i], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (count > 0) Text("$count", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
