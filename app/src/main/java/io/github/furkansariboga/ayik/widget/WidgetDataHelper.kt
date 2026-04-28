/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.widget

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import io.github.furkansariboga.ayik.data.local.HabitDatabase
import io.github.furkansariboga.ayik.domain.model.Habit
import io.github.furkansariboga.ayik.domain.model.Relapse
import io.github.furkansariboga.ayik.domain.model.RestToken
import kotlinx.coroutines.flow.first

val Context.widgetDataStore by preferencesDataStore(name = "widget_prefs")

object WidgetDataHelper {

    fun habitIdKey(appWidgetId: Int) = intPreferencesKey("widget_habit_$appWidgetId")

    suspend fun getHabitForWidget(context: Context, appWidgetId: Int): Habit? {
        val prefs = context.widgetDataStore.data.first()
        val habitId = prefs[habitIdKey(appWidgetId)] ?: return null
        val db = getDatabase(context)
        return db.habitDao.getHabitByIdSync(habitId)
    }

    suspend fun getAllHabits(context: Context): List<Habit> {
        val db = getDatabase(context)
        return db.habitDao.getAllHabitsSync()
    }

    suspend fun getRelapsesForHabit(context: Context, habitId: Int): List<Relapse> {
        val db = getDatabase(context)
        return db.habitDao.getRelapsesForHabit(habitId).first()
    }

    suspend fun getRestTokensForHabit(context: Context, habitId: Int): List<RestToken> {
        val db = getDatabase(context)
        return db.habitDao.getRestTokensForHabit(habitId).first()
    }

    private var dbInstance: HabitDatabase? = null

    @Synchronized
    private fun getDatabase(context: Context): HabitDatabase {
        return dbInstance ?: Room.databaseBuilder(
            context.applicationContext,
            HabitDatabase::class.java,
            HabitDatabase.DATABASE_NAME
        ).addMigrations(HabitDatabase.MIGRATION_1_2, HabitDatabase.MIGRATION_2_3)
            .build().also { dbInstance = it }
    }
}
