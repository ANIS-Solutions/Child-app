package com.anis.child

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.anis.child.ai.util.BlurOverlayManager
import com.anis.child.ui.screen.ai.AiSessionScreen
import com.anis.child.ui.screen.ai.AiSessionViewModel
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.PreferenceManager
import com.anis.child.data.TelemetryManager
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
import com.anis.child.ui.screen.splash.SplashScreen
import com.anis.child.ui.screen.task.TaskScreen
import com.anis.child.ui.screen.task.TaskViewModel
import com.anis.child.ui.theme.ANISTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

sealed class Screen {
    data object Splash : Screen()
    data object Pairing : Screen()
    data object Home : Screen()
    data object Settings : Screen()
    data object ScreenTime : Screen()
    data object ContentProtection : Screen()
    data object LocationHistory : Screen()
    data object Notifications : Screen()
    data object Task : Screen()
    data object Reward : Screen()
    data class Pin(val isSettingUp: Boolean = false) : Screen()
    data object AiSession : Screen()
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferenceManager: PreferenceManager
    @Inject lateinit var telemetryManager: TelemetryManager
    @Inject lateinit var logManager: LogManager

    private val TAG = "MainActivity"
    private var blockedOverlay: View? = null

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

        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
            var pendingProtectedScreen by remember { mutableStateOf<Screen?>(null) }

            fun navigateToProtected(target: Screen) {
                if (preferenceManager.hasPin) {
                    pendingProtectedScreen = target
                    currentScreen = Screen.Pin()
                } else {
                    currentScreen = target
                }
            }

            ANISTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (val screen = currentScreen) {
                        is Screen.Splash -> {
                            SplashScreen(
                                onSplashComplete = {
                                    currentScreen = if (preferenceManager.isLoggedIn) {
                                        Screen.Home
                                    } else {
                                        Screen.Pairing
                                    }
                                }
                            )
                        }

                        is Screen.Pairing -> {
                            val pairingViewModel: PairingViewModel = hiltViewModel()
                            PairingScreen(
                                viewModel = pairingViewModel,
                                onNavigateToHome = {
                                    logManager.log("Paired successfully", LogType.SUCCESS)
                                    currentScreen = Screen.Home
                                }
                            )
                        }

                        is Screen.Home -> {
                            val homeViewModel: HomeViewModel = hiltViewModel()
                            LaunchedEffect(Unit) {
                                homeViewModel.loadHomeData()
                            }
                            HomeScreen(
                                childName = preferenceManager.childName ?: "Child",
                                homeViewModel = homeViewModel,
                                onSettingsClick = { navigateToProtected(Screen.Settings) },
                                onTaskClick = { currentScreen = Screen.Task },
                                onRewardClick = { currentScreen = Screen.Reward }
                            )
                        }

                        is Screen.Pin -> {
                            val pinViewModel: PinViewModel = hiltViewModel()
                            PinScreen(
                                viewModel = pinViewModel,
                                isSettingUp = screen.isSettingUp,
                                onVerified = {
                                    currentScreen = pendingProtectedScreen ?: Screen.Settings
                                    pendingProtectedScreen = null
                                },
                                onCancel = {
                                    currentScreen = Screen.Home
                                    pendingProtectedScreen = null
                                }
                            )
                        }

                        is Screen.Settings -> {
                            val homeViewModel: HomeViewModel = hiltViewModel()
                            val isSending by homeViewModel.isSending.collectAsState()
                            val isSendingApps by homeViewModel.isSendingApps.collectAsState()
                            val isFetchingChild by homeViewModel.isFetchingChild.collectAsState()

                            LaunchedEffect(Unit) {
                                if (preferenceManager.needsInitialAppSync) {
                                    preferenceManager.needsInitialAppSync = false
                                    homeViewModel.sendInstalledApps(this@MainActivity)
                                    homeViewModel.fetchChildMe()
                                }
                            }

                            SettingsScreen(
                                logManager = logManager,
                                isMonitoringEnabled = preferenceManager.isMonitoringEnabled,
                                childId = preferenceManager.childId,
                                onMonitoringChange = { enabled ->
                                    preferenceManager.isMonitoringEnabled = enabled
                                    if (enabled) {
                                        requestLocationPermissions()
                                    } else {
                                        telemetryManager.stopMonitoring()
                                        logManager.log("Location monitoring stopped", LogType.INFO)
                                    }
                                },
                                onSendLocationClick = {
                                    homeViewModel.sendCurrentLocation(this@MainActivity)
                                },
                                isSending = isSending,
                                onSendAppsClick = {
                                    homeViewModel.sendInstalledApps(this@MainActivity)
                                },
                                isSendingApps = isSendingApps,
                                onGetMeClick = {
                                    homeViewModel.fetchChildMe()
                                },
                                isFetchingChild = isFetchingChild,
                                onScreenTimeClick = { currentScreen = Screen.ScreenTime },
                                onContentProtectionClick = { currentScreen = Screen.ContentProtection },
                                onAiSessionClick = { navigateToProtected(Screen.AiSession) },
                                onLocationHistoryClick = { currentScreen = Screen.LocationHistory },
                                onNotificationsClick = { currentScreen = Screen.Notifications },
                                onBack = { currentScreen = Screen.Home },
                                onChangePin = { currentScreen = Screen.Pin(isSettingUp = true) },
                                onLogout = {
                                    telemetryManager.stopMonitoring()
                                    preferenceManager.clear()
                                    logManager.clear()
                                    logManager.log("Logged out", LogType.INFO)
                                    currentScreen = Screen.Pairing
                                }
                            )
                        }

                        is Screen.ScreenTime -> {
                            val screenTimeViewModel: ScreenTimeViewModel = hiltViewModel()
                            ScreenTimeScreen(
                                viewModel = screenTimeViewModel,
                                onBack = { navigateToProtected(Screen.Settings) }
                            )
                        }

                        is Screen.LocationHistory -> {
                            val locationViewModel: LocationHistoryViewModel = hiltViewModel()
                            LocationHistoryScreen(
                                viewModel = locationViewModel,
                                onBack = { navigateToProtected(Screen.Settings) }
                            )
                        }

                        is Screen.AiSession -> {
                            val aiSessionViewModel: AiSessionViewModel = hiltViewModel()
                            AiSessionScreen(
                                viewModel = aiSessionViewModel,
                                onBack = { navigateToProtected(Screen.Settings) }
                            )
                        }

                        is Screen.ContentProtection -> {
                            val contentProtectionViewModel: ContentProtectionViewModel = hiltViewModel()
                            ContentProtectionScreen(
                                viewModel = contentProtectionViewModel,
                                onBack = { navigateToProtected(Screen.Settings) }
                            )
                        }

                        is Screen.Notifications -> {
                            val notificationViewModel: NotificationHistoryViewModel = hiltViewModel()
                            NotificationHistoryScreen(
                                viewModel = notificationViewModel,
                                onBack = { navigateToProtected(Screen.Settings) }
                            )
                        }

                        is Screen.Task -> {
                            val taskViewModel: TaskViewModel = hiltViewModel()
                            TaskScreen(
                                viewModel = taskViewModel,
                                onBack = { currentScreen = Screen.Home }
                            )
                        }

                        is Screen.Reward -> {
                            val rewardViewModel: RewardViewModel = hiltViewModel()
                            RewardScreen(
                                viewModel = rewardViewModel,
                                onBack = { currentScreen = Screen.Home }
                            )
                        }
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
