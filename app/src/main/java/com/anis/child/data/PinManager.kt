package com.anis.child.data

import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinManager @Inject constructor(
    private val preferenceManager: PreferenceManager
) {
    private val secureRandom = SecureRandom()

    fun isPinSet(): Boolean = preferenceManager.hasPin

    fun setPin(pin: String) {
        val salt = ByteArray(16).also { secureRandom.nextBytes(it) }
        val hash = hashPin(pin, salt)
        preferenceManager.pinHash = hash
        preferenceManager.pinSalt = salt.joinToString("") { "%02x".format(it) }
        preferenceManager.hasPin = true
        preferenceManager.failedPinAttempts = 0
        preferenceManager.pinLockoutUntil = 0
    }

    fun validatePin(pin: String): Boolean {
        val storedHash = preferenceManager.pinHash ?: return false
        val saltHex = preferenceManager.pinSalt ?: return false
        val salt = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val computedHash = hashPin(pin, salt)

        return if (computedHash == storedHash) {
            preferenceManager.failedPinAttempts = 0
            true
        } else {
            preferenceManager.failedPinAttempts = preferenceManager.failedPinAttempts + 1
            if (preferenceManager.failedPinAttempts >= 5) {
                preferenceManager.pinLockoutUntil = System.currentTimeMillis() + 30_000
            }
            false
        }
    }

    fun isLockedOut(): Boolean {
        val lockoutUntil = preferenceManager.pinLockoutUntil
        if (lockoutUntil == 0L) return false
        if (System.currentTimeMillis() >= lockoutUntil) {
            preferenceManager.pinLockoutUntil = 0
            preferenceManager.failedPinAttempts = 0
            return false
        }
        return true
    }

    fun getFailedAttempts(): Int = preferenceManager.failedPinAttempts

    private fun hashPin(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val hash = digest.digest(pin.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
