/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import io.github.furkansariboga.ayik.domain.model.Relapse
import io.github.furkansariboga.ayik.domain.model.RestToken
import java.util.Calendar

object WidgetHeatmapRenderer {

    enum class DayType { EMPTY, CLEAN, RELAPSE, DIFFICULT, PAUSED }

    fun buildDayTypeMap(
        createdTimestamp: Long,
        relapses: List<Relapse>,
        restTokens: List<RestToken>
    ): Map<Long, DayType> {
        val map = mutableMapOf<Long, DayType>()

        relapses.forEach { r -> map[normalizeDay(r.timestamp)] = DayType.RELAPSE }

        restTokens.forEach { t ->
            val key = normalizeDay(t.timestamp)
            if (map[key] != DayType.RELAPSE) {
                map[key] = if (t.type == "PAUSED") DayType.PAUSED else DayType.DIFFICULT
            }
        }

        val start = Calendar.getInstance().apply {
            timeInMillis = createdTimestamp
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val today = todayCal()
        val cur = start.clone() as Calendar
        while (!cur.after(today)) {
            val key = cur.timeInMillis
            if (!map.containsKey(key)) map[key] = DayType.CLEAN
            cur.add(Calendar.DAY_OF_YEAR, 1)
        }

        return map
    }

    /**
     * Renders a dot-map bitmap that fills [widthPx]×[heightPx] exactly — no padding,
     * no centering. cellW and cellH are computed independently so the grid always
     * occupies every pixel. Pair with ContentScale.FillBounds in the widget Image.
     */
    fun render(
        createdTimestamp: Long,
        relapses: List<Relapse>,
        restTokens: List<RestToken>,
        widthPx: Int,
        heightPx: Int,
        weeks: Int,
        isDarkMode: Boolean
    ): Bitmap {
        val dayTypeMap = buildDayTypeMap(createdTimestamp, relapses, restTokens)
        return renderFromMap(dayTypeMap, widthPx, heightPx, weeks, isDarkMode)
    }

    fun renderFromMap(
        dayTypeMap: Map<Long, DayType>,
        widthPx: Int,
        heightPx: Int,
        weeks: Int,
        isDarkMode: Boolean
    ): Bitmap {
        val w = widthPx.coerceAtLeast(4)
        val h = heightPx.coerceAtLeast(4)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        // Gap is 15 % of the cell in each axis — computed independently
        // so the grid fills the bitmap with zero dead space.
        val gapRatio = 0.15f
        val cellW = w.toFloat() / (weeks + (weeks - 1) * gapRatio)
        val cellH = h.toFloat() / (7 + 6 * gapRatio)
        val gapX = cellW * gapRatio
        val gapY = cellH * gapRatio
        val cornerR = minOf(cellW, cellH) * 0.22f

        val today = todayCal()
        val startCal = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, -(weeks - 1))
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }

        for (week in 0 until weeks) {
            for (day in 0..6) {
                val cur = startCal.clone() as Calendar
                cur.add(Calendar.WEEK_OF_YEAR, week)
                cur.set(Calendar.DAY_OF_WEEK, cur.firstDayOfWeek)
                cur.add(Calendar.DAY_OF_YEAR, day)
                cur.set(Calendar.HOUR_OF_DAY, 0); cur.set(Calendar.MINUTE, 0)
                cur.set(Calendar.SECOND, 0); cur.set(Calendar.MILLISECOND, 0)

                paint.color = when {
                    cur.after(today) -> emptyColor(isDarkMode)
                    else -> when (dayTypeMap[cur.timeInMillis]) {
                        DayType.CLEAN     -> cleanColor(isDarkMode)
                        DayType.RELAPSE   -> RELAPSE_COLOR
                        DayType.DIFFICULT -> DIFFICULT_COLOR
                        DayType.PAUSED    -> PAUSED_COLOR
                        else              -> emptyColor(isDarkMode)
                    }
                }

                val x = week * (cellW + gapX)
                val y = day  * (cellH + gapY)
                canvas.drawRoundRect(RectF(x, y, x + cellW, y + cellH), cornerR, cornerR, paint)
            }
        }

        return bitmap
    }

    private fun normalizeDay(ts: Long): Long = Calendar.getInstance().apply {
        timeInMillis = ts
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun todayCal(): Calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    fun cleanColor(dark: Boolean) = if (dark) 0xFFD0BCFF.toInt() else 0xFF6650A4.toInt()
    fun emptyColor(dark: Boolean) = if (dark) 0xFF2A2A2A.toInt() else 0xFFE8E8E8.toInt()

    val RELAPSE_COLOR:   Int = 0xFFEF5350.toInt()
    val DIFFICULT_COLOR: Int = 0xFFFFCA28.toInt()
    val PAUSED_COLOR:    Int = 0xFF4FC3F7.toInt()
}
