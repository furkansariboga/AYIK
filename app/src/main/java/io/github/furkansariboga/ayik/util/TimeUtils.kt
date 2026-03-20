/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package io.github.furkansariboga.ayik.util

import android.content.Context
import io.github.furkansariboga.ayik.R
import java.util.concurrent.TimeUnit

object TimeUtils {
    fun formatElapsedTime(context: Context, millis: Long): String {
        val days = TimeUnit.MILLISECONDS.toDays(millis)
        val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

        return StringBuilder().apply {
            if (days > 0) append("${days}${context.getString(R.string.days)} ")
            if (hours > 0 || days > 0) append("${hours}${context.getString(R.string.hours)} ")
            if (minutes > 0 || hours > 0 || days > 0) append("${minutes}${context.getString(R.string.minutes)} ")
            append("${seconds}${context.getString(R.string.seconds)}")
        }.toString()
    }
}
