package com.example.nogramtime.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * SettingsManager stores miscellaneous preferences for NoGram Time that are not
 * represented as part of the scheduling database. The values are stored in
 * encrypted shared preferences to prevent tampering. Currently this manager
 * controls whether the blocking functionality is temporarily suspended and
 * whether the optional VPN module is enabled. Additional settings can be
 * added here in the future.
 */
object SettingsManager {
    private const val PREF_NAME = "nogram_settings"
    private const val KEY_SUSPEND_BLOCK = "suspend_block"
    private const val KEY_VPN_ENABLED = "vpn_enabled"

    /**
     * Returns true if blocking is temporarily suspended. When suspended the
     * accessibility service will ignore active rules.
     */
    fun isBlockingSuspended(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.getBoolean(KEY_SUSPEND_BLOCK, false)
    }

    /**
     * Enables or disables temporary suspension of blocking. When true the
     * accessibility service will allow Instagram to launch. Callers should
     * implement their own timeout if they wish to auto‑reenable blocking.
     */
    fun setBlockingSuspended(context: Context, suspended: Boolean) {
        val prefs = getPrefs(context)
        prefs.edit().putBoolean(KEY_SUSPEND_BLOCK, suspended).apply()
    }

    /**
     * Returns true if the VPN module should be running. The UI persists
     * this preference and the worker/service uses it when deciding whether
     * to start the VPN service.
     */
    fun isVpnEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_VPN_ENABLED, false)
    }

    /**
     * Persists the VPN enabled flag. This does not automatically start or
     * stop the service – the caller must handle service lifecycle.
     */
    fun setVpnEnabled(context: Context, enabled: Boolean) {
        val prefs = getPrefs(context)
        prefs.edit().putBoolean(KEY_VPN_ENABLED, enabled).apply()
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
}