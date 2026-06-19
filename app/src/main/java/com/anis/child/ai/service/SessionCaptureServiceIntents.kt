package com.anis.child.ai.service

import android.content.Context
import android.content.Intent

const val ACTION_START = "com.anis.child.ACTION_START"
const val ACTION_STOP = "com.anis.child.ACTION_STOP"
const val EXTRA_SESSION_ID = "extra_session_id"
const val EXTRA_INTERVAL_MS = "extra_interval_ms"
const val EXTRA_BLUR_TRIGGER_THRESHOLD = "extra_blur_trigger_threshold"
const val EXTRA_RESULT_CODE = "extra_result_code"
const val EXTRA_DATA = "extra_data"
const val EXTRA_AUTO_ROTATE_MAX_CAPTURES = "extra_auto_rotate_max_captures"

const val NOTIFICATION_ID = 1
const val CHANNEL_ID = "session_capture_channel"
const val DEFAULT_AUTO_ROTATE_MAX_CAPTURES = 900

fun startSessionCaptureService(
    context: Context,
    sessionId: Long,
    intervalMs: Int,
    blurTriggerThreshold: Int,
    resultCode: Int,
    data: Intent,
    autoRotateMaxCaptures: Int = DEFAULT_AUTO_ROTATE_MAX_CAPTURES
) {
    val intent = Intent(context, SessionCaptureService::class.java)
    intent.action = ACTION_START
    intent.putExtra(EXTRA_SESSION_ID, sessionId)
    intent.putExtra(EXTRA_INTERVAL_MS, intervalMs)
    intent.putExtra(EXTRA_BLUR_TRIGGER_THRESHOLD, blurTriggerThreshold)
    intent.putExtra(EXTRA_RESULT_CODE, resultCode)
    intent.putExtra(EXTRA_DATA, data)
    intent.putExtra(EXTRA_AUTO_ROTATE_MAX_CAPTURES, autoRotateMaxCaptures)
    context.startForegroundService(intent)
}

fun stopSessionCaptureService(context: Context) {
    val intent = Intent(context, SessionCaptureService::class.java).apply {
        action = ACTION_STOP
    }
    context.startService(intent)
}
