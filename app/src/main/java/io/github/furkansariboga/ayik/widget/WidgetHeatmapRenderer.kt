/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import io.github.furkansariboga.ayik.domain.model.Relapse
import io.github.furkansariboga.ayik.domain.model.RestToken
import java.util.*
import java.util.concurrent.TimeUnit

object WidgetHeatmapRenderer {

    fun renderHeatmap(
        context: Context,
        createdTimestamp: Long,
        relapses: List<Relapse>,
        restTokens: List<RestToken>,
        widthDp: Int,
        heightDp: Int,
        weeks: Int = 12,
        isDarkMode: Boolean = false
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val width = (widthDp * density).toInt()
        val height = (heightDp * density).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val cellSize = (width.toFloat() / weeks / 1.15f).coerceAtMost(height / 7f / 1.15f)
        val spacing = cellSize * 0.15f

        // Colors
        val cleanColor = if (isDarkMode) 0xFF7C4DFF.toInt() else 0xFF6200EE.toInt()
        val cleanColorLight = if (isDarkMode) 0x507C4DFF.toInt() else 0x506200EE.toInt()
        val relapseColor = 0xFFEF5350.toInt()
        val restTokenColor = 0xFFFFCA28.toInt()
        val emptyColor = if (isDarkMode) 0xFF303030.toInt() else 0xFFE0E0E0.toInt()

        val paint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }

        // Build day status map
        val relapseDays = mutableSetOf<Long>()
        relapses.forEach { r ->
            val cal = Calendar.getInstance().apply { timeInMillis = r.timestamp; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
            relapseDays.add(cal.timeInMillis)
        }
        val restDays = mutableSetOf<Long>()
        restTokens.forEach { t ->
            val cal = Calendar.getInstance().apply { timeInMillis = t.timestamp; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
            restDays.add(cal.timeInMillis)
        }

        val cal = Calendar.getInstance()
        val today = cal.clone() as Calendar
        cal.add(Calendar.WEEK_OF_YEAR, -(weeks - 1))
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)

        for (week in 0 until weeks) {
            for (day in 0..6) {
                val currentCal = cal.clone() as Calendar
                currentCal.add(Calendar.WEEK_OF_YEAR, week)
                currentCal.set(Calendar.DAY_OF_WEEK, currentCal.firstDayOfWeek)
                currentCal.add(Calendar.DAY_OF_YEAR, day)

                val x = week * (cellSize + spacing)
                val y = day * (cellSize + spacing)

                currentCal.set(Calendar.HOUR_OF_DAY, 0); currentCal.set(Calendar.MINUTE, 0)
                currentCal.set(Calendar.SECOND, 0); currentCal.set(Calendar.MILLISECOND, 0)
                val dayKey = currentCal.timeInMillis
                val isFuture = currentCal.after(today)
                val isBeforeCreation = currentCal.timeInMillis < createdTimestamp - TimeUnit.DAYS.toMillis(1)

                paint.color = when {
                    isFuture || isBeforeCreation -> emptyColor
                    relapseDays.contains(dayKey) -> relapseColor
                    restDays.contains(dayKey) -> restTokenColor
                    dayKey >= createdTimestamp - TimeUnit.DAYS.toMillis(1) -> cleanColor
                    else -> cleanColorLight
                }

                canvas.drawRoundRect(RectF(x, y, x + cellSize, y + cellSize), 3f, 3f, paint)
            }
        }

        return bitmap
    }
}
