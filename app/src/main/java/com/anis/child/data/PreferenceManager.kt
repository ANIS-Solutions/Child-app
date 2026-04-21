package com.anis.child.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferenceManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var accessToken: String?
        get() = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        set(value) = sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, value).apply()

    var isLoggedIn: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var childId: String?
        get() = sharedPreferences.getString(KEY_CHILD_ID, null)
        set(value) = sharedPreferences.edit().putString(KEY_CHILD_ID, value).apply()

    var childName: String?
        get() = sharedPreferences.getString(KEY_CHILD_NAME, null)
        set(value) = sharedPreferences.edit().putString(KEY_CHILD_NAME, value).apply()

    var isMonitoringEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_MONITORING_ENABLED, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_IS_MONITORING_ENABLED, value).apply()

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "anis_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_CHILD_ID = "child_id"
        private const val KEY_CHILD_NAME = "child_name"
        private const val KEY_IS_MONITORING_ENABLED = "is_monitoring_enabled"
    }
}