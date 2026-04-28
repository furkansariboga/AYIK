/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.glance.appwidget.updateAll
import io.github.furkansariboga.ayik.domain.model.Habit
import io.github.furkansariboga.ayik.ui.theme.AYIKTheme
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            AYIKTheme {
                WidgetConfigScreen(
                    context = this,
                    appWidgetId = appWidgetId,
                    onHabitSelected = {
                        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        setResult(RESULT_OK, resultValue)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigScreen(context: Context, appWidgetId: Int, onHabitSelected: (Int) -> Unit) {
    var habits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        habits = WidgetDataHelper.getAllHabits(context)
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Select Habit", fontWeight = FontWeight.SemiBold) }) }) { innerPadding ->
        if (habits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No habits tracked yet. Add one in the app first.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(habits) { habit ->
                    Card(modifier = Modifier.fillMaxWidth().clickable {
                        scope.launch {
                            context.widgetDataStore.edit { prefs -> prefs[WidgetDataHelper.habitIdKey(appWidgetId)] = habit.id }
                            // Force update all widget types so selected habit data shows immediately
                            CounterSmallWidget().updateAll(context)
                            CounterMediumWidget().updateAll(context)
                            HeatmapSmallWidget().updateAll(context)
                            HeatmapMediumWidget().updateAll(context)
                            HeatmapLargeWidget().updateAll(context)
                            onHabitSelected(habit.id)
                        }
                    }) {
                        Text(habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}
