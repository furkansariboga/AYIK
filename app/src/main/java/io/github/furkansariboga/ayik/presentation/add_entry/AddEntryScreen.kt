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
    var initialized by remember { mutableStateOf(false) }

    // Pre-fill fields in edit mode
    LaunchedEffect(existingHabit) {
        existingHabit?.let { habit ->
            if (!initialized) {
                habitName = habit.name
                selectedTimestamp = habit.lastOccurrenceTimestamp
                dailyCostText = if (habit.dailyCost > 0) habit.dailyCost.toString() else ""
                calendar.timeInMillis = habit.lastOccurrenceTimestamp
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
                                if (isEditMode && existingHabit != null) {
                                    viewModel.updateHabit(
                                        existingHabit!!.copy(
                                            name = habitName,
                                            lastOccurrenceTimestamp = selectedTimestamp,
                                            dailyCost = dailyCost
                                        )
                                    )
                                } else {
                                    viewModel.addHabit(habitName, selectedTimestamp, dailyCost)
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
