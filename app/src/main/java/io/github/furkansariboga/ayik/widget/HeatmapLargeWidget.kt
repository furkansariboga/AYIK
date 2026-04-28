/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.*
import io.github.furkansariboga.ayik.R
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

        val heatmapBitmap = if (habit != null) {
            val relapses = WidgetDataHelper.getRelapsesForHabit(context, habit.id)
            val restTokens = WidgetDataHelper.getRestTokensForHabit(context, habit.id)
            val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            WidgetHeatmapRenderer.renderHeatmap(context, habit.createdTimestamp, relapses, restTokens, 320, 120, weeks = 52, isDarkMode = isDark)
        } else null

        provideContent { 
            GlanceTheme {
                HeatmapLargeContent(name, days, hours, minutes, heatmapBitmap) 
            }
        }
    }
}

@Composable
private fun HeatmapLargeContent(name: String, days: Long, hours: Long, minutes: Long, heatmapBitmap: android.graphics.Bitmap?) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(12.dp).cornerRadius(16.dp)
            .background(GlanceTheme.colors.surface)
    ) {
        Text(text = name, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GlanceTheme.colors.onSurface), maxLines = 1)
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(text = "${days}d ${hours}h ${minutes}m", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, color = GlanceTheme.colors.primary))
        Spacer(modifier = GlanceModifier.height(8.dp))
        if (heatmapBitmap != null) {
            Image(provider = ImageProvider(heatmapBitmap), contentDescription = "Heatmap", modifier = GlanceModifier.fillMaxWidth().defaultWeight())
        }
    }
}

class HeatmapLargeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HeatmapLargeWidget()
}
