package com.anis.child.content

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.anis.child.R
import com.anis.child.data.ScreenTimeManager.BlockReason
import com.anis.child.util.getAppLabel

object BlockingOverlayManager {
    private var overlayView: ViewGroup? = null
    private var windowManager: WindowManager? = null
    private var currentBlockedApp: String? = null

    fun showOverlay(
        context: Context,
        packageName: String,
        accessibilityOverlay: Boolean = true,
        reason: BlockReason = BlockReason.PARENT_BLOCK
    ) {
        try {
            if (overlayView != null) hideOverlay()
            if (!canDrawOverlays(context, accessibilityOverlay)) return

            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val inflater = LayoutInflater.from(context)
            overlayView = inflater.inflate(R.layout.overlay_blocked_app, null) as ViewGroup

            val label = context.packageManager.getAppLabel(packageName)
            currentBlockedApp = packageName

            overlayView?.findViewById<TextView>(R.id.blocked_app_name)?.text = label

            val isTimeLimit = reason == BlockReason.TIME_LIMIT
            val assetName = if (isTimeLimit) "TimeLimit_app.png" else "Blocked_app.png"

            try {
                val bmp = context.assets.open(assetName).use { BitmapFactory.decodeStream(it) }
                overlayView?.findViewById<ImageView>(R.id.blocking_icon)?.setImageBitmap(bmp)
            } catch (_: Exception) {}

            overlayView?.findViewById<TextView>(R.id.blocking_title)?.text =
                if (isTimeLimit) "You have reached app time limit today" else "This app is restricted"

            overlayView?.findViewById<TextView>(R.id.blocking_description)?.text =
                if (isTimeLimit) "Please close the app and try again tomorrow"
                else "The current application has been blocked by your parent\u2019s settings"

            val type = when {
                accessibilityOverlay && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else -> WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.FILL }

            windowManager?.addView(overlayView, params)
        } catch (_: Exception) {}
    }

    fun hideOverlay() {
        try {
            overlayView?.let { windowManager?.removeView(it) }
        } catch (_: Exception) {}
        overlayView = null
        windowManager = null
        currentBlockedApp = null
    }

    fun isShowing(): Boolean = overlayView != null

    fun getBlockedApp(): String? = currentBlockedApp

    private fun canDrawOverlays(context: Context, accessibilityOverlay: Boolean): Boolean {
        if (accessibilityOverlay && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.provider.Settings.canDrawOverlays(context)
        } else true
    }
}
