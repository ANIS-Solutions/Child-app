package com.anis.child.service

import android.util.Log
import com.anis.child.data.repository.FCMRepository
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

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            fcmRepository.registerToken(token)
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
    }

    companion object {
        private const val TAG = "FCMService"
    }
}
