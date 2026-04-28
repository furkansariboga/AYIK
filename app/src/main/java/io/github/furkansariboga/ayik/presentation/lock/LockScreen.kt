/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik.presentation.lock

import androidx.biometric.BiometricPrompt
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import io.github.furkansariboga.ayik.R
import io.github.furkansariboga.ayik.security.SecurityManager

@Composable
fun LockScreen(securityManager: SecurityManager, onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val lockType = securityManager.lockType
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lockType) {
        if (lockType == SecurityManager.LOCK_TYPE_BIOMETRIC) {
            showBiometricPrompt(context as FragmentActivity, onUnlocked) { errorMessage = it }
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -it / 4 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.unlock_app), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(48.dp))
            when (lockType) {
                SecurityManager.LOCK_TYPE_PIN -> PinInput(
                    onPinEntered = { if (securityManager.validateCredential(it)) onUnlocked() else errorMessage = context.getString(R.string.incorrect_pin) },
                    errorMessage = errorMessage, onErrorDismissed = { errorMessage = null }
                )
                SecurityManager.LOCK_TYPE_PASSWORD -> PasswordInput(
                    onPasswordEntered = { if (securityManager.validateCredential(it)) onUnlocked() else errorMessage = context.getString(R.string.incorrect_password) },
                    errorMessage = errorMessage, onErrorDismissed = { errorMessage = null }
                )
                SecurityManager.LOCK_TYPE_BIOMETRIC -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FilledTonalButton(onClick = { showBiometricPrompt(context as FragmentActivity, onUnlocked) { errorMessage = it } }) {
                            Icon(Icons.Default.Lock, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.use_biometric))
                        }
                        errorMessage?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinInput(onPinEntered: (String) -> Unit, errorMessage: String?, onErrorDismissed: () -> Unit) {
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
        Button(onClick = { if (pin.length >= 4) { onPinEntered(pin); pin = "" } }, enabled = pin.length >= 4, modifier = Modifier.fillMaxWidth(0.6f)) { Text(stringResource(R.string.unlock)) }
    }
}

@Composable
private fun PasswordInput(onPasswordEntered: (String) -> Unit, errorMessage: String?, onErrorDismissed: () -> Unit) {
    var password by remember { mutableStateOf("") }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(0.8f)) {
        OutlinedTextField(value = password, onValueChange = { onErrorDismissed(); password = it }, label = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (password.isNotEmpty()) { onPasswordEntered(password); password = "" } }),
            isError = errorMessage != null, supportingText = { errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { if (password.isNotEmpty()) { onPasswordEntered(password); password = "" } }, enabled = password.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.unlock)) }
    }
}

private fun showBiometricPrompt(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onSuccess() }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) onError(errString.toString()) }
        override fun onAuthenticationFailed() { onError(activity.getString(R.string.biometric_failed)) }
    }
    val prompt = BiometricPrompt(activity, executor, callback)
    val promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle(activity.getString(R.string.app_name)).setSubtitle(activity.getString(R.string.unlock_app)).setNegativeButtonText(activity.getString(R.string.cancel)).build()
    prompt.authenticate(promptInfo)
}
