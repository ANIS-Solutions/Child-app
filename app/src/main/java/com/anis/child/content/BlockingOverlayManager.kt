package com.anis.child.content

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import com.anis.child.R

object BlockingOverlayManager {
    private var overlayView: ViewGroup? = null
    private var windowManager: WindowManager? = null
    private var currentBlockedApp: String? = null

    fun showOverlay(context: Context, packageName: String, accessibilityOverlay: Boolean = true) {
        try {
            if (overlayView != null) hideOverlay()
            if (!canDrawOverlays(context, accessibilityOverlay)) return

            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val inflater = LayoutInflater.from(context)
            overlayView = inflater.inflate(R.layout.overlay_blocked_app, null) as ViewGroup

            val label = try {
                val pm = context.packageManager
                val ai = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(ai).toString()
            } catch (_: Exception) {
                packageName
            }

            overlayView?.findViewById<TextView>(R.id.blocked_app_name)?.text = label
            currentBlockedApp = packageName

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
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
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
