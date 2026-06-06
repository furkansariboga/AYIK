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

class HeatmapMediumWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val habit = WidgetDataHelper.getHabitForWidget(context, appWidgetId)
        val name = habit?.name ?: context.getString(R.string.app_name)
        val elapsed = if (habit != null) System.currentTimeMillis() - habit.lastOccurrenceTimestamp else 0L
        val days = TimeUnit.MILLISECONDS.toDays(elapsed)
        val hours = TimeUnit.MILLISECONDS.toHours(elapsed) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60

        val bitmap = if (habit != null) {
            val relapses = WidgetDataHelper.getRelapsesForHabit(context, habit.id)
            val restTokens = WidgetDataHelper.getRestTokensForHabit(context, habit.id)
            val isDark = (context.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            WidgetHeatmapRenderer.render(
                createdTimestamp = habit.createdTimestamp,
                relapses = relapses,
                restTokens = restTokens,
                widthPx = 960,
                heightPx = 200,
                weeks = 24,
                isDarkMode = isDark
            )
        } else null

        provideContent {
            GlanceTheme {
                MediumContent(name, days, hours, minutes, bitmap)
            }
        }
    }
}

@Composable
private fun MediumContent(
    name: String,
    days: Long,
    hours: Long,
    minutes: Long,
    bitmap: android.graphics.Bitmap?
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(GlanceTheme.colors.surface)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = name,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = GlanceTheme.colors.onSurface
            ),
            maxLines = 1
        )
        Text(
            text = "${days}d ${hours}h ${minutes}m",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = GlanceTheme.colors.primary
            )
        )
        if (bitmap != null) {
            Spacer(modifier = GlanceModifier.height(4.dp))
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = null,
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}

class HeatmapMediumWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HeatmapMediumWidget()

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
