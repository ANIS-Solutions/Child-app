package com.anis.child.ai.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.anis.child.MainActivity

object BlurNotificationManager {

    private const val TAG = "BlurNotification"
    private const val CHANNEL_ID = "blocking_alert_channel"
    private const val ALERT_NOTIFICATION_ID = 101

    fun showBlockedContentNotification(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Content Blocking Alert",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when blocked content is detected"
                enableVibration(true)
                setShowBadge(true)
                setVibrationPattern(longArrayOf(0, 500, 200, 500))
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val fullScreenIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val largeIcon = try {
            context.assets.open("blocked_content.png").use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) { null }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("CONTENT BLOCKED")
            .setContentText("Inappropriate content detected")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setLargeIcon(largeIcon)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setColor(0xFFFF0000.toInt())
            .build()

        notificationManager.notify(ALERT_NOTIFICATION_ID, notification)
        Log.d(TAG, "High priority notification shown!")
    }

    fun cancelBlockedNotification(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(ALERT_NOTIFICATION_ID)
    }
}
