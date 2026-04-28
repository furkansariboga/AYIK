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
package io.github.furkansariboga.ayik.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.furkansariboga.ayik.domain.model.Habit
import io.github.furkansariboga.ayik.domain.model.Milestone
import io.github.furkansariboga.ayik.domain.model.Relapse
import io.github.furkansariboga.ayik.domain.model.RestToken
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY lastOccurrenceTimestamp DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabitById(id: Int): Flow<Habit?>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitByIdSync(id: Int): Habit?

    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsSync(): List<Habit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    // Relapse operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelapse(relapse: Relapse)

    @Query("SELECT * FROM relapses WHERE habitId = :habitId ORDER BY timestamp DESC")
    fun getRelapsesForHabit(habitId: Int): Flow<List<Relapse>>

    @Query("SELECT * FROM relapses WHERE habitId = :habitId AND timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getRelapsesForHabitSince(habitId: Int, sinceTimestamp: Long): Flow<List<Relapse>>

    @Query("DELETE FROM relapses WHERE habitId = :habitId")
    suspend fun deleteRelapsesForHabit(habitId: Int)

    // Rest Token operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestToken(restToken: RestToken)

    @Query("SELECT * FROM rest_tokens WHERE habitId = :habitId ORDER BY timestamp DESC")
    fun getRestTokensForHabit(habitId: Int): Flow<List<RestToken>>

    @Query("SELECT * FROM rest_tokens WHERE habitId = :habitId AND timestamp >= :start AND timestamp <= :end ORDER BY timestamp DESC")
    fun getRestTokensForHabitBetween(habitId: Int, start: Long, end: Long): Flow<List<RestToken>>

    @Query("DELETE FROM rest_tokens WHERE habitId = :habitId")
    suspend fun deleteRestTokensForHabit(habitId: Int)

    // Milestone operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: Milestone)

    @Query("SELECT * FROM milestones WHERE habitId = :habitId ORDER BY dayThreshold ASC")
    fun getMilestonesForHabit(habitId: Int): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones WHERE habitId = :habitId AND dayThreshold = :dayThreshold LIMIT 1")
    suspend fun getMilestoneByThreshold(habitId: Int, dayThreshold: Int): Milestone?

    @Query("DELETE FROM milestones WHERE habitId = :habitId")
    suspend fun deleteMilestonesForHabit(habitId: Int)
}
