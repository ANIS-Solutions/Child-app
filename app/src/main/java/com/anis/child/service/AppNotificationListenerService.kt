package com.anis.child.service

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.anis.child.data.local.NotificationInterceptDao
import com.anis.child.data.local.NotificationInterceptEntity
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.util.getAppLabel
import com.anis.child.util.registerReceiverSafe
import kotlinx.coroutines.flow.first
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppNotificationListenerService : NotificationListenerService() {

    @Inject lateinit var notificationDao: NotificationInterceptDao
    @Inject lateinit var logManager: LogManager

    private val enableReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_REQUEST_PERMISSION -> {
                    logManager.log("Notification access: open settings", LogType.INFO)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiverSafe(enableReceiver, IntentFilter(ACTION_REQUEST_PERMISSION))
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        logManager.log("Notification listener connected", LogType.SUCCESS)

        val activeNotifications = activeNotifications
        for (sbn in activeNotifications) {
            interceptNotification(sbn)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        logManager.log("Notification listener disconnected", LogType.ERROR)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        interceptNotification(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        kotlinx.coroutines.runBlocking {
            val existing = notificationDao.getAll().first()
            val match = existing.find {
                it.packageName == sbn.packageName &&
                        it.timestamp >= sbn.postTime - 5000 &&
                        it.timestamp <= sbn.postTime + 5000
            }
            if (match != null) {
                notificationDao.markAsRemoved(match.id)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(enableReceiver) } catch (_: Exception) {}
    }

    private fun interceptNotification(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notification = sbn.notification

        val extras = notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE, "")?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT, "")?.toString() ?: ""

        if (title.isNullOrEmpty() && text.isNullOrEmpty()) return
        if (packageName == this.packageName) return

        val appLabel = packageManager.getAppLabel(packageName)

        kotlinx.coroutines.runBlocking {
            notificationDao.insert(
                NotificationInterceptEntity(
                    packageName = packageName,
                    appLabel = appLabel,
                    title = title ?: "",
                    text = text ?: "",
                    timestamp = sbn.postTime,
                    isRead = false,
                    isRemoved = false
                )
            )
            logManager.log("Notification from $appLabel: ${title?.take(30)}", LogType.INFO)
        }
    }

    companion object {
        const val ACTION_REQUEST_PERMISSION = "com.anis.child.notification.REQUEST_PERMISSION"

        fun openNotificationSettings(context: Context) {
            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        fun isNotificationListenerEnabled(context: Context): Boolean {
            val enabledListeners = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            return enabledListeners != null && enabledListeners.contains(context.packageName)
        }
    }
}
