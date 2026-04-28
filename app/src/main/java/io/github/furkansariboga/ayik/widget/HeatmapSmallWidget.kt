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

class HeatmapSmallWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val habit = WidgetDataHelper.getHabitForWidget(context, appWidgetId)
        val name = habit?.name ?: context.getString(R.string.app_name)
        val elapsed = if (habit != null) System.currentTimeMillis() - habit.lastOccurrenceTimestamp else 0L
        val days = TimeUnit.MILLISECONDS.toDays(elapsed)
        val hours = TimeUnit.MILLISECONDS.toHours(elapsed) % 24

        val heatmapBitmap = if (habit != null) {
            val relapses = WidgetDataHelper.getRelapsesForHabit(context, habit.id)
            val restTokens = WidgetDataHelper.getRestTokensForHabit(context, habit.id)
            val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            WidgetHeatmapRenderer.renderHeatmap(context, habit.createdTimestamp, relapses, restTokens, 180, 80, weeks = 52, isDarkMode = isDark)
        } else null

        provideContent { 
            GlanceTheme {
                HeatmapSmallContent(name, days, hours, heatmapBitmap) 
            }
        }
    }
}

@Composable
private fun HeatmapSmallContent(name: String, days: Long, hours: Long, heatmapBitmap: android.graphics.Bitmap?) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(8.dp).cornerRadius(16.dp)
            .background(GlanceTheme.colors.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GlanceTheme.colors.onSurface), maxLines = 1)
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(text = "${days}d ${hours}h", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GlanceTheme.colors.primary))
        Spacer(modifier = GlanceModifier.height(4.dp))
        if (heatmapBitmap != null) {
            Image(provider = ImageProvider(heatmapBitmap), contentDescription = "Heatmap", modifier = GlanceModifier.fillMaxWidth().height(50.dp))
        }
    }
}

class HeatmapSmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HeatmapSmallWidget()
}
