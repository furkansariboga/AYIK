/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import io.github.furkansariboga.ayik.R
import java.util.concurrent.TimeUnit

class CounterMediumWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val habit = WidgetDataHelper.getHabitForWidget(context, appWidgetId)
        val name = habit?.name ?: context.getString(R.string.app_name)
        val elapsed = if (habit != null) System.currentTimeMillis() - habit.lastOccurrenceTimestamp else 0L
        val days = TimeUnit.MILLISECONDS.toDays(elapsed)
        val hours = TimeUnit.MILLISECONDS.toHours(elapsed) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60

        provideContent { CounterMediumContent(name, days, hours, minutes) }
    }
}

@Composable
private fun CounterMediumContent(name: String, days: Long, hours: Long, minutes: Long) {
    Row(
        modifier = GlanceModifier.fillMaxSize().padding(12.dp).cornerRadius(16.dp)
            .background(androidx.glance.R.color.glance_colorBackground),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight(), horizontalAlignment = Alignment.Start) {
            Text(
                text = name,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ColorProvider(R.color.black)),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "${days}d ${hours}h ${minutes}m",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, color = ColorProvider(R.color.purple_500))
            )
        }
    }
}

class CounterMediumWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CounterMediumWidget()
}
