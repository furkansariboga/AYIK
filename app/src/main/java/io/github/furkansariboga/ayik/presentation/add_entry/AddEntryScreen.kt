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
package io.github.furkansariboga.ayik.presentation.add_entry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.furkansariboga.ayik.R
import io.github.furkansariboga.ayik.presentation.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    viewModel: HabitViewModel,
    onNavigateBack: () -> Unit,
    habitId: Int? = null
) {
    val isEditMode = habitId != null && habitId != -1

    // Load existing habit for edit mode
    val habitFlow = remember(habitId) {
        if (isEditMode) viewModel.getHabitById(habitId!!) else null
    }
    val existingHabit by habitFlow?.collectAsState(initial = null) ?: remember { mutableStateOf(null) }

    var habitName by remember { mutableStateOf("") }
    val calendar = remember { Calendar.getInstance() }
    var selectedTimestamp by remember { mutableLongStateOf(calendar.timeInMillis) }
    var dailyCostText by remember { mutableStateOf("") }
    var dailyTimeMinutesText by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Pre-fill fields in edit mode
    // Use createdTimestamp for the date field, NOT lastOccurrenceTimestamp
    // This ensures the start date is preserved even after relapses
    LaunchedEffect(existingHabit) {
        existingHabit?.let { habit ->
            if (!initialized) {
                habitName = habit.name
                selectedTimestamp = habit.createdTimestamp
                dailyCostText = if (habit.dailyCost > 0) habit.dailyCost.toString() else ""
                dailyTimeMinutesText = if (habit.dailyTimeMinutes > 0) habit.dailyTimeMinutes.toString() else ""
                calendar.timeInMillis = habit.createdTimestamp
                initialized = true
            }
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedTimestamp
    )
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE)
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val formattedDateTime = remember(selectedTimestamp) {
        sdf.format(Date(selectedTimestamp))
    }

    // Entrance animations
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) stringResource(R.string.edit_tracker)
                        else stringResource(R.string.add_new_entry),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                },
                actions = {
                    if (isEditMode && existingHabit != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_tracker),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { -it / 4 }
            ) {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text(stringResource(R.string.habit_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 100)) + slideInVertically(tween(400, delayMillis = 100)) { -it / 4 }
            ) {
                OutlinedTextField(
                    value = formattedDateTime,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.select_date_time)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    readOnly = true,
                    enabled = false,
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = null)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200)) { -it / 4 }
            ) {
                OutlinedTextField(
                    value = dailyCostText,
                    onValueChange = { newValue ->
                        // Only allow valid decimal numbers
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            dailyCostText = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.daily_cost)) },
                    placeholder = { Text(stringResource(R.string.daily_cost_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(550, delayMillis = 250)) + slideInVertically(tween(550, delayMillis = 250)) { -it / 4 }
            ) {
                OutlinedTextField(
                    value = dailyTimeMinutesText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d+$"))) {
                            dailyTimeMinutesText = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.daily_time_minutes)) },
                    placeholder = { Text(stringResource(R.string.daily_time_minutes_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, delayMillis = 300)) + slideInVertically(tween(600, delayMillis = 300)) { it / 4 }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = {
                            if (habitName.isNotBlank()) {
                                val dailyCost = dailyCostText.toDoubleOrNull() ?: 0.0
                                val dailyTimeMinutes = dailyTimeMinutesText.toIntOrNull() ?: 0
                                if (isEditMode && existingHabit != null) {
                                    val habit = existingHabit!!
                                    // Only update createdTimestamp with the selected date.
                                    // If no relapses have occurred (lastOccurrence == old createdTimestamp),
                                    // also update lastOccurrenceTimestamp so the counter reflects the new start date.
                                    // Otherwise, keep lastOccurrenceTimestamp at the last relapse time.
                                    val newLastOccurrence = if (habit.lastOccurrenceTimestamp == habit.createdTimestamp) {
                                        selectedTimestamp
                                    } else {
                                        habit.lastOccurrenceTimestamp
                                    }
                                    viewModel.updateHabit(
                                        habit.copy(
                                            name = habitName,
                                            lastOccurrenceTimestamp = newLastOccurrence,
                                            dailyCost = dailyCost,
                                            createdTimestamp = selectedTimestamp,
                                            dailyTimeMinutes = dailyTimeMinutes
                                        )
                                    )
                                } else {
                                    viewModel.addHabit(habitName, selectedTimestamp, dailyCost, dailyTimeMinutes)
                                }
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && existingHabit != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.delete_tracker),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(stringResource(R.string.confirm_delete))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHabit(existingHabit!!)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val finalCalendar = Calendar.getInstance()
                    datePickerState.selectedDateMillis?.let {
                        finalCalendar.timeInMillis = it
                    }
                    finalCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    finalCalendar.set(Calendar.MINUTE, timePickerState.minute)
                    selectedTimestamp = finalCalendar.timeInMillis
                    showTimePicker = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}
