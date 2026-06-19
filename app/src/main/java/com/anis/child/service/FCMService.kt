package com.anis.child.service

import android.util.Log
import com.anis.child.data.local.AppRestrictionDao
import com.anis.child.data.local.AppRestrictionEntity
import com.anis.child.data.repository.EmbeddingRepository
import com.anis.child.data.repository.FCMRepository
import com.anis.child.util.getAppLabel
import com.anis.child.worker.AppRestrictionWatchdogWorker
import com.anis.child.worker.LocationTelemetryWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject lateinit var fcmRepository: FCMRepository
    @Inject lateinit var appRestrictionDao: AppRestrictionDao
    @Inject lateinit var embeddingRepository: EmbeddingRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            fcmRepository.registerToken(token)
            ensureRestrictionServiceRunning()
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        data.forEach { (key, value) ->
            Log.d(TAG, "FCM data: $key = $value")
        }
        if (data.containsKey("sync_locations") || data.containsKey("trigger_sync")) {
            LocationTelemetryWorker.triggerImmediateSync(this)
        }
        val action = data["action"]
        if (action == ACTION_SYNC_APP_STATE) {
            val packageId = data["packageId"] ?: return
            CoroutineScope(Dispatchers.IO).launch {
                processAppStateUpdate(packageId, data)
            }
        }
        // DEACTIVATED: temporarily disabled receiving new embeddings from server
        // if (action == ACTION_SYNC_PROMPTS) {
        //     val promptId = data["promptId"] ?: return
        //     CoroutineScope(Dispatchers.IO).launch {
        //         val success = embeddingRepository.fetchAndSaveEmbeddings(promptId)
        //         if (success) {
        //             Log.d(TAG, "SYNC_PROMPTS completed for promptId=$promptId")
        //         } else {
        //             Log.e(TAG, "SYNC_PROMPTS failed for promptId=$promptId")
        //         }
        //     }
        // }
    }

    private suspend fun ensureRestrictionServiceRunning() {
        try {
            val blockedApps = appRestrictionDao.getBlockedApps()
            if (blockedApps.isNotEmpty()) {
                AppRestrictionService.start(this)
                AppRestrictionWatchdogWorker.enqueue(this)
                Log.d(TAG, "Auto-started AppRestrictionService (${blockedApps.size} blocked apps)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking blocked apps on startup", e)
        }
    }

    private suspend fun processAppStateUpdate(packageId: String, data: Map<String, String>) {
        try {
            val isBlocked = data["isBlocked"]?.toBooleanStrictOrNull()
            val dailyLimit = data["dailyLimit"]?.toIntOrNull()
            Log.d(TAG, "Processing SYNC_APP_STATE: pkg=$packageId isBlocked=$isBlocked dailyLimit=$dailyLimit")
            val existing = appRestrictionDao.getRestriction(packageId)
            val entity = (existing ?: AppRestrictionEntity(
                packageName = packageId,
                label = packageManager.getAppLabel(packageId)
            )).copy(
                isBlocked = isBlocked ?: existing?.isBlocked ?: false,
                dailyTimeLimitMinutes = dailyLimit ?: existing?.dailyTimeLimitMinutes ?: 0
            )
            appRestrictionDao.upsert(entity)
            val verify = appRestrictionDao.getRestriction(packageId)
            Log.d(TAG, "Upserted restriction for $packageId: isBlocked=${entity.isBlocked}, verify=${verify?.isBlocked}")
            if (entity.isBlocked || entity.dailyTimeLimitMinutes > 0) {
                AppRestrictionService.start(this)
                AppRestrictionWatchdogWorker.enqueue(this)
                Log.d(TAG, "Auto-started AppRestrictionService")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing app state update", e)
        }
    }

    companion object {
        private const val TAG = "FCMService"
        private const val ACTION_SYNC_APP_STATE = "SYNC_APP_STATE"
        private const val ACTION_SYNC_PROMPTS = "SYNC_PROMPTS"
    }
}
