/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

object WidgetUpdateManager {
    private const val WORK_NAME = "widget_update_work"
    private const val ALARM_REQUEST_CODE = 9001

    fun scheduleUpdates(context: Context, intervalMinutes: Int) {
        val workManager = WorkManager.getInstance(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val alarmIntent = Intent(context, WidgetAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, ALARM_REQUEST_CODE, alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (intervalMinutes < 15) {
            // Cancel WorkManager if active
            workManager.cancelUniqueWork(WORK_NAME)
            
            // Schedule AlarmManager (repeating exact, though OS may still throttle)
            val intervalMillis = intervalMinutes * 60 * 1000L
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + intervalMillis,
                intervalMillis,
                pendingIntent
            )
        } else {
            // Cancel AlarmManager if active
            alarmManager.cancel(pendingIntent)
            
            // Schedule WorkManager
            val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                intervalMinutes.toLong(), TimeUnit.MINUTES
            ).setConstraints(Constraints.Builder().build()).build()
            
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
        
        // Also force an immediate update
        forceUpdate(context)
    }

    fun forceUpdate(context: Context) {
        val intent = Intent(context, WidgetAlarmReceiver::class.java).apply {
            action = WidgetAlarmReceiver.ACTION_FORCE_UPDATE
        }
        context.sendBroadcast(intent)
    }
}
