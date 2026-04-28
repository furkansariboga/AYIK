/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetUpdateWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        updateAllWidgets(applicationContext)
        return Result.success()
    }

    companion object {
        suspend fun updateAllWidgets(context: Context) {
            CounterSmallWidget().updateAll(context)
            CounterMediumWidget().updateAll(context)
            HeatmapSmallWidget().updateAll(context)
            HeatmapMediumWidget().updateAll(context)
            HeatmapLargeWidget().updateAll(context)
        }
    }
}
