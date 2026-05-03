/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.furkansariboga.ayik.domain.model.Habit
import io.github.furkansariboga.ayik.domain.model.Milestone
import io.github.furkansariboga.ayik.domain.model.Relapse
import io.github.furkansariboga.ayik.domain.model.RestToken
import io.github.furkansariboga.ayik.domain.repository.HabitRepository
import io.github.furkansariboga.ayik.util.MilestoneNotificationHelper
import io.github.furkansariboga.ayik.util.MilestoneUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val application: Application
) : ViewModel() {

    val habits: StateFlow<List<Habit>> = repository.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHabit(name: String, timestamp: Long, dailyCost: Double = 0.0, dailyTimeMinutes: Int = 0) {
        viewModelScope.launch {
            repository.insertHabit(
                Habit(
                    name = name,
                    lastOccurrenceTimestamp = timestamp,
                    dailyCost = dailyCost,
                    createdTimestamp = timestamp,
                    dailyTimeMinutes = dailyTimeMinutes
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

    fun addRelapse(habit: Habit, journalNote: String = "", trigger: String = "") {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1; Calendar.TUESDAY -> 2; Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4; Calendar.FRIDAY -> 5; Calendar.SATURDAY -> 6
            else -> 7 // Sunday
        }
        viewModelScope.launch {
            repository.insertRelapse(
                Relapse(
                    habitId = habit.id,
                    timestamp = now,
                    journalNote = journalNote,
                    trigger = trigger,
                    hourOfDay = hourOfDay,
                    dayOfWeek = dayOfWeek
                )
            )
            repository.updateHabit(habit.copy(lastOccurrenceTimestamp = now))
        }
    }

    fun addRestToken(habitId: Int, type: String, note: String = "") {
        viewModelScope.launch {
            repository.insertRestToken(
                RestToken(
                    habitId = habitId,
                    type = type,
                    note = note
                )
            )
        }
    }

    fun getRelapsesForHabit(habitId: Int): Flow<List<Relapse>> {
        return repository.getRelapsesForHabit(habitId)
    }

    fun getRestTokensForHabit(habitId: Int): Flow<List<RestToken>> {
        return repository.getRestTokensForHabit(habitId)
    }

    fun getMilestonesForHabit(habitId: Int): Flow<List<Milestone>> {
        return repository.getMilestonesForHabit(habitId)
    }

    fun getHabitById(id: Int): Flow<Habit?> {
        return repository.getHabitById(id)
    }

    fun checkAndAwardMilestones(habit: Habit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val cleanDays = TimeUnit.MILLISECONDS.toDays(now - habit.createdTimestamp).toInt()
            val achievedDefs = MilestoneUtils.getAchievedMilestones(application, cleanDays)

            for (def in achievedDefs) {
                val existing = repository.getMilestoneByThreshold(habit.id, def.days)
                if (existing == null) {
                    repository.insertMilestone(
                        Milestone(
                            habitId = habit.id,
                            dayThreshold = def.days,
                            label = def.label,
                            emoji = def.emoji,
                            achievedTimestamp = now
                        )
                    )
                    // Send notification
                    try {
                        MilestoneNotificationHelper.showMilestoneNotification(
                            context = application,
                            habitName = habit.name,
                            milestoneEmoji = def.emoji,
                            milestoneLabel = def.label,
                            notificationId = habit.id * 1000 + def.days
                        )
                    } catch (_: Exception) {
                        // Notification permission may not be granted
                    }
                }
            }
        }
    }
}
