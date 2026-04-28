/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.presentation.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import io.github.furkansariboga.ayik.R
import io.github.furkansariboga.ayik.security.SecurityManager
import io.github.furkansariboga.ayik.widget.widgetDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onSetupLock: () -> Unit,
    securityManager: SecurityManager
) {
    val context = LocalContext.current
    val languages = listOf(
        "English" to "en", "Türkçe" to "tr", "Español" to "es", "Français" to "fr",
        "Português" to "pt", "Bahasa Indonesia" to "id", "العربية" to "ar", "हिन्दी" to "hi",
        "বাংলা" to "bn", "اردو" to "ur", "简体中文" to "zh-Hans", "繁體中文" to "zh-Hant"
    )

    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentLocale = if (!currentLocales.isEmpty) {
        val firstLocale = currentLocales.get(0)
        if (firstLocale?.language == "zh") {
            if (firstLocale.script == "Hant" || firstLocale.country == "TW" || firstLocale.country == "HK") "zh-Hant" else "zh-Hans"
        } else {
            val lang = firstLocale?.language ?: "en"
            if (lang == "in") "id" else lang
        }
    } else "en"

    var showLanguageDialog by remember { mutableStateOf(false) }
    val isLockEnabled = securityManager.isLockEnabled
    val lockType = securityManager.lockType

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.settings)) },
            navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cancel)) } }
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())
        ) {
            // Language
            ListItem(
                headlineContent = { Text(stringResource(R.string.language)) },
                supportingContent = { Text(languages.find { it.second == currentLocale }?.first ?: "English") },
                modifier = Modifier.clickable { showLanguageDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // App Lock
            ListItem(
                headlineContent = { Text(stringResource(R.string.app_lock)) },
                supportingContent = {
                    Text(if (isLockEnabled) {
                        when (lockType) {
                            SecurityManager.LOCK_TYPE_BIOMETRIC -> stringResource(R.string.biometric_lock)
                            SecurityManager.LOCK_TYPE_PIN -> stringResource(R.string.pin_lock)
                            SecurityManager.LOCK_TYPE_PASSWORD -> stringResource(R.string.password_lock)
                            else -> stringResource(R.string.enabled)
                        }
                    } else stringResource(R.string.disabled))
                },
                leadingContent = { Icon(Icons.Default.Lock, null) },
                modifier = Modifier.clickable { onSetupLock() }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Widget Update Interval
            var showIntervalDialog by remember { mutableStateOf(false) }
            var currentInterval by remember { mutableStateOf(30) }
            val scope = rememberCoroutineScope()
            var showBatteryWarning by remember { mutableStateOf<Int?>(null) }
            
            LaunchedEffect(Unit) {
                currentInterval = io.github.furkansariboga.ayik.widget.WidgetDataHelper.getUpdateIntervalMinutes(context)
            }
            
            val formatInterval = { mins: Int -> 
                when {
                    mins < 60 -> "$mins mins"
                    mins == 60 -> "1 hour"
                    else -> "${mins / 60} hours"
                }
            }

            ListItem(
                headlineContent = { Text(stringResource(R.string.widget_update_interval)) },
                supportingContent = { Text(formatInterval(currentInterval)) },
                leadingContent = { Icon(Icons.Default.Info, null) },
                modifier = Modifier.clickable { showIntervalDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            if (showIntervalDialog) {
                val intervals = listOf(1, 5, 10, 30, 60, 120, 240, 480, 960, 1440)
                val scrollState = rememberScrollState()
                AlertDialog(
                    onDismissRequest = { showIntervalDialog = false },
                    title = { Text(stringResource(R.string.widget_update_interval)) },
                    text = {
                        Column(modifier = Modifier.verticalScroll(scrollState).fillMaxWidth()) {
                            intervals.forEach { interval ->
                                Row(modifier = Modifier.fillMaxWidth().clickable {
                                    if (interval in listOf(1, 5, 10)) {
                                        showBatteryWarning = interval
                                        showIntervalDialog = false
                                    } else {
                                        currentInterval = interval
                                        scope.launch {
                                            context.widgetDataStore.edit { prefs -> 
                                                prefs[io.github.furkansariboga.ayik.widget.WidgetDataHelper.updateIntervalKey] = interval
                                            }
                                            io.github.furkansariboga.ayik.widget.WidgetUpdateManager.scheduleUpdates(context, interval)
                                        }
                                        showIntervalDialog = false
                                    }
                                }.padding(vertical = 12.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = currentInterval == interval, onClick = null)
                                    Spacer(Modifier.width(16.dp))
                                    Text(formatInterval(interval))
                                }
                            }
                        }
                    },
                    confirmButton = { TextButton(onClick = { showIntervalDialog = false }) { Text(stringResource(android.R.string.cancel)) } }
                )
            }

            if (showBatteryWarning != null) {
                val interval = showBatteryWarning!!
                AlertDialog(
                    onDismissRequest = { showBatteryWarning = null },
                    icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    title = { Text(stringResource(R.string.battery_warning_title)) },
                    text = { Text(stringResource(R.string.battery_warning_desc)) },
                    confirmButton = {
                        Button(onClick = {
                            currentInterval = interval
                            scope.launch {
                                context.widgetDataStore.edit { prefs -> 
                                    prefs[io.github.furkansariboga.ayik.widget.WidgetDataHelper.updateIntervalKey] = interval
                                }
                                io.github.furkansariboga.ayik.widget.WidgetUpdateManager.scheduleUpdates(context, interval)
                            }
                            showBatteryWarning = null
                        }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                            Text(stringResource(R.string.proceed))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBatteryWarning = null }) { Text(stringResource(android.R.string.cancel)) }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About section
            Text(
                text = stringResource(R.string.about),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.about_disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.about_disclaimer_2),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showLanguageDialog) {
        val scrollState = rememberScrollState()
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column(modifier = Modifier.verticalScroll(scrollState).fillMaxWidth()) {
                    languages.forEach { (name, code) ->
                        Row(modifier = Modifier.fillMaxWidth().clickable {
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
                            showLanguageDialog = false
                        }.padding(vertical = 12.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = currentLocale == code, onClick = null)
                            Spacer(Modifier.width(16.dp))
                            Text(name)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLanguageDialog = false }) { Text(stringResource(android.R.string.ok)) } }
        )
    }
}
