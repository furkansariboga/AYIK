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
package io.github.furkansariboga.ayik.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.furkansariboga.ayik.domain.model.Habit
import io.github.furkansariboga.ayik.domain.model.Relapse
import io.github.furkansariboga.ayik.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    val habits: StateFlow<List<Habit>> = repository.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHabit(name: String, timestamp: Long, dailyCost: Double = 0.0) {
        viewModelScope.launch {
            repository.insertHabit(
                Habit(
                    name = name,
                    lastOccurrenceTimestamp = timestamp,
                    dailyCost = dailyCost,
                    createdTimestamp = timestamp
                )
            )
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun addRelapse(habit: Habit) {
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            // Record the relapse
            repository.insertRelapse(Relapse(habitId = habit.id, timestamp = now))
            // Update the habit's lastOccurrenceTimestamp so the counter resets
            repository.updateHabit(habit.copy(lastOccurrenceTimestamp = now))
        }
    }

    fun getRelapsesForHabit(habitId: Int): Flow<List<Relapse>> {
        return repository.getRelapsesForHabit(habitId)
    }

    fun getHabitById(id: Int): Flow<Habit?> {
        return repository.getHabitById(id)
    }
}
