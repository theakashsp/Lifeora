package com.liferise.app.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import java.security.MessageDigest

class SecurityManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("liferise_security_prefs", Context.MODE_PRIVATE)

    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun savePin(pin: String) {
        val hashed = hashPin(pin)
        prefs.edit().putString(KEY_PIN_HASH, hashed).apply()
    }

    fun verifyPin(inputPin: String): Boolean {
        val savedHash = prefs.getString(KEY_PIN_HASH, null) ?: return true
        return hashPin(inputPin) == savedHash
    }

    fun isPinSet(): Boolean {
        return prefs.contains(KEY_PIN_HASH)
    }

    fun clearPin() {
        prefs.edit().remove(KEY_PIN_HASH).apply()
    }

    fun canAuthenticateWithBiometrics(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    companion object {
        private const val KEY_PIN_HASH = "key_pin_hash_sha256"
    }
}
