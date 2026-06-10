package com.anis.child.ai.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

object BlurOverlayManager {

    private const val TAG = "BlurOverlay"
    const val ACTION_SHOW_BLOCKED = "com.anis.child.SHOW_BLOCKED"
    const val ACTION_HIDE_BLOCKED = "com.anis.child.HIDE_BLOCKED"

    private var receiver: BroadcastReceiver? = null

    fun showFrostedGlassOverlay(context: Context) {
        Log.d(TAG, "Broadcasting show action")
        try {
            val intent = Intent(ACTION_SHOW_BLOCKED)
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Broadcast error", e)
        }
    }

    fun hideBlurOverlay(context: Context) {
        Log.d(TAG, "Broadcasting hide action")
        try {
            val intent = Intent(ACTION_HIDE_BLOCKED)
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Broadcast error", e)
        }
    }

    fun registerReceiver(context: Context, onShow: () -> Unit, onHide: () -> Unit) {
        receiver?.let {
            try { context.unregisterReceiver(it) } catch (e: Exception) {}
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    ACTION_SHOW_BLOCKED -> onShow()
                    ACTION_HIDE_BLOCKED -> onHide()
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(ACTION_SHOW_BLOCKED)
            addAction(ACTION_HIDE_BLOCKED)
        }

        context.registerReceiver(receiver, filter)
        Log.d(TAG, "Receiver registered")
    }

    fun unregisterReceiver(context: Context) {
        receiver?.let {
            try { context.unregisterReceiver(it) } catch (e: Exception) {}
            receiver = null
        }
    }
}
