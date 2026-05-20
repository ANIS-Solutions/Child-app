package com.anis.child

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.PreferenceManager
import com.anis.child.data.TelemetryManager
import com.anis.child.ui.screen.home.HomeScreen
import com.anis.child.ui.screen.home.HomeViewModel
import com.anis.child.ui.screen.pairing.PairingScreen
import com.anis.child.ui.screen.pairing.PairingViewModel
import com.anis.child.ui.screen.settings.SettingsScreen
import com.anis.child.ui.theme.ANISTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var telemetryManager: TelemetryManager
    private lateinit var logManager: LogManager

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

        preferenceManager = PreferenceManager(this)
        telemetryManager = TelemetryManager(this)
        logManager = LogManager(this)

        logManager.log("App started", LogType.INFO)

        if (preferenceManager.isMonitoringEnabled) {
            requestLocationPermissions()
        }

        setContent {
            var isDarkMode by remember { mutableStateOf(false) }

            ANISTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val isLoggedIn = preferenceManager.isLoggedIn

                    if (isLoggedIn) {
                        MainNavigation(
                            childName = preferenceManager.childName ?: "Child",
                            onLogout = {
                                telemetryManager.stopMonitoring()
                                preferenceManager.clear()
                                logManager.clear()
                                logManager.log("Logged out", LogType.INFO)
                                recreate()
                            },
                            isDarkMode = isDarkMode,
                            isMonitoringEnabled = preferenceManager.isMonitoringEnabled,
                            onDarkModeChange = { isDarkMode = it },
                            onMonitoringChange = { enabled ->
                                preferenceManager.isMonitoringEnabled = enabled
                                if (enabled) {
                                    requestLocationPermissions()
                                } else {
                                    telemetryManager.stopMonitoring()
                                    logManager.log("Location monitoring stopped", LogType.INFO)
                                }
                            }
                        )
                    } else {
                        val pairingViewModel: PairingViewModel = viewModel()
                        PairingScreen(
                            viewModel = pairingViewModel,
                            onNavigateToHome = {
                                logManager.log("Paired successfully", LogType.SUCCESS)
                                recreate()
                            }
                        )
                    }
                }
            }
        }
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

@Composable
private fun MainNavigation(
    childName: String,
    onLogout: () -> Unit,
    isDarkMode: Boolean,
    isMonitoringEnabled: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onMonitoringChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val logManager = remember { LogManager(context) }
    val homeViewModel: HomeViewModel = viewModel()
    val isSending by homeViewModel.isSending.collectAsState()
    val isSendingApps by homeViewModel.isSendingApps.collectAsState()
    val isFetchingChild by homeViewModel.isFetchingChild.collectAsState()
    val preferenceManager = remember { PreferenceManager(context) }

    LaunchedEffect(Unit) {
        if (preferenceManager.needsInitialAppSync) {
            preferenceManager.needsInitialAppSync = false
            homeViewModel.sendInstalledApps(context)
            homeViewModel.fetchChildMe(context)
        }
    }

    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(
            logManager = logManager,
            isDarkMode = isDarkMode,
            isMonitoringEnabled = isMonitoringEnabled,
            childId = preferenceManager.childId,
            onDarkModeChange = onDarkModeChange,
            onMonitoringChange = onMonitoringChange,
            onSendLocationClick = {
                homeViewModel.sendCurrentLocation(context)
            },
            isSending = isSending,
            onSendAppsClick = {
                homeViewModel.sendInstalledApps(context)
            },
            isSendingApps = isSendingApps,
            onGetMeClick = {
                homeViewModel.fetchChildMe(context)
            },
            isFetchingChild = isFetchingChild,
            onLogout = {
                onLogout()
                showSettings = false
            }
        )
    } else {
        HomeScreen(
            childName = childName,
            onSettingsClick = { showSettings = true }
        )
    }
}