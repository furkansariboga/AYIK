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

/**
 * Dynamic milestone generation:
 * - Days 1–14: every day
 * - Weeks 3–8 (days 21–56): every week
 * - Months 3–12 (days 90–365): every month (~30 days)
 * - Years 2+: every year (365 days) — extends dynamically
 */
object MilestoneUtils {

    data class MilestoneDefinition(
        val days: Int,
        val label: String,
        val emoji: String
    )

    /**
     * Generate all milestone definitions up to and including the given number of days.
     * This is dynamic — it will produce yearly milestones for however many years the user has been clean.
     */
    fun generateMilestones(upToDays: Int): List<MilestoneDefinition> {
        val milestones = mutableListOf<MilestoneDefinition>()

        // Days 1–14: every day
        for (d in 1..14) {
            val emoji = when (d) {
                1 -> "🌱"
                7 -> "🔥"
                14 -> "💪"
                else -> "⭐"
            }
            val label = when (d) {
                1 -> "1 Day"
                7 -> "1 Week"
                else -> "$d Days"
            }
            milestones.add(MilestoneDefinition(d, label, emoji))
        }

        // Weeks 3–8 (days 21, 28, 35, 42, 49, 56)
        for (w in 3..8) {
            val d = w * 7
            val emoji = when (w) {
                4 -> "🏅"
                8 -> "🎯"
                else -> "🌟"
            }
            val label = "$w Weeks"
            milestones.add(MilestoneDefinition(d, label, emoji))
        }

        // Months 3–12 (approx 90, 120, 150, 180, 210, 240, 270, 300, 330, 365)
        for (m in 3..12) {
            val d = if (m == 12) 365 else m * 30
            val emoji = when (m) {
                3 -> "🥉"
                6 -> "🥈"
                12 -> "👑"
                else -> "🏆"
            }
            val label = if (m == 12) "1 Year" else "$m Months"
            milestones.add(MilestoneDefinition(d, label, emoji))
        }

        // Years 2+: dynamically extend
        var year = 2
        while (year * 365 <= upToDays + 365) { // generate one year ahead
            val d = year * 365
            val emoji = when {
                year == 2 -> "💎"
                year == 5 -> "🌈"
                year == 10 -> "🏰"
                year % 5 == 0 -> "✨"
                else -> "👑"
            }
            milestones.add(MilestoneDefinition(d, "$year Years", emoji))
            year++
        }

        return milestones.filter { it.days <= upToDays + 365 }
    }

    /**
     * Get milestones that should be awarded for a given number of clean days.
     */
    fun getAchievedMilestones(cleanDays: Int): List<MilestoneDefinition> {
        return generateMilestones(cleanDays).filter { it.days <= cleanDays }
    }

    /**
     * Get the next upcoming milestone for a given number of clean days.
     */
    fun getNextMilestone(cleanDays: Int): MilestoneDefinition? {
        return generateMilestones(cleanDays + 365).firstOrNull { it.days > cleanDays }
    }
}
