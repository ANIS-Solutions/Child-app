package com.anis.child.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.anis.child.service.AppRestrictionService
import java.util.concurrent.TimeUnit

class AppRestrictionWatchdogWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!AppRestrictionService.isRunning) {
            AppRestrictionService.start(applicationContext)
        }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "app_restriction_watchdog"

        fun enqueue(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<AppRestrictionWatchdogWorker>(
                5, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
