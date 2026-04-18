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

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.furkansariboga.ayik.domain.model.Habit
import io.github.furkansariboga.ayik.domain.model.Relapse

@Database(entities = [Habit::class, Relapse::class], version = 2, exportSchema = false)
abstract class HabitDatabase : RoomDatabase() {
    abstract val habitDao: HabitDao

    companion object {
        const val DATABASE_NAME = "ayik_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to habits table
                db.execSQL("ALTER TABLE habits ADD COLUMN dailyCost REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE habits ADD COLUMN createdTimestamp INTEGER NOT NULL DEFAULT 0")
                // Set createdTimestamp to lastOccurrenceTimestamp for existing records
                db.execSQL("UPDATE habits SET createdTimestamp = lastOccurrenceTimestamp WHERE createdTimestamp = 0")

                // Create relapses table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS relapses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        habitId INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        FOREIGN KEY(habitId) REFERENCES habits(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_relapses_habitId ON relapses(habitId)")
            }
        }
    }
}
