/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.GlanceTheme
import androidx.glance.text.*
import io.github.furkansariboga.ayik.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class HeatmapLargeWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val habit = WidgetDataHelper.getHabitForWidget(context, appWidgetId)
        val name = habit?.name ?: context.getString(R.string.app_name)
        val elapsed = if (habit != null) System.currentTimeMillis() - habit.lastOccurrenceTimestamp else 0L
        val days = TimeUnit.MILLISECONDS.toDays(elapsed)
        val hours = TimeUnit.MILLISECONDS.toHours(elapsed) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60

        val relapses = if (habit != null) WidgetDataHelper.getRelapsesForHabit(context, habit.id) else emptyList()
        val restTokens = if (habit != null) WidgetDataHelper.getRestTokensForHabit(context, habit.id) else emptyList()

        val bitmap = if (habit != null) {
            val isDark = (context.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            WidgetHeatmapRenderer.render(
                createdTimestamp = habit.createdTimestamp,
                relapses = relapses,
                restTokens = restTokens,
                widthPx = 960,
                heightPx = 320,
                weeks = 52,
                isDarkMode = isDark
            )
        } else null

        provideContent {
            GlanceTheme {
                LargeContent(
                    name = name,
                    days = days,
                    hours = hours,
                    minutes = minutes,
                    relapseCount = relapses.size,
                    bitmap = bitmap
                )
            }
        }
    }
}

@Composable
private fun LargeContent(
    name: String,
    days: Long,
    hours: Long,
    minutes: Long,
    relapseCount: Int,
    bitmap: android.graphics.Bitmap?
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(GlanceTheme.colors.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Header: name + streak | relapse count
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = name,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GlanceTheme.colors.onSurface
                    ),
                    maxLines = 1
                )
                Text(
                    text = "${days}d ${hours}h ${minutes}m",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = GlanceTheme.colors.primary
                    )
                )
            }
            if (relapseCount > 0) {
                Text(
                    text = "× $relapseCount",
                    style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.error)
                )
            }
        }

        // Dot map fills all remaining vertical space
        if (bitmap != null) {
            Spacer(modifier = GlanceModifier.height(6.dp))
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = null,
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                contentScale = ContentScale.FillBounds
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            LegendRow()
        }
    }
}

@Composable
private fun LegendRow() {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(Color(WidgetHeatmapRenderer.cleanColor(false)), "Clean")
        Spacer(modifier = GlanceModifier.width(8.dp))
        LegendDot(Color(WidgetHeatmapRenderer.RELAPSE_COLOR), "Relapse")
        Spacer(modifier = GlanceModifier.width(8.dp))
        LegendDot(Color(WidgetHeatmapRenderer.DIFFICULT_COLOR), "Difficult")
        Spacer(modifier = GlanceModifier.width(8.dp))
        LegendDot(Color(WidgetHeatmapRenderer.PAUSED_COLOR), "Paused")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Box(
        modifier = GlanceModifier
            .size(8.dp)
            .cornerRadius(4.dp)
            .background(color),
        contentAlignment = Alignment.Center
    ) {}
    Spacer(modifier = GlanceModifier.width(3.dp))
    Text(
        text = label,
        style = TextStyle(fontSize = 9.sp, color = GlanceTheme.colors.onSurface)
    )
}

class HeatmapLargeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HeatmapLargeWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                glanceAppWidget.updateAll(context)
            } finally {
                result.finish()
            }
        }
    }
}
