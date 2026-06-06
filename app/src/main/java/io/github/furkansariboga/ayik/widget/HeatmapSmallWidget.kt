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

class HeatmapSmallWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val habit = WidgetDataHelper.getHabitForWidget(context, appWidgetId)
        val name = habit?.name ?: context.getString(R.string.app_name)
        val elapsed = if (habit != null) System.currentTimeMillis() - habit.lastOccurrenceTimestamp else 0L
        val days = TimeUnit.MILLISECONDS.toDays(elapsed)
        val hours = TimeUnit.MILLISECONDS.toHours(elapsed) % 24

        val bitmap = if (habit != null) {
            val relapses = WidgetDataHelper.getRelapsesForHabit(context, habit.id)
            val restTokens = WidgetDataHelper.getRestTokensForHabit(context, habit.id)
            val isDark = (context.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            WidgetHeatmapRenderer.render(
                createdTimestamp = habit.createdTimestamp,
                relapses = relapses,
                restTokens = restTokens,
                widthPx = 720,
                heightPx = 200,
                weeks = 12,
                isDarkMode = isDark
            )
        } else null

        provideContent {
            GlanceTheme {
                SmallContent(name, days, hours, bitmap)
            }
        }
    }
}

@Composable
private fun SmallContent(
    name: String,
    days: Long,
    hours: Long,
    bitmap: android.graphics.Bitmap?
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(GlanceTheme.colors.surface)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = name,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = GlanceTheme.colors.onSurface
            ),
            maxLines = 1
        )
        Text(
            text = "${days}d ${hours}h",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
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

class HeatmapSmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HeatmapSmallWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Grab the PendingResult FIRST — goAsync() can only be called once per
        // broadcast; calling it here prevents GlanceAppWidgetReceiver.onUpdate
        // from consuming it and leaving us with null, which would NPE on finish().
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
