package com.anis.child

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.anis.child.ai.service.AiFilteringService
import com.anis.child.ai.util.BlurOverlayManager
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.PreferenceManager
import com.anis.child.data.TelemetryManager

import com.anis.child.ui.theme.ThemeManager
import com.anis.child.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferenceManager: PreferenceManager
    @Inject lateinit var telemetryManager: TelemetryManager
    @Inject lateinit var logManager: LogManager

    private val TAG = "MainActivity"
    private var blockedOverlay: View? = null

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            AiFilteringService.grantMediaProjection(this, result.resultCode, result.data!!)
        } else {
            AiFilteringService.grantDenied(this)
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineLocationGranted || coarseLocationGranted) {
            logManager.log("Location permission granted", LogType.SUCCESS)
            val backgroundGranted = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true
            if (backgroundGranted) {
                logManager.log("Background location permission granted", LogType.SUCCESS)
            } else {
                logManager.log("Background location not granted — foreground only", LogType.INFO)
            }
            if (preferenceManager.isMonitoringEnabled) {
                telemetryManager.startMonitoring()
                logManager.log("Location monitoring started", LogType.INFO)
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        val prefs = base.getSharedPreferences("anis_simple_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("locale_language", "en") ?: "en"
        super.attachBaseContext(LocaleHelper.setLocale(base, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        logManager.log("App started", LogType.INFO)
        requestBatteryOptimizationExemption()

        BlurOverlayManager.registerReceiver(
            context = this,
            onShow = {
                Log.d(TAG, "RECEIVER: Show action received")
                showBlockedOverlay()
            },
            onHide = {
                Log.d(TAG, "RECEIVER: Hide action received")
                hideBlockedOverlay()
            }
        )

        if (preferenceManager.isMonitoringEnabled) {
            requestLocationPermissions()
        }

        if (!preferenceManager.hasIsAiFilteringEnabled()) {
            preferenceManager.isAiFilteringEnabled = true
        }
        if (preferenceManager.isAiFilteringEnabled && !AiFilteringService.isRunning) {
            AiFilteringService.start(this)
            com.anis.child.worker.AiFilterWatchdogWorker.enqueue(this)
        }

        if (intent?.getBooleanExtra(AiFilteringService.EXTRA_REQUEST_MEDIA_PROJECTION, false) == true) {
            val mpm = getSystemService("media_projection") as? MediaProjectionManager
            if (mpm != null) {
                mediaProjectionLauncher.launch(mpm.createScreenCaptureIntent())
            }
        }

        ThemeManager.init(preferenceManager)

        setContent {
            AppNavigation(
                preferenceManager = preferenceManager,
                logManager = logManager,
                onRequestLocationPermissions = { requestLocationPermissions() },
                onRecreate = { recreate() }
            )
        }
    }

    private fun showBlockedOverlay() {
        runOnUiThread {
            try {
                hideBlockedOverlay()

                val overlayView = View(this).apply {
                    setBackgroundColor(Color.argb(240, 255, 255, 255))
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    android.graphics.PixelFormat.TRANSLUCENT
                )

                windowManager?.addView(overlayView, params)
                blockedOverlay = overlayView
            } catch (e: Exception) {
                Log.e(TAG, "Error showing overlay", e)
            }
        }
    }

    private fun hideBlockedOverlay() {
        runOnUiThread {
            try {
                blockedOverlay?.let {
                    windowManager?.removeView(it)
                    blockedOverlay = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error hiding overlay", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BlurOverlayManager.unregisterReceiver(this)
        hideBlockedOverlay()
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:${packageName}")
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Battery optimization exemption request failed", e)
            }
        }
    }

    private fun requestLocationPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        locationPermissionLauncher.launch(permissions.toTypedArray())
    }
}
