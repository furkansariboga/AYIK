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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.furkansariboga.ayik.R
import io.github.furkansariboga.ayik.domain.model.Habit
import io.github.furkansariboga.ayik.domain.model.Relapse
import io.github.furkansariboga.ayik.presentation.HabitViewModel
import io.github.furkansariboga.ayik.presentation.stats.StatsBottomSheet
import io.github.furkansariboga.ayik.util.TimeUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: HabitViewModel,
    onAddEntryClick: () -> Unit,
    onEditClick: (Int) -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    var selectedHabits by remember { mutableStateOf(setOf<Habit>()) }
    val isSelectionMode = selectedHabits.isNotEmpty()

    // Relapse confirmation dialog state
    var habitToRelapse by remember { mutableStateOf<Habit?>(null) }

    // Stats bottom sheet state
    var habitForStats by remember { mutableStateOf<Habit?>(null) }
    var relapsesList by remember { mutableStateOf<List<Relapse>>(emptyList()) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val relapseRecordedText = stringResource(R.string.relapse_recorded)

    BackHandler(enabled = isSelectionMode) {
        selectedHabits = emptySet()
    }

    // Collect relapses when stats sheet is shown
    habitForStats?.let { habit ->
        val relapsesFlow = remember(habit.id) { viewModel.getRelapsesForHabit(habit.id) }
        val relapsesState by relapsesFlow.collectAsState(initial = emptyList())
        LaunchedEffect(relapsesState) { relapsesList = relapsesState }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                TopAppBar(
                    title = { Text("${selectedHabits.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { selectedHabits = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            selectedHabits.forEach { viewModel.deleteHabit(it) }
                            selectedHabits = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }
                )
            }

            if (habits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Animated pulsing text
                        val infiniteTransition = rememberInfiniteTransition(label = "empty_pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = EaseInOutCubic),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )
                        Text(
                            text = stringResource(R.string.empty_dashboard),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit,
                            isSelected = selectedHabits.contains(habit),
                            onLongClick = {
                                selectedHabits = if (selectedHabits.contains(habit)) {
                                    selectedHabits - habit
                                } else {
                                    selectedHabits + habit
                                }
                            },
                            onClick = {
                                if (isSelectionMode) {
                                    selectedHabits = if (selectedHabits.contains(habit)) {
                                        selectedHabits - habit
                                    } else {
                                        selectedHabits + habit
                                    }
                                }
                            },
                            onEditClick = { onEditClick(habit.id) },
                            onRelapseClick = { habitToRelapse = habit },
                            onStatsClick = { habitForStats = habit },
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(300),
                                fadeOutSpec = tween(300),
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        )
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

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Relapse confirmation dialog
    habitToRelapse?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToRelapse = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.relapse),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(stringResource(R.string.confirm_relapse))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addRelapse(habit)
                        habitToRelapse = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { habitToRelapse = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Stats bottom sheet
    habitForStats?.let { habit ->
        StatsBottomSheet(
            habit = habit,
            relapses = relapsesList,
            onDismiss = {
                habitForStats = null
                relapsesList = emptyList()
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitItem(
    habit: Habit,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onRelapseClick: () -> Unit,
    onStatsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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

    // Card entrance animation
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val scale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        ),
        shape = RoundedCornerShape(16.dp),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Edit button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated counter
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.primary
            )

            if (habit.dailyCost > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                val daysSinceStart = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(
                    currentTime - habit.createdTimestamp
                ).coerceAtLeast(0)
                val savedAmount = habit.dailyCost * daysSinceStart
                Text(
                    text = "${stringResource(R.string.total_savings)}: ${String.format(java.util.Locale.getDefault(), "%.2f", savedAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Relapse button
                OutlinedButton(
                    onClick = onRelapseClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.relapse),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Stats button
                FilledTonalButton(
                    onClick = onStatsClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.stats),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
