/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.presentation.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.furkansariboga.ayik.R
import io.github.furkansariboga.ayik.domain.model.Habit
import io.github.furkansariboga.ayik.domain.model.Milestone
import io.github.furkansariboga.ayik.domain.model.Relapse
import io.github.furkansariboga.ayik.domain.model.RestToken
import io.github.furkansariboga.ayik.presentation.components.MilestoneBadge
import io.github.furkansariboga.ayik.presentation.components.RelapseAnalyticsView
import io.github.furkansariboga.ayik.ui.theme.*
import io.github.furkansariboga.ayik.util.TimeUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsBottomSheet(habit: Habit, relapses: List<Relapse>, restTokens: List<RestToken>, milestones: List<Milestone>, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header
            item {
                Text(stringResource(R.string.stats), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                Text(habit.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Total Savings Card
            item {
                val daysSinceCreation = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - habit.createdTimestamp).coerceAtLeast(0)
                val totalSavings = habit.dailyCost * daysSinceCreation
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Star, null, tint = SavingsGold, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.total_savings), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(String.format(Locale.getDefault(), "%.2f", totalSavings), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(Modifier.height(4.dp))
                        Text("${stringResource(R.string.total_days_clean)}: $daysSinceCreation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        if (habit.dailyCost > 0) {
                            Text("${stringResource(R.string.daily_cost)}: ${String.format(Locale.getDefault(), "%.2f", habit.dailyCost)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                        if (habit.dailyTimeMinutes > 0) {
                            val totalMinutes = habit.dailyTimeMinutes * daysSinceCreation
                            val hours = totalMinutes / 60; val mins = totalMinutes % 60
                            Text("${stringResource(R.string.time_reclaimed)}: ${if (hours > 0) "${hours}h ${mins}m" else "${mins}m"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                        Text("${stringResource(R.string.created_on)}: ${sdf.format(Date(habit.createdTimestamp))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                }
            }

            // Milestones
            if (milestones.isNotEmpty()) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(stringResource(R.string.milestones), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        milestones.reversed().forEach { milestone -> MilestoneBadge(milestone = milestone, modifier = Modifier.fillMaxWidth()) }
                    }
                }
            }

            // Relapse Analytics
            if (relapses.size >= 2) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(stringResource(R.string.relapse_analytics), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                }
                item { RelapseAnalyticsView(relapses = relapses) }
            }

            // Last 3 Relapses
            item { Text(stringResource(R.string.last_3_relapses), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            if (relapses.isEmpty()) {
                item { Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(R.string.no_relapses), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } }
            } else {
                val lastThree = relapses.take(3)
                itemsIndexed(lastThree) { _, relapse ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = RelapseRedLight), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(sdf.format(Date(relapse.timestamp)), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = RelapseRed)
                            if (relapse.trigger.isNotBlank()) { Text("${stringResource(R.string.trigger_label)}: ${relapse.trigger}", style = MaterialTheme.typography.bodySmall, color = RelapseRed.copy(alpha = 0.8f)) }
                            if (relapse.journalNote.isNotBlank()) { Text(relapse.journalNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp)) }
                        }
                    }
                }
            }

            // Average Time Between Relapses
            if (relapses.size >= 2) {
                item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)); Text(stringResource(R.string.avg_time_last_3), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp)) }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (relapses.size >= 3) { AverageRow(stringResource(R.string.avg_time_last_3), TimeUtils.formatElapsedTime(context, calculateAverageInterval(relapses.take(3)))) }
                        else if (relapses.size >= 2) { AverageRow(stringResource(R.string.avg_time_last_3), TimeUtils.formatElapsedTime(context, calculateAverageInterval(relapses.take(2)))) }
                        if (relapses.size >= 5) { AverageRow(stringResource(R.string.avg_time_last_5), TimeUtils.formatElapsedTime(context, calculateAverageInterval(relapses.take(5)))) }
                        else if (relapses.size in 2..4) { AverageRow(stringResource(R.string.avg_time_last_5), stringResource(R.string.not_enough_data)) }
                        if (relapses.size >= 10) { AverageRow(stringResource(R.string.avg_time_last_10), TimeUtils.formatElapsedTime(context, calculateAverageInterval(relapses.take(10)))) }
                        else if (relapses.size in 2..9) { AverageRow(stringResource(R.string.avg_time_last_10), stringResource(R.string.not_enough_data)) }
                    }
                }
            }

            // Rest Tokens
            if (restTokens.isNotEmpty()) {
                item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)); Text(stringResource(R.string.rest_token_history), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp)) }
                itemsIndexed(restTokens.take(10)) { _, token ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = RelapseYellowLight), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(if (token.type == "DIFFICULT") stringResource(R.string.difficult) else stringResource(R.string.paused), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium, color = RelapseYellow)
                                Text(sdf.format(Date(token.timestamp)), style = MaterialTheme.typography.bodySmall, color = RelapseYellow)
                            }
                            if (token.note.isNotBlank()) { Text(token.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp)) }
                        }
                    }
                }
            }

            // Full Relapse History
            if (relapses.isNotEmpty()) {
                item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)); Text(stringResource(R.string.relapse_history), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp)) }
                itemsIndexed(relapses) { index, relapse ->
                    val (bgColor, textColor) = when { index < 3 -> RelapseRedLight to RelapseRed; index < 5 -> RelapseYellowLight to RelapseYellow; index < 10 -> RelapseGreenLight to RelapseGreen; else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant }
                    var visible by remember { mutableStateOf(false) }; LaunchedEffect(Unit) { visible = true }
                    AnimatedVisibility(visible = visible, enter = fadeIn(tween(300, delayMillis = index * 50)) + slideInVertically(tween(300, delayMillis = index * 50)) { it / 2 }) {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = bgColor), shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("#${index + 1}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = textColor)
                                    Text(sdf.format(Date(relapse.timestamp)), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = textColor)
                                }
                                if (relapse.trigger.isNotBlank()) { Text("${stringResource(R.string.trigger_label)}: ${relapse.trigger}", style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.8f)) }
                                if (relapse.journalNote.isNotBlank()) { Text(relapse.journalNote, style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.7f), modifier = Modifier.padding(top = 2.dp)) }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun AverageRow(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun calculateAverageInterval(relapses: List<Relapse>): Long {
    if (relapses.size < 2) return 0L
    val sortedDesc = relapses.sortedByDescending { it.timestamp }
    var totalInterval = 0L
    for (i in 0 until sortedDesc.size - 1) { totalInterval += sortedDesc[i].timestamp - sortedDesc[i + 1].timestamp }
    return totalInterval / (sortedDesc.size - 1)
}
