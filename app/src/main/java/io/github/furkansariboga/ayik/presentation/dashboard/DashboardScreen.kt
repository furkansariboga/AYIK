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
package io.github.furkansariboga.ayik.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.furkansariboga.ayik.R
import io.github.furkansariboga.ayik.domain.model.Habit
import io.github.furkansariboga.ayik.presentation.HabitViewModel
import io.github.furkansariboga.ayik.util.TimeUtils
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    viewModel: HabitViewModel,
    onAddEntryClick: () -> Unit
) {
    val habits by viewModel.habits.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (habits.isEmpty()) {
            Text(
                text = stringResource(R.string.empty_dashboard),
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(habits) { habit ->
                    HabitItem(habit = habit)
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                    Text(
                        text = stringResource(R.string.disclaimer),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit) {
    val context = LocalContext.current
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(key1 = habit.id) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val elapsedTime = currentTime - habit.lastOccurrenceTimestamp
    val formattedTime = TimeUtils.formatElapsedTime(context, elapsedTime)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
