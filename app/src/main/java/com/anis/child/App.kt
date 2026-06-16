package com.anis.child

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.anis.child.data.local.AppRestrictionDao
import com.anis.child.di.AnisWorkerFactory
import com.anis.child.service.AppRestrictionService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: AnisWorkerFactory
    @Inject lateinit var appRestrictionDao: AppRestrictionDao

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            ensureRestrictionServiceRunning()
        }
    }

    private suspend fun ensureRestrictionServiceRunning() {
        try {
            val blockedApps = appRestrictionDao.getBlockedApps()
            if (blockedApps.isNotEmpty()) {
                AppRestrictionService.start(this)
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
