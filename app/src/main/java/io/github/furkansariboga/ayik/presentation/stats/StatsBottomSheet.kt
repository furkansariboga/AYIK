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
package io.github.furkansariboga.ayik.presentation.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.furkansariboga.ayik.R
import io.github.furkansariboga.ayik.domain.model.Habit
import io.github.furkansariboga.ayik.domain.model.Relapse
import io.github.furkansariboga.ayik.ui.theme.*
import io.github.furkansariboga.ayik.util.TimeUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsBottomSheet(
    habit: Habit,
    relapses: List<Relapse>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = stringResource(R.string.stats),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Total Savings Card
            item {
                val daysSinceCreation = TimeUnit.MILLISECONDS.toDays(
                    System.currentTimeMillis() - habit.createdTimestamp
                ).coerceAtLeast(0)
                val totalSavings = habit.dailyCost * daysSinceCreation

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = SavingsGold,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.total_savings),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f", totalSavings),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${stringResource(R.string.total_days_clean)}: $daysSinceCreation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        if (habit.dailyCost > 0) {
                            Text(
                                text = "${stringResource(R.string.daily_cost)}: ${String.format(Locale.getDefault(), "%.2f", habit.dailyCost)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = "${stringResource(R.string.created_on)}: ${sdf.format(Date(habit.createdTimestamp))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Last 3 Relapses
            item {
                Text(
                    text = stringResource(R.string.last_3_relapses),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (relapses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_relapses),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val lastThree = relapses.take(3)
                itemsIndexed(lastThree) { _, relapse ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = RelapseRedLight
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = sdf.format(Date(relapse.timestamp)),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp),
                            color = RelapseRed
                        )
                    }
                }
            }

            // Average Time Between Relapses
            if (relapses.size >= 2) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = stringResource(R.string.avg_time_last_3),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Calculate averages
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Last 3 average
                        if (relapses.size >= 3) {
                            val avg3 = calculateAverageInterval(relapses.take(3))
                            AverageRow(
                                label = stringResource(R.string.avg_time_last_3),
                                value = TimeUtils.formatElapsedTime(context, avg3)
                            )
                        } else if (relapses.size >= 2) {
                            val avg = calculateAverageInterval(relapses.take(2))
                            AverageRow(
                                label = stringResource(R.string.avg_time_last_3),
                                value = TimeUtils.formatElapsedTime(context, avg)
                            )
                        }

                        // Last 5 average
                        if (relapses.size >= 5) {
                            val avg5 = calculateAverageInterval(relapses.take(5))
                            AverageRow(
                                label = stringResource(R.string.avg_time_last_5),
                                value = TimeUtils.formatElapsedTime(context, avg5)
                            )
                        } else if (relapses.size in 2..4) {
                            AverageRow(
                                label = stringResource(R.string.avg_time_last_5),
                                value = stringResource(R.string.not_enough_data)
                            )
                        }

                        // Last 10 average
                        if (relapses.size >= 10) {
                            val avg10 = calculateAverageInterval(relapses.take(10))
                            AverageRow(
                                label = stringResource(R.string.avg_time_last_10),
                                value = TimeUtils.formatElapsedTime(context, avg10)
                            )
                        } else if (relapses.size in 2..9) {
                            AverageRow(
                                label = stringResource(R.string.avg_time_last_10),
                                value = stringResource(R.string.not_enough_data)
                            )
                        }
                    }
                }
            }

            // Full Relapse History
            if (relapses.isNotEmpty()) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = stringResource(R.string.relapse_history),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                itemsIndexed(relapses) { index, relapse ->
                    val (bgColor, textColor) = when {
                        index < 3 -> RelapseRedLight to RelapseRed
                        index < 5 -> RelapseYellowLight to RelapseYellow
                        index < 10 -> RelapseGreenLight to RelapseGreen
                        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { visible = true }

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(300, delayMillis = index * 50)) +
                                slideInVertically(tween(300, delayMillis = index * 50)) { it / 2 }
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = sdf.format(Date(relapse.timestamp)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AverageRow(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun calculateAverageInterval(relapses: List<Relapse>): Long {
    if (relapses.size < 2) return 0L
    val sortedDesc = relapses.sortedByDescending { it.timestamp }
    var totalInterval = 0L
    for (i in 0 until sortedDesc.size - 1) {
        totalInterval += sortedDesc[i].timestamp - sortedDesc[i + 1].timestamp
    }
    return totalInterval / (sortedDesc.size - 1)
}
