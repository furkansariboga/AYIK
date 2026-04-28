/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.presentation.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import io.github.furkansariboga.ayik.domain.model.Milestone
import io.github.furkansariboga.ayik.domain.model.Relapse
import io.github.furkansariboga.ayik.domain.model.RestToken
import io.github.furkansariboga.ayik.presentation.HabitViewModel
import io.github.furkansariboga.ayik.presentation.components.HeatmapView
import io.github.furkansariboga.ayik.presentation.components.MilestoneBadge
import io.github.furkansariboga.ayik.presentation.stats.StatsBottomSheet
import io.github.furkansariboga.ayik.util.TimeUtils
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: HabitViewModel, onAddEntryClick: () -> Unit, onEditClick: (Int) -> Unit) {
    val habits by viewModel.habits.collectAsState()
    var selectedHabits by remember { mutableStateOf(setOf<Habit>()) }
    val isSelectionMode = selectedHabits.isNotEmpty()
    var habitToRelapse by remember { mutableStateOf<Habit?>(null) }
    var habitForStats by remember { mutableStateOf<Habit?>(null) }
    var relapsesList by remember { mutableStateOf<List<Relapse>>(emptyList()) }
    var restTokensList by remember { mutableStateOf<List<RestToken>>(emptyList()) }
    var milestonesList by remember { mutableStateOf<List<Milestone>>(emptyList()) }
    // Rest token dialog
    var habitForRestToken by remember { mutableStateOf<Habit?>(null) }
    // Relapse journal state
    var relapseJournalNote by remember { mutableStateOf("") }
    var relapseSelectedTrigger by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val relapseRecordedText = stringResource(R.string.relapse_recorded)

    BackHandler(enabled = isSelectionMode) { selectedHabits = emptySet() }

    // Collect relapses/rest tokens/milestones when stats sheet is shown
    habitForStats?.let { habit ->
        val relapsesFlow = remember(habit.id) { viewModel.getRelapsesForHabit(habit.id) }
        val relapsesState by relapsesFlow.collectAsState(initial = emptyList())
        val restTokensFlow = remember(habit.id) { viewModel.getRestTokensForHabit(habit.id) }
        val restTokensState by restTokensFlow.collectAsState(initial = emptyList())
        val milestonesFlow = remember(habit.id) { viewModel.getMilestonesForHabit(habit.id) }
        val milestonesState by milestonesFlow.collectAsState(initial = emptyList())
        LaunchedEffect(relapsesState) { relapsesList = relapsesState }
        LaunchedEffect(restTokensState) { restTokensList = restTokensState }
        LaunchedEffect(milestonesState) { milestonesList = milestonesState }
    }

    // Check milestones for all habits periodically
    LaunchedEffect(habits) {
        habits.forEach { viewModel.checkAndAwardMilestones(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(visible = isSelectionMode, enter = slideInVertically() + fadeIn(), exit = slideOutVertically() + fadeOut()) {
                TopAppBar(
                    title = { Text("${selectedHabits.size} selected") },
                    navigationIcon = { IconButton(onClick = { selectedHabits = emptySet() }) { Icon(Icons.Default.Close, null) } },
                    actions = { IconButton(onClick = { selectedHabits.forEach { viewModel.deleteHabit(it) }; selectedHabits = emptySet() }) { Icon(Icons.Default.Delete, null) } }
                )
            }

            if (habits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val infiniteTransition = rememberInfiniteTransition(label = "empty_pulse")
                        val alpha by infiniteTransition.animateFloat(initialValue = 0.4f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOutCubic), RepeatMode.Reverse), label = "alpha")
                        Text(text = stringResource(R.string.empty_dashboard), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(habits, key = { it.id }) { habit ->
                        HabitItem(
                            habit = habit, viewModel = viewModel, isSelected = selectedHabits.contains(habit),
                            isSelectionMode = isSelectionMode,
                            onLongClick = { selectedHabits = if (selectedHabits.contains(habit)) selectedHabits - habit else selectedHabits + habit },
                            onClick = { if (isSelectionMode) { selectedHabits = if (selectedHabits.contains(habit)) selectedHabits - habit else selectedHabits + habit } },
                            onEditClick = { onEditClick(habit.id) },
                            onRelapseClick = { habitToRelapse = habit },
                            onStatsClick = { habitForStats = habit },
                            onRestTokenClick = { habitForRestToken = habit },
                            modifier = Modifier.animateItem(fadeInSpec = tween(300), fadeOutSpec = tween(300), placementSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                        Text(text = stringResource(R.string.disclaimer), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    // Relapse confirmation dialog with journal
    habitToRelapse?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToRelapse = null; relapseJournalNote = ""; relapseSelectedTrigger = "" },
            icon = { Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.relapse), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.confirm_relapse))
                    HorizontalDivider()
                    Text(stringResource(R.string.relapse_journal_prompt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = relapseJournalNote, onValueChange = { relapseJournalNote = it },
                        label = { Text(stringResource(R.string.journal_note_optional)) },
                        modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4
                    )
                    Text(stringResource(R.string.trigger_optional), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val triggers = listOf("Stress", "Boredom", "Social", "Loneliness", "Celebration", "Other")
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        triggers.forEach { trigger ->
                            FilterChip(
                                selected = relapseSelectedTrigger == trigger,
                                onClick = { relapseSelectedTrigger = if (relapseSelectedTrigger == trigger) "" else trigger },
                                label = { Text(trigger, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addRelapse(habit, relapseJournalNote, relapseSelectedTrigger)
                    habitToRelapse = null; relapseJournalNote = ""; relapseSelectedTrigger = ""
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = { OutlinedButton(onClick = { habitToRelapse = null; relapseJournalNote = ""; relapseSelectedTrigger = "" }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    // Rest Token dialog
    habitForRestToken?.let { habit ->
        var restNote by remember { mutableStateOf("") }
        var restType by remember { mutableStateOf("DIFFICULT") }
        AlertDialog(
            onDismissRequest = { habitForRestToken = null },
            icon = { Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.tertiary) },
            title = { Text(stringResource(R.string.mark_day), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.rest_token_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = restType == "DIFFICULT", onClick = { restType = "DIFFICULT" }, label = { Text(stringResource(R.string.difficult)) })
                        FilterChip(selected = restType == "PAUSED", onClick = { restType = "PAUSED" }, label = { Text(stringResource(R.string.paused)) })
                    }
                    OutlinedTextField(value = restNote, onValueChange = { restNote = it }, label = { Text(stringResource(R.string.note_optional)) }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                }
            },
            confirmButton = { Button(onClick = { viewModel.addRestToken(habit.id, restType, restNote); habitForRestToken = null }) { Text(stringResource(R.string.save)) } },
            dismissButton = { OutlinedButton(onClick = { habitForRestToken = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    // Stats bottom sheet
    habitForStats?.let { habit ->
        StatsBottomSheet(habit = habit, relapses = relapsesList, restTokens = restTokensList, milestones = milestonesList, onDismiss = { habitForStats = null; relapsesList = emptyList(); restTokensList = emptyList(); milestonesList = emptyList() })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitItem(
    habit: Habit, viewModel: HabitViewModel, isSelected: Boolean, isSelectionMode: Boolean,
    onLongClick: () -> Unit, onClick: () -> Unit, onEditClick: () -> Unit,
    onRelapseClick: () -> Unit, onStatsClick: () -> Unit, onRestTokenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(habit.id) { while (true) { currentTime = System.currentTimeMillis(); delay(1000) } }

    var isExpanded by remember { mutableStateOf(false) }
    val hasRelapsed = habit.lastOccurrenceTimestamp != habit.createdTimestamp

    val elapsedSinceLastOccurrence = currentTime - habit.lastOccurrenceTimestamp
    val formattedCurrentStreak = TimeUtils.formatElapsedTime(context, elapsedSinceLastOccurrence)

    val totalElapsed = currentTime - habit.createdTimestamp
    val formattedTotalTime = TimeUtils.formatElapsedTime(context, totalElapsed)

    // Collect data for expanded card
    val relapsesFlow = remember(habit.id) { viewModel.getRelapsesForHabit(habit.id) }
    val relapses by relapsesFlow.collectAsState(initial = emptyList())
    val restTokensFlow = remember(habit.id) { viewModel.getRestTokensForHabit(habit.id) }
    val restTokens by restTokensFlow.collectAsState(initial = emptyList())
    val milestonesFlow = remember(habit.id) { viewModel.getMilestonesForHabit(habit.id) }
    val milestones by milestonesFlow.collectAsState(initial = emptyList())

    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val scale by animateFloatAsState(targetValue = if (appeared) 1f else 0.92f, animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "card_scale")

    Card(
        modifier = modifier.fillMaxWidth().scale(scale).combinedClickable(
            onClick = { if (isSelectionMode) onClick() else isExpanded = !isExpanded },
            onLongClick = onLongClick
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = if (isSelected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp).animateContentSize(spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(habit.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, stringResource(R.string.edit), Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Current streak (since last relapse or creation)
            if (hasRelapsed) {
                Text(stringResource(R.string.current_streak), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(formattedCurrentStreak, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary)

            // Sub-counter: since last relapse (if user has relapsed)
            if (hasRelapsed) {
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.total_time_clean), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formattedTotalTime, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (habit.dailyCost > 0) {
                Spacer(Modifier.height(4.dp))
                val daysSinceStart = TimeUnit.MILLISECONDS.toDays(currentTime - habit.createdTimestamp).coerceAtLeast(0)
                val savedAmount = habit.dailyCost * daysSinceStart
                Text("${stringResource(R.string.total_savings)}: ${String.format(java.util.Locale.getDefault(), "%.2f", savedAmount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Expanded content
            if (isExpanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                // Time Reclaimed
                if (habit.dailyTimeMinutes > 0) {
                    val daysSinceStart = TimeUnit.MILLISECONDS.toDays(currentTime - habit.createdTimestamp).coerceAtLeast(0)
                    val totalMinutesReclaimed = habit.dailyTimeMinutes * daysSinceStart
                    val hoursReclaimed = totalMinutesReclaimed / 60
                    val minsRemaining = totalMinutesReclaimed % 60
                    val timeReclaimedText = if (hoursReclaimed > 0) "${hoursReclaimed}h ${minsRemaining}m" else "${minsRemaining}m"
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(stringResource(R.string.time_reclaimed), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                Text(timeReclaimedText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Heatmap
                Text(stringResource(R.string.activity_heatmap), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                HeatmapView(createdTimestamp = habit.createdTimestamp, relapses = relapses, restTokens = restTokens, modifier = Modifier.fillMaxWidth())

                // Milestones
                if (milestones.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.milestones), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        milestones.takeLast(5).reversed().forEach { milestone -> MilestoneBadge(milestone = milestone) }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Action buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRelapseClick, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) {
                    Icon(Icons.Default.Refresh, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text(stringResource(R.string.relapse), style = MaterialTheme.typography.labelMedium)
                }
                FilledTonalButton(onClick = onRestTokenClick, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text(stringResource(R.string.mark_day), style = MaterialTheme.typography.labelMedium)
                }
                FilledTonalButton(onClick = onStatsClick, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) {
                    Icon(Icons.Outlined.Info, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text(stringResource(R.string.stats), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
