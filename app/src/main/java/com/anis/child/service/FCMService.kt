package com.anis.child.service

import android.util.Log
import com.anis.child.data.repository.FCMRepository
import com.anis.child.worker.LocationTelemetryWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            FCMRepository(this@FCMService).registerToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        Log.d(TAG, "Silent data received: $data")
        if (data.containsKey("sync_locations") || data.containsKey("trigger_sync")) {
            LocationTelemetryWorker.triggerImmediateSync(this)
        }
    }

    companion object {
        private const val TAG = "FCMService"
    }
}
