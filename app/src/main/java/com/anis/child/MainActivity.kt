package com.anis.child

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anis.child.ai.service.AiFilteringService
import com.anis.child.ai.util.BlurOverlayManager
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.PreferenceManager
import com.anis.child.data.TelemetryManager
import com.anis.child.ui.screen.ai.AiSessionScreen
import com.anis.child.ui.screen.ai.AiSessionViewModel
import com.anis.child.ui.screen.ai.PermissionsCheckScreen
import com.anis.child.ui.screen.ai.SessionDetailScreen
import com.anis.child.ui.screen.ai.SessionDetailViewModel
import com.anis.child.ui.screen.ai.SessionHistoryScreen
import com.anis.child.ui.screen.ai.SessionHistoryViewModel
import com.anis.child.ui.screen.contentprotection.ContentProtectionScreen
import com.anis.child.ui.screen.contentprotection.ContentProtectionViewModel
import com.anis.child.ui.screen.home.HomeScreen
import com.anis.child.ui.screen.home.HomeViewModel
import com.anis.child.ui.screen.location.LocationHistoryScreen
import com.anis.child.ui.screen.location.LocationHistoryViewModel
import com.anis.child.ui.screen.notifications.NotificationHistoryScreen
import com.anis.child.ui.screen.notifications.NotificationHistoryViewModel
import com.anis.child.ui.screen.pairing.PairingScreen
import com.anis.child.ui.screen.pairing.PairingViewModel
import com.anis.child.ui.screen.pin.PinScreen
import com.anis.child.ui.screen.pin.PinViewModel
import com.anis.child.ui.screen.reward.RewardScreen
import com.anis.child.ui.screen.reward.RewardViewModel
import com.anis.child.ui.screen.screentime.ScreenTimeScreen
import com.anis.child.ui.screen.screentime.ScreenTimeViewModel
import com.anis.child.ui.screen.settings.SettingsScreen
import com.anis.child.ui.screen.settings.SettingsViewModel
import com.anis.child.ui.screen.splash.SplashScreen
import com.anis.child.ui.screen.task.TaskScreen
import com.anis.child.ui.screen.task.TaskViewModel
import com.anis.child.ui.theme.ANISTheme
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
            if (preferenceManager.isMonitoringEnabled) {
                telemetryManager.startMonitoring()
                logManager.log("Location monitoring started", LogType.INFO)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        logManager.log("App started", LogType.INFO)

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

        setContent {
            ANISTheme {
                val navController = rememberNavController()

                fun navigateToProtected(route: String) {
                    if (preferenceManager.hasPin) {
                        navController.navigate("pin?target=${android.net.Uri.encode(route)}")
                    } else {
                        navController.navigate(route)
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("splash") {
                        SplashScreen(
                            onSplashComplete = {
                                val dest = if (preferenceManager.isLoggedIn) "home" else "pairing"
                                navController.navigate(dest) {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("pairing") {
                        val pairingViewModel: PairingViewModel = hiltViewModel()
                        PairingScreen(
                            viewModel = pairingViewModel,
                            onNavigateToHome = {
                                logManager.log("Paired successfully", LogType.SUCCESS)
                                navController.navigate("home") {
                                    popUpTo("pairing") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("home") {
                        val homeViewModel: HomeViewModel = hiltViewModel()
                        LaunchedEffect(Unit) {
                            homeViewModel.loadHomeData()
                        }
                        HomeScreen(
                            childName = preferenceManager.childName ?: "Child",
                            homeViewModel = homeViewModel,
                            onSettingsClick = { navigateToProtected("settings") },
                            onTaskClick = { navController.navigate("task") },
                            onRewardClick = { navController.navigate("reward") }
                        )
                    }

                    composable(
                        route = "pin?target={target}&isSettingUp={isSettingUp}",
                        arguments = listOf(
                            navArgument("target") {
                                type = NavType.StringType
                                defaultValue = "settings"
                            },
                            navArgument("isSettingUp") {
                                type = NavType.BoolType
                                defaultValue = false
                            }
                        )
                    ) { backStackEntry ->
                        val target = backStackEntry.arguments?.getString("target") ?: "settings"
                        val isSettingUp = backStackEntry.arguments?.getBoolean("isSettingUp") ?: false
                        val pinViewModel: PinViewModel = hiltViewModel()
                        PinScreen(
                            viewModel = pinViewModel,
                            isSettingUp = isSettingUp,
                            onVerified = {
                                navController.navigate(target) {
                                    popUpTo("home")
                                }
                            },
                            onCancel = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("settings") {
                        val settingsViewModel: SettingsViewModel = hiltViewModel()
                        LaunchedEffect(Unit) { }
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onMonitoringChange = { enabled ->
                                settingsViewModel.onMonitoringChanged(enabled)
                                if (enabled) {
                                    requestLocationPermissions()
                                }
                            },
                            onScreenTimeClick = { navController.navigate("screen_time") },
                            onContentProtectionClick = { navController.navigate("content_protection") },
                            onAiSessionClick = { navController.navigate("permissions_check") },
                            onLocationHistoryClick = { navController.navigate("location_history") },
                            onNotificationsClick = { navController.navigate("notifications") },
                            onChangePin = {
                                navController.navigate("pin?target=settings&isSettingUp=true")
                            },
                            onLogout = {
                                settingsViewModel.logout()
                                navController.navigate("pairing") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("screen_time") {
                        val screenTimeViewModel: ScreenTimeViewModel = hiltViewModel()
                        ScreenTimeScreen(
                            viewModel = screenTimeViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("content_protection") {
                        val contentProtectionViewModel: ContentProtectionViewModel = hiltViewModel()
                        ContentProtectionScreen(
                            viewModel = contentProtectionViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("location_history") {
                        val locationViewModel: LocationHistoryViewModel = hiltViewModel()
                        LocationHistoryScreen(
                            viewModel = locationViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("notifications") {
                        val notificationViewModel: NotificationHistoryViewModel = hiltViewModel()
                        NotificationHistoryScreen(
                            viewModel = notificationViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("permissions_check") {
                        PermissionsCheckScreen(
                            onBack = { navController.popBackStack() },
                            onContinue = {
                                navController.navigate("ai_session") {
                                    popUpTo("permissions_check") { inclusive = true }
                                }
                            },
                            onHistoryClick = {
                                navController.navigate("session_history")
                            }
                        )
                    }

                    composable("ai_session") {
                        val aiSessionViewModel: AiSessionViewModel = hiltViewModel()
                        AiSessionScreen(
                            viewModel = aiSessionViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("session_history") {
                        val sessionHistoryViewModel: SessionHistoryViewModel = hiltViewModel()
                        SessionHistoryScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onSessionClick = { id ->
                                navController.navigate("session_detail/$id")
                            },
                            viewModel = sessionHistoryViewModel
                        )
                    }

                    composable(
                        route = "session_detail/{sessionId}",
                        arguments = listOf(
                            navArgument("sessionId") {
                                type = NavType.LongType
                            }
                        )
                    ) { backStackEntry ->
                        val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: return@composable
                        val sessionDetailViewModel: SessionDetailViewModel = hiltViewModel()
                        SessionDetailScreen(
                            sessionId = sessionId,
                            onNavigateBack = { navController.popBackStack() },
                            viewModel = sessionDetailViewModel
                        )
                    }

                    composable("task") {
                        val taskViewModel: TaskViewModel = hiltViewModel()
                        TaskScreen(
                            viewModel = taskViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("reward") {
                        val rewardViewModel: RewardViewModel = hiltViewModel()
                        RewardScreen(
                            viewModel = rewardViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
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

    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}
