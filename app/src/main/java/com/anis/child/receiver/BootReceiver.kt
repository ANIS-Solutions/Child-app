package com.anis.child.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.anis.child.ai.service.AiFilteringService
import com.anis.child.data.PreferenceManager
import com.anis.child.data.TelemetryManager
import com.anis.child.data.local.AppRestrictionDao
import com.anis.child.service.AppRestrictionService
import com.anis.child.worker.AiFilterWatchdogWorker
import com.anis.child.worker.AppRestrictionWatchdogWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var preferenceManager: PreferenceManager
    @Inject lateinit var appRestrictionDao: AppRestrictionDao
    @Inject lateinit var telemetryManager: TelemetryManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                val blockedApps = appRestrictionDao.getBlockedApps()
                if (blockedApps.isNotEmpty()) {
                    AppRestrictionService.start(context)
                    AppRestrictionWatchdogWorker.enqueue(context)
                }
            } finally {
                pendingResult.finish()
            }
        }

        if (preferenceManager.isAiFilteringEnabled) {
            AiFilteringService.start(context)
            AiFilterWatchdogWorker.enqueue(context)
        }

        if (preferenceManager.isMonitoringEnabled) {
            telemetryManager.startMonitoring()
        }

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (pm.isIgnoringBatteryOptimizations(context.packageName).not()) {
            val wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BootReceiver:lock")
            wakelock.acquire(30_000L)
        }
    }
}
