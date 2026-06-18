package com.anis.child.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.anis.child.data.DailyUsageApp
import com.anis.child.data.DailyUsageReport
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.ScreenTimeManager
import com.anis.child.network.ApiService
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyUsageWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val screenTimeManager: ScreenTimeManager,
    private val apiService: ApiService,
    private val logManager: LogManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val totalMinutes = screenTimeManager.getTodayScreenTimeMinutes()
            val apps = screenTimeManager.getAppUsageToday()
                .filter { it.totalTimeInForegroundMs >= 60_000 }
                .map { DailyUsageApp(it.packageName, (it.totalTimeInForegroundMs / 60000).toInt()) }

            val report = DailyUsageReport(
                date = System.currentTimeMillis(),
                totalScreenTimeMinutes = totalMinutes,
                apps = apps
            )

            val response = apiService.sendDailyUsage(report)
            if (response.isSuccessful) {
                logManager.log("Daily usage sent: ${totalMinutes}min, ${apps.size} apps", LogType.SUCCESS)
                Result.success()
            } else {
                val errorBody = response.errorBody()?.string()
                logManager.log("Daily usage failed: HTTP ${response.code()} ${errorBody ?: ""}", LogType.ERROR)
                if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            logManager.log("Daily usage error: ${e.message}", LogType.ERROR)
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val MAX_RETRIES = 3
        const val WORK_NAME = "daily_usage_work"

        fun enqueue(context: Context) {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 0)
            }
            val initialDelay = (cal.timeInMillis - now).coerceAtLeast(60_000L)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<DailyUsageWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        fun triggerImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<DailyUsageWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
