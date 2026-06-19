package com.anis.child

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
import com.anis.child.data.LogManager
import com.anis.child.data.PreferenceManager
import com.anis.child.ui.screen.ai.SessionDetailScreen
import com.anis.child.ui.screen.ai.SessionDetailViewModel
import com.anis.child.ui.screen.ai.SessionHistoryScreen
import com.anis.child.ui.screen.ai.SessionHistoryViewModel
import com.anis.child.ui.screen.contentprotection.ContentProtectionScreen
import com.anis.child.ui.screen.contentprotection.ContentProtectionViewModel
import com.anis.child.ui.screen.home.HomeScreen
import com.anis.child.ui.screen.home.HomeViewModel
import com.anis.child.ui.screen.pairing.PairingScreen
import com.anis.child.ui.screen.pairing.PairingViewModel
import com.anis.child.ui.screen.pin.PinScreen
import com.anis.child.ui.screen.pin.PinViewModel
import com.anis.child.ui.screen.reward.RewardScreen
import com.anis.child.ui.screen.reward.RewardViewModel
import com.anis.child.ui.screen.settings.components.LogsScreen
import com.anis.child.ui.screen.settings.SettingsScreen
import com.anis.child.ui.screen.settings.SettingsViewModel
import com.anis.child.ui.screen.splash.SplashScreen
import com.anis.child.ui.screen.task.QuestScreen
import com.anis.child.ui.screen.task.QuestViewModel
import com.anis.child.ui.theme.ANISTheme
import com.anis.child.ui.theme.ThemeManager

@Composable
fun AppNavigation(
    preferenceManager: PreferenceManager,
    logManager: LogManager,
    onRequestLocationPermissions: () -> Unit,
    onRecreate: () -> Unit,
) {
    ANISTheme(darkTheme = ThemeManager.isDarkMode) {
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
                        logManager.log("Paired successfully", com.anis.child.data.LogType.SUCCESS)
                        navController.navigate("pin?target=home&isSettingUp=true") {
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
                    currentLanguage = preferenceManager.localeLanguage,
                    onSettingsClick = { navigateToProtected("settings") },
                    onTaskClick = { navController.navigate("quest") },
                    onRewardClick = { navController.navigate("reward") },
                    onLanguageChange = { languageCode ->
                        preferenceManager.localeLanguage = languageCode
                        onRecreate()
                    }
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
                        if (isSettingUp) {
                            navController.navigate(target) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            navController.navigate(target) {
                                popUpTo("home")
                            }
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
                            onRequestLocationPermissions()
                        }
                    },
                    onContentProtectionClick = { navController.navigate("content_protection") },
                    onSessionHistoryClick = { navController.navigate("session_history") },
                    onLogsClick = { navController.navigate("logs") },
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

            composable("logs") {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                LogsScreen(
                    logManager = settingsViewModel.logManager,
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

            composable("quest") {
                val taskViewModel: QuestViewModel = hiltViewModel()
                QuestScreen(
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
