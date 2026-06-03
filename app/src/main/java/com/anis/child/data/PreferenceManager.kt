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

    var fcmToken: String?
        get() = sharedPreferences.getString(KEY_FCM_TOKEN, null)
        set(value) = sharedPreferences.edit().putString(KEY_FCM_TOKEN, value).apply()

    var needsInitialAppSync: Boolean
        get() = sharedPreferences.getBoolean(KEY_NEEDS_INITIAL_APP_SYNC, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_NEEDS_INITIAL_APP_SYNC, value).apply()

    var childAge: Int?
        get() = sharedPreferences.getInt(KEY_CHILD_AGE, -1).let { if (it == -1) null else it }
        set(value) = sharedPreferences.edit().putInt(KEY_CHILD_AGE, value ?: -1).apply()

    var childAvatarUrl: String?
        get() = sharedPreferences.getString(KEY_CHILD_AVATAR_URL, null)
        set(value) = sharedPreferences.edit().putString(KEY_CHILD_AVATAR_URL, value).apply()

    var childEmail: String?
        get() = sharedPreferences.getString(KEY_CHILD_EMAIL, null)
        set(value) = sharedPreferences.edit().putString(KEY_CHILD_EMAIL, value).apply()

    var pinHash: String?
        get() = sharedPreferences.getString(KEY_PIN_HASH, null)
        set(value) = sharedPreferences.edit().putString(KEY_PIN_HASH, value).apply()

    var pinSalt: String?
        get() = sharedPreferences.getString(KEY_PIN_SALT, null)
        set(value) = sharedPreferences.edit().putString(KEY_PIN_SALT, value).apply()

    var failedPinAttempts: Int
        get() = sharedPreferences.getInt(KEY_FAILED_PIN_ATTEMPTS, 0)
        set(value) = sharedPreferences.edit().putInt(KEY_FAILED_PIN_ATTEMPTS, value).apply()

    var pinLockoutUntil: Long
        get() = sharedPreferences.getLong(KEY_PIN_LOCKOUT_UNTIL, 0)
        set(value) = sharedPreferences.edit().putLong(KEY_PIN_LOCKOUT_UNTIL, value).apply()

    var hasPin: Boolean
        get() = sharedPreferences.getBoolean(KEY_HAS_PIN, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_HAS_PIN, value).apply()

    var rewardPoints: Int
        get() = sharedPreferences.getInt(KEY_REWARD_POINTS, 0)
        set(value) = sharedPreferences.edit().putInt(KEY_REWARD_POINTS, value).apply()

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
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_NEEDS_INITIAL_APP_SYNC = "needs_initial_app_sync"
        private const val KEY_CHILD_AGE = "child_age"
        private const val KEY_CHILD_AVATAR_URL = "child_avatar_url"
        private const val KEY_CHILD_EMAIL = "child_email"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_FAILED_PIN_ATTEMPTS = "failed_pin_attempts"
        private const val KEY_PIN_LOCKOUT_UNTIL = "pin_lockout_until"
        private const val KEY_HAS_PIN = "has_pin"
        private const val KEY_REWARD_POINTS = "reward_points"
    }
}