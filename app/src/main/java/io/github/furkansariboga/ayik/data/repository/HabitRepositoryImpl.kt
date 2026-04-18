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
package io.github.furkansariboga.ayik.data.repository

import io.github.furkansariboga.ayik.data.local.HabitDao
import io.github.furkansariboga.ayik.domain.model.Habit
import io.github.furkansariboga.ayik.domain.model.Relapse
import io.github.furkansariboga.ayik.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val dao: HabitDao
) : HabitRepository {
    override fun getAllHabits(): Flow<List<Habit>> = dao.getAllHabits()

    override fun getHabitById(id: Int): Flow<Habit?> = dao.getHabitById(id)

    override suspend fun insertHabit(habit: Habit) {
        dao.insertHabit(habit)
    }

    override suspend fun updateHabit(habit: Habit) {
        dao.updateHabit(habit)
    }

    override suspend fun deleteHabit(habit: Habit) {
        dao.deleteHabit(habit)
    }

    // Relapse operations
    override suspend fun insertRelapse(relapse: Relapse) {
        dao.insertRelapse(relapse)
    }

    override fun getRelapsesForHabit(habitId: Int): Flow<List<Relapse>> =
        dao.getRelapsesForHabit(habitId)

    override suspend fun deleteRelapsesForHabit(habitId: Int) {
        dao.deleteRelapsesForHabit(habitId)
    }
}
