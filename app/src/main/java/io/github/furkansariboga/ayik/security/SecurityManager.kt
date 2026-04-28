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
package io.github.furkansariboga.ayik.security

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

class SecurityManager(context: Context) {

    companion object {
        private const val PREF_NAME = "ayik_security_prefs"
        private const val KEY_LOCK_ENABLED = "lock_enabled"
        private const val KEY_LOCK_TYPE = "lock_type" // "biometric", "pin", "password"
        private const val KEY_CREDENTIAL_HASH = "credential_hash"

        const val LOCK_TYPE_BIOMETRIC = "biometric"
        const val LOCK_TYPE_PIN = "pin"
        const val LOCK_TYPE_PASSWORD = "password"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val isLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOCK_ENABLED, false)

    val lockType: String
        get() = prefs.getString(KEY_LOCK_TYPE, "") ?: ""

    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    fun enableLock(type: String, credential: String? = null) {
        prefs.edit().apply {
            putBoolean(KEY_LOCK_ENABLED, true)
            putString(KEY_LOCK_TYPE, type)
            if (credential != null) {
                putString(KEY_CREDENTIAL_HASH, hashCredential(credential))
            }
            apply()
        }
    }

    fun disableLock() {
        prefs.edit().apply {
            putBoolean(KEY_LOCK_ENABLED, false)
            putString(KEY_LOCK_TYPE, "")
            remove(KEY_CREDENTIAL_HASH)
            apply()
        }
    }

    fun validateCredential(input: String): Boolean {
        val storedHash = prefs.getString(KEY_CREDENTIAL_HASH, null) ?: return false
        return hashCredential(input) == storedHash
    }

    private fun hashCredential(credential: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(credential.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
