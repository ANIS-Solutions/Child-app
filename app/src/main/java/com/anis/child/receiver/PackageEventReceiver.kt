package com.anis.child.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anis.child.data.AppPackage
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.PreferenceManager
import com.anis.child.network.ApiService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackageEventReceiver : BroadcastReceiver() {

    @Inject lateinit var preferenceManager: PreferenceManager
    @Inject lateinit var apiService: ApiService
    @Inject lateinit var logManager: LogManager

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_PACKAGE_ADDED && action != Intent.ACTION_PACKAGE_REMOVED) return
        if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return

        val uri = intent.data ?: return
        val packageId = uri.schemeSpecificPart ?: return
        if (packageId == context.packageName) return

        if (!preferenceManager.isLoggedIn) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                when (action) {
                    Intent.ACTION_PACKAGE_ADDED -> {
                        val response = apiService.installApp(AppPackage(packageId))
                        if (response.isSuccessful) {
                            logManager.log("App install notified: $packageId", LogType.SUCCESS)
                        } else {
                            logManager.log("Install notify failed: $packageId ${response.code()}", LogType.ERROR)
                        }
                    }
                    Intent.ACTION_PACKAGE_REMOVED -> {
                        val childId = preferenceManager.childId ?: return@launch
                        val response = apiService.deleteApp(packageId, childId)
                        if (response.isSuccessful) {
                            logManager.log("App delete notified: $packageId", LogType.SUCCESS)
                        } else {
                            logManager.log("Delete notify failed: $packageId ${response.code()}", LogType.ERROR)
                        }
                    }
                }
            } catch (e: Exception) {
                logManager.log("Package event error: ${e.message}", LogType.ERROR)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
