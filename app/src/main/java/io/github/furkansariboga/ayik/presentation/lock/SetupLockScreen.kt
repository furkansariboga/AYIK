/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.presentation.lock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack as ArrowBackIcon
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.furkansariboga.ayik.R
import io.github.furkansariboga.ayik.security.SecurityManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupLockScreen(securityManager: SecurityManager, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val biometricAvailable = remember { securityManager.isBiometricAvailable(context) }
    var step by remember { mutableStateOf(0) } // 0=choose type, 1=enter, 2=confirm, 3=done
    var selectedType by remember { mutableStateOf("") }
    var firstEntry by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.setup_lock), fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
        )
    }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when (step) {
                0 -> { // Choose type
                    Text(stringResource(R.string.choose_lock_type), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 24.dp))
                    if (biometricAvailable) {
                        LockTypeCard(icon = { Icon(Icons.Default.Lock, null, Modifier.size(32.dp)) }, title = stringResource(R.string.biometric_lock),
                            subtitle = stringResource(R.string.biometric_lock_desc), onClick = { selectedType = SecurityManager.LOCK_TYPE_BIOMETRIC; securityManager.enableLock(SecurityManager.LOCK_TYPE_BIOMETRIC); step = 3 })
                        Spacer(Modifier.height(12.dp))
                    }
                    LockTypeCard(icon = { Icon(Icons.Default.Lock, null, Modifier.size(32.dp)) }, title = stringResource(R.string.pin_lock),
                        subtitle = stringResource(R.string.pin_lock_desc), onClick = { selectedType = SecurityManager.LOCK_TYPE_PIN; step = 1 })
                    Spacer(Modifier.height(12.dp))
                    LockTypeCard(icon = { Icon(Icons.Default.Lock, null, Modifier.size(32.dp)) }, title = stringResource(R.string.password_lock),
                        subtitle = stringResource(R.string.password_lock_desc), onClick = { selectedType = SecurityManager.LOCK_TYPE_PASSWORD; step = 1 })
                    if (securityManager.isLockEnabled) {
                        Spacer(Modifier.height(24.dp))
                        OutlinedButton(onClick = { securityManager.disableLock(); onNavigateBack() }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Text(stringResource(R.string.disable_lock))
                        }
                    }
                }
                1 -> { // Enter credential
                    Text(if (selectedType == SecurityManager.LOCK_TYPE_PIN) stringResource(R.string.enter_new_pin) else stringResource(R.string.enter_new_password),
                        style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 24.dp))
                    if (selectedType == SecurityManager.LOCK_TYPE_PIN) {
                        SetupPinInput(onPinEntered = { firstEntry = it; errorMessage = null; step = 2 }, errorMessage = errorMessage, onErrorDismissed = { errorMessage = null })
                    } else {
                        SetupPasswordInput(onPasswordEntered = { firstEntry = it; errorMessage = null; step = 2 }, errorMessage = errorMessage, onErrorDismissed = { errorMessage = null })
                    }
                }
                2 -> { // Confirm credential
                    Text(if (selectedType == SecurityManager.LOCK_TYPE_PIN) stringResource(R.string.confirm_new_pin) else stringResource(R.string.confirm_new_password),
                        style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 24.dp))
                    if (selectedType == SecurityManager.LOCK_TYPE_PIN) {
                        SetupPinInput(onPinEntered = { if (it == firstEntry) { securityManager.enableLock(selectedType, it); step = 3 } else { errorMessage = context.getString(R.string.credentials_mismatch) } },
                            errorMessage = errorMessage, onErrorDismissed = { errorMessage = null })
                    } else {
                        SetupPasswordInput(onPasswordEntered = { if (it == firstEntry) { securityManager.enableLock(selectedType, it); step = 3 } else { errorMessage = context.getString(R.string.credentials_mismatch) } },
                            errorMessage = errorMessage, onErrorDismissed = { errorMessage = null })
                    }
                }
                3 -> { // Done
                    Spacer(Modifier.height(48.dp))
                    Icon(Icons.Default.Check, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.lock_setup_complete), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = onNavigateBack) { Text(stringResource(R.string.done)) }
                }
            }
        }
    }
}

@Composable
private fun LockTypeCard(icon: @Composable () -> Unit, title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(Modifier.width(16.dp))
            Column { Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun SetupPinInput(onPinEntered: (String) -> Unit, errorMessage: String?, onErrorDismissed: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            repeat(pin.length.coerceAtMost(16)) { Box(Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)) }
            if (pin.length > 16) Text("+${pin.length - 16}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp)) }
        val buttons = listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"), listOf("","0","⌫"))
        buttons.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                row.forEach { key ->
                    if (key.isEmpty()) { Spacer(Modifier.size(72.dp)) } else {
                        FilledTonalButton(onClick = { onErrorDismissed(); if (key == "⌫") { if (pin.isNotEmpty()) pin = pin.dropLast(1) } else if (pin.length < 32) pin += key }, modifier = Modifier.size(72.dp), shape = CircleShape) {
                            if (key == "⌫") Text("⌫", fontSize = 20.sp) else Text(key, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { if (pin.length >= 4) { onPinEntered(pin); pin = "" } }, enabled = pin.length >= 4, modifier = Modifier.fillMaxWidth(0.6f)) { Text(stringResource(R.string.confirm)) }
    }
}

@Composable
private fun SetupPasswordInput(onPasswordEntered: (String) -> Unit, errorMessage: String?, onErrorDismissed: () -> Unit) {
    var password by remember { mutableStateOf("") }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(0.8f)) {
        OutlinedTextField(value = password, onValueChange = { onErrorDismissed(); password = it }, label = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (password.isNotEmpty()) { onPasswordEntered(password); password = "" } }),
            isError = errorMessage != null, supportingText = { errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { if (password.isNotEmpty()) { onPasswordEntered(password); password = "" } }, enabled = password.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.confirm)) }
    }
}
