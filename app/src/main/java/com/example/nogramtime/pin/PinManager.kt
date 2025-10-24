package com.example.nogramtime.pin

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

/**
 * Simple helper object that stores and verifies a user defined PIN using
 * EncryptedSharedPreferences. The PIN is hashed using SHA‑256 to avoid
 * persisting the raw digits.
 */
object PinManager {
    private const val PREF_NAME = "nogram_pin"
    private const val KEY_PIN_HASH = "pin_hash"

    /**
     * Set or update the PIN. The value is hashed before being stored to avoid
     * writing clear text secrets to disk.
     */
    fun setPin(context: Context, pin: String) {
        val prefs = getPrefs(context)
        prefs.edit().putString(KEY_PIN_HASH, hash(pin)).apply()
    }

    /**
     * Returns true if [pin] matches the stored hash. Returns false if no PIN
     * has been set or the hashes differ.
     */
    fun checkPin(context: Context, pin: String): Boolean {
        val prefs = getPrefs(context)
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return storedHash == hash(pin)
    }

    /**
     * Returns true if a PIN has been defined.
     */
    fun hasPin(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.contains(KEY_PIN_HASH)
    }

    /**
     * Removes any stored PIN. After calling this the application will no
     * longer require a PIN to access settings until a new one is set.
     */
    fun clearPin(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().remove(KEY_PIN_HASH).apply()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        val sb = StringBuilder()
        for (b in digest) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }
}