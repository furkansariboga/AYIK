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
package io.github.furkansariboga.ayik.domain.repository

import io.github.furkansariboga.ayik.domain.model.Habit
import io.github.furkansariboga.ayik.domain.model.Milestone
import io.github.furkansariboga.ayik.domain.model.Relapse
import io.github.furkansariboga.ayik.domain.model.RestToken
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    fun getHabitById(id: Int): Flow<Habit?>
    suspend fun getHabitByIdSync(id: Int): Habit?
    suspend fun getAllHabitsSync(): List<Habit>
    suspend fun insertHabit(habit: Habit)
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)

    // Relapse operations
    suspend fun insertRelapse(relapse: Relapse)
    fun getRelapsesForHabit(habitId: Int): Flow<List<Relapse>>
    fun getRelapsesForHabitSince(habitId: Int, sinceTimestamp: Long): Flow<List<Relapse>>
    suspend fun deleteRelapsesForHabit(habitId: Int)

    // Rest Token operations
    suspend fun insertRestToken(restToken: RestToken)
    fun getRestTokensForHabit(habitId: Int): Flow<List<RestToken>>
    fun getRestTokensForHabitBetween(habitId: Int, start: Long, end: Long): Flow<List<RestToken>>
    suspend fun deleteRestTokensForHabit(habitId: Int)

    // Milestone operations
    suspend fun insertMilestone(milestone: Milestone)
    fun getMilestonesForHabit(habitId: Int): Flow<List<Milestone>>
    suspend fun getMilestoneByThreshold(habitId: Int, dayThreshold: Int): Milestone?
    suspend fun deleteMilestonesForHabit(habitId: Int)
}
