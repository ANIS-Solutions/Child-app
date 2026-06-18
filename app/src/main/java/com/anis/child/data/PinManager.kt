package com.anis.child.data

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
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
        val hash = pbkdf2Hash(pin, salt)
        preferenceManager.pinHash = hash
        preferenceManager.pinSalt = salt.joinToString("") { "%02x".format(it) }
        preferenceManager.hasPin = true
        preferenceManager.failedPinAttempts = 0
        preferenceManager.pinLockoutUntil = 0
    }

    fun validatePin(pin: String): Boolean {
        if (isLockedOut()) return false

        val storedHash = preferenceManager.pinHash ?: return false
        val saltHex = preferenceManager.pinSalt
        if (saltHex == null) {
            recordFailedAttempt()
            return false
        }

        val salt = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

        if (storedHash.length == 64) {
            if (verifyOldSha256(pin, storedHash, salt)) {
                setPin(pin)
                return true
            }
            recordFailedAttempt()
            return false
        }

        val computedHash = pbkdf2Hash(pin, salt)
        return if (computedHash == storedHash) {
            preferenceManager.failedPinAttempts = 0
            true
        } else {
            recordFailedAttempt()
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

    fun resetLockout() {
        preferenceManager.failedPinAttempts = 0
        preferenceManager.pinLockoutUntil = 0
    }

    private fun recordFailedAttempt() {
        preferenceManager.failedPinAttempts = preferenceManager.failedPinAttempts + 1
        if (preferenceManager.failedPinAttempts >= 5) {
            preferenceManager.pinLockoutUntil = System.currentTimeMillis() + 30_000
        }
    }

    private fun verifyOldSha256(pin: String, storedHash: String, salt: ByteArray): Boolean {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(salt)
            val hash = digest.digest(pin.toByteArray())
            hash.joinToString("") { "%02x".format(it) } == storedHash
        } catch (_: Exception) {
            false
        }
    }

    private fun pbkdf2Hash(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, 100000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}
