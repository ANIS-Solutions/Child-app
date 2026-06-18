package com.anis.child

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.anis.child.data.local.AppRestrictionDao
import com.anis.child.di.AnisWorkerFactory
import com.anis.child.security.IntegrityVerifier
import com.anis.child.security.RootDetector
import com.anis.child.security.SecurityState
import com.anis.child.service.AppRestrictionService
import com.anis.child.worker.AppRestrictionWatchdogWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: AnisWorkerFactory
    @Inject lateinit var appRestrictionDao: AppRestrictionDao

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Replace with your actual Google Cloud project number
    private val cloudProjectNumber = 0L

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        appScope.launch {
            checkSecurity()
        }

        appScope.launch {
            ensureRestrictionServiceRunning()
        }
    }

    private suspend fun checkSecurity() {
        try {
            val rootState = RootDetector.detect(this)
            when (rootState) {
                is SecurityState.Emulator -> {
                    Log.w(TAG, "Device is an emulator — some features may be restricted")
                }
                is SecurityState.Rooted -> {
                    Log.e(TAG, "ROOTED DEVICE DETECTED — monitoring bypass risk")
                }
                is SecurityState.Debuggable -> {
                    Log.w(TAG, "App is debuggable — should not happen in release builds")
                }
                is SecurityState.Ok -> {
                    Log.d(TAG, "Root check passed")
                }
                else -> {}
            }

            if (cloudProjectNumber > 0L) {
                val integrityVerifier = IntegrityVerifier(this, cloudProjectNumber)
                val integrityState = integrityVerifier.verify()
                if (integrityState is SecurityState.IntegrityFailed) {
                    Log.e(TAG, "Integrity check failed: ${integrityState.reason}")
                }
            } else {
                Log.d(TAG, "Play Integrity not configured — set cloudProjectNumber to enable")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Security check error", e)
        }
    }

    private suspend fun ensureRestrictionServiceRunning() {
        try {
            val blockedApps = appRestrictionDao.getBlockedApps()
            if (blockedApps.isNotEmpty()) {
                AppRestrictionService.start(this)
                AppRestrictionWatchdogWorker.enqueue(this)
                Log.d(TAG, "Auto-started AppRestrictionService on app launch (${blockedApps.size} blocked apps)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting restriction service", e)
        }
    }

    companion object {
        private const val TAG = "ANISApp"
    }
}
