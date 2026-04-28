/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.furkansariboga.ayik.domain.model.Relapse
import io.github.furkansariboga.ayik.domain.model.RestToken
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalTextApi::class)
@Composable
fun HeatmapView(
    createdTimestamp: Long,
    relapses: List<Relapse>,
    restTokens: List<RestToken>,
    modifier: Modifier = Modifier,
    weeks: Int = 52
) {
    val cleanColor = MaterialTheme.colorScheme.primary
    val cleanColorLight = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    val relapseColor = Color(0xFFEF5350)
    val restTokenColor = Color(0xFFFFCA28)
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    val cellSize = 14.dp
    val cellSpacing = 2.dp
    val leftLabelWidth = 20.dp
    val topLabelHeight = 16.dp
    val density = LocalDensity.current

    // Build day status map
    val dayStatusMap = remember(relapses, restTokens, createdTimestamp) {
        buildDayStatusMap(createdTimestamp, relapses, restTokens)
    }

    val totalWidth = leftLabelWidth + (cellSize + cellSpacing) * weeks
    val totalHeight = topLabelHeight + (cellSize + cellSpacing) * 7

    val scrollState = rememberScrollState(Int.MAX_VALUE) // scroll to end (most recent)

    Box(modifier = modifier.horizontalScroll(scrollState)) {
        Canvas(
            modifier = Modifier
                .width(with(density) { totalWidth.toPx().toDp() })
                .height(with(density) { totalHeight.toPx().toDp() })
        ) {
            val cellSizePx = cellSize.toPx()
            val cellSpacingPx = cellSpacing.toPx()
            val leftLabelPx = leftLabelWidth.toPx()
            val topLabelPx = topLabelHeight.toPx()

            // Calculate start date (weeks ago from today)
            val cal = Calendar.getInstance()
            val today = cal.clone() as Calendar
            cal.add(Calendar.WEEK_OF_YEAR, -(weeks - 1))
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)

            // Draw day labels (M, W, F)
            val dayLabels = listOf("" to 0, "M" to 1, "" to 2, "W" to 3, "" to 4, "F" to 5, "" to 6)
            dayLabels.forEach { (label, dayIndex) ->
                if (label.isNotEmpty()) {
                    val y = topLabelPx + dayIndex * (cellSizePx + cellSpacingPx) + cellSizePx / 2
                    val textResult = textMeasurer.measure(
                        AnnotatedString(label),
                        style = TextStyle(fontSize = 8.sp, color = textColor)
                    )
                    drawText(textResult, topLeft = Offset(0f, y - textResult.size.height / 2))
                }
            }

            // Draw cells
            var prevMonth = -1
            for (week in 0 until weeks) {
                for (day in 0..6) {
                    val currentCal = cal.clone() as Calendar
                    currentCal.add(Calendar.WEEK_OF_YEAR, week)
                    currentCal.set(Calendar.DAY_OF_WEEK, currentCal.firstDayOfWeek)
                    currentCal.add(Calendar.DAY_OF_YEAR, day)

                    val x = leftLabelPx + week * (cellSizePx + cellSpacingPx)
                    val y = topLabelPx + day * (cellSizePx + cellSpacingPx)

                    // Month label at top
                    if (day == 0) {
                        val month = currentCal.get(Calendar.MONTH)
                        if (month != prevMonth) {
                            prevMonth = month
                            val monthName = currentCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""
                            val textResult = textMeasurer.measure(
                                AnnotatedString(monthName),
                                style = TextStyle(fontSize = 8.sp, color = textColor)
                            )
                            drawText(textResult, topLeft = Offset(x, 0f))
                        }
                    }

                    // Determine cell color
                    val dayKey = dayKey(currentCal)
                    val isFuture = currentCal.after(today)
                    val isBeforeCreation = currentCal.timeInMillis < createdTimestamp - TimeUnit.DAYS.toMillis(1)

                    val color = when {
                        isFuture || isBeforeCreation -> emptyColor
                        dayStatusMap[dayKey] == DayStatus.RELAPSE -> relapseColor
                        dayStatusMap[dayKey] == DayStatus.REST_TOKEN -> restTokenColor
                        dayStatusMap[dayKey] == DayStatus.CLEAN -> cleanColor
                        else -> cleanColorLight
                    }

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(cellSizePx, cellSizePx),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                }
            }
        }
    }
}

private enum class DayStatus { CLEAN, RELAPSE, REST_TOKEN }

private fun dayKey(cal: Calendar): Long {
    val c = cal.clone() as Calendar
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

private fun buildDayStatusMap(
    createdTimestamp: Long,
    relapses: List<Relapse>,
    restTokens: List<RestToken>
): Map<Long, DayStatus> {
    val map = mutableMapOf<Long, DayStatus>()

    // Mark relapse days
    relapses.forEach { relapse ->
        val cal = Calendar.getInstance().apply { timeInMillis = relapse.timestamp }
        map[dayKey(cal)] = DayStatus.RELAPSE
    }

    // Mark rest token days (don't overwrite relapses)
    restTokens.forEach { token ->
        val cal = Calendar.getInstance().apply { timeInMillis = token.timestamp }
        val key = dayKey(cal)
        if (map[key] != DayStatus.RELAPSE) {
            map[key] = DayStatus.REST_TOKEN
        }
    }

    // Fill in clean days
    val startCal = Calendar.getInstance().apply {
        timeInMillis = createdTimestamp
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val endCal = Calendar.getInstance()

    val current = startCal.clone() as Calendar
    while (!current.after(endCal)) {
        val key = dayKey(current)
        if (!map.containsKey(key)) {
            map[key] = DayStatus.CLEAN
        }
        current.add(Calendar.DAY_OF_YEAR, 1)
    }

    return map
}
