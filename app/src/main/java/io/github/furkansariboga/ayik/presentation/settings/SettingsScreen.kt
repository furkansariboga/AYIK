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
package io.github.furkansariboga.ayik.presentation.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import io.github.furkansariboga.ayik.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val languages = listOf(
        "English" to "en",
        "Türkçe" to "tr",
        "Español" to "es",
        "Français" to "fr",
        "Português" to "pt",
        "Bahasa Indonesia" to "id",
        "العربية" to "ar",
        "हिन्दी" to "hi",
        "বাংলা" to "bn",
        "اردو" to "ur",
        "简体中文" to "zh-Hans",
        "繁體中文" to "zh-Hant"
    )

    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentLocale = if (!currentLocales.isEmpty) {
        val firstLocale = currentLocales.get(0)
        if (firstLocale?.language == "zh") {
            if (firstLocale.script == "Hant" || firstLocale.country == "TW" || firstLocale.country == "HK") "zh-Hant"
            else "zh-Hans"
        } else {
            // Normalize 'in' to 'id' for Indonesian if necessary, though AppCompat handles it
            val lang = firstLocale?.language ?: "en"
            if (lang == "in") "id" else lang
        }
    } else {
        "en"
    }

    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
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
            ListItem(
                headlineContent = { Text(stringResource(R.string.language)) },
                supportingContent = { 
                    val currentLangName = languages.find { it.second == currentLocale }?.first ?: "English"
                    Text(currentLangName)
                },
                modifier = Modifier.clickable { showLanguageDialog = true }
            )
            
            HorizontalDivider()
        }
    }

    if (showLanguageDialog) {
        val scrollState = rememberScrollState()
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .fillMaxWidth()
                ) {
                    languages.forEach { (name, code) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(code)
                                    AppCompatDelegate.setApplicationLocales(appLocale)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentLocale == code,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }
}
