package com.anis.child.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.anis.child.ai.service.AiFilteringService
import com.anis.child.data.PreferenceManager
import java.util.concurrent.TimeUnit

class AiFilterWatchdogWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = PreferenceManager(applicationContext)
        if (!prefs.isAiFilteringEnabled) {
            return Result.success()
        }
        if (!AiFilteringService.isRunning) {
            AiFilteringService.start(applicationContext)
        }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "ai_filter_watchdog"

        fun enqueue(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<AiFilterWatchdogWorker>(
                15, TimeUnit.MINUTES
            )
                .build()

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
