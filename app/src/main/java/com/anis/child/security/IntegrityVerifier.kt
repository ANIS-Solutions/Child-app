package com.anis.child.security

import android.content.Context
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import kotlinx.coroutines.tasks.await

class IntegrityVerifier(
    private val appContext: Context,
    private val cloudProjectNumber: Long
) {
    private val TAG = "IntegrityVerifier"

    suspend fun verify(): SecurityState {
        return try {
            val integrityManager = IntegrityManagerFactory.create(appContext)
            val tokenResponse = integrityManager.requestIntegrityToken(
                IntegrityTokenRequest.builder()
                    .setCloudProjectNumber(cloudProjectNumber)
                    .build()
            ).await()

            val token = tokenResponse.token()
            Log.d(TAG, "Integrity token obtained: ${token.take(50)}...")

            SecurityState.Ok
        } catch (e: com.google.android.play.core.integrity.IntegrityServiceException) {
            if (e.errorCode == -1) {
                Log.w(TAG, "Play Integrity not available (app may not be published)")
                SecurityState.Ok
            } else {
                Log.e(TAG, "Integrity service error: code=${e.errorCode}", e)
                SecurityState.IntegrityFailed("Integrity service error: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Integrity check failed", e)
            SecurityState.IntegrityFailed(e.message ?: "Unknown integrity error")
        }
    }
}
