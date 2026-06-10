package com.anis.child.ai.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

object PermissionManager {

    fun hasMediaProjectionPermission(activity: Activity): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity,
            "android.permission.MEDIA_PROJECTION"
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                "android.permission.POST_NOTIFICATIONS"
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun getMediaProjectionIntent(activity: Activity): Intent? {
        val projectionManager = activity.getSystemService<MediaProjectionManager>()
        return projectionManager?.createScreenCaptureIntent()
    }

    fun getMediaProjectionPermissionLauncherIntent(activity: Activity): Intent? {
        return getMediaProjectionIntent(activity)
    }

    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf("android.permission.POST_NOTIFICATIONS"),
                REQUEST_NOTIFICATION
            )
        }
    }

    fun requestOverlayPermission(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${activity.packageName}")
        )
        activity.startActivityForResult(intent, REQUEST_OVERLAY)
    }

    fun getMissingPermissions(context: Context, activity: Activity): List<PermissionType> {
        val missing = mutableListOf<PermissionType>()

        if (!hasNotificationPermission(context)) {
            missing.add(PermissionType.NOTIFICATION)
        }
        if (!hasOverlayPermission(context)) {
            missing.add(PermissionType.OVERLAY)
        }

        return missing
    }

    fun areAllPermissionsGranted(context: Context, activity: Activity): Boolean {
        return getMissingPermissions(context, activity).isEmpty()
    }

    const val REQUEST_MEDIA_PROJECTION = 1001
    const val REQUEST_NOTIFICATION = 1002
    const val REQUEST_OVERLAY = 1003
}

enum class PermissionType(val title: String, val description: String) {
    MEDIA_PROJECTION(
        "Screen Recording",
        "Required to capture screenshots of the screen"
    ),
    NOTIFICATION(
        "Notifications",
        "Required to show session status notification"
    ),
    OVERLAY(
        "Overlay Permission",
        "Required for advanced screen capture features"
    )
}
