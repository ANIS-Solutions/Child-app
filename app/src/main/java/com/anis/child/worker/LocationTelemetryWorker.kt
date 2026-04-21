package com.anis.child.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.PreferenceManager
import com.anis.child.data.TelemetryRequest
import com.anis.child.data.local.AppDatabase
import com.anis.child.network.ApiService
import com.anis.child.network.NetworkProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class LocationTelemetryWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val apiService = NetworkProvider.provideApiService(context)
    private val database = AppDatabase.getInstance(context)
    private val dao = database.locationTelemetryDao()
    private val preferenceManager = PreferenceManager(context)
    private val logManager = LogManager(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val accessToken = preferenceManager.accessToken
            if (accessToken.isNullOrEmpty()) {
                logManager.log("Worker: No access token, skipping", LogType.ERROR)
                return@withContext Result.success()
            }

            val childId = preferenceManager.childId
            if (childId.isNullOrEmpty()) {
                logManager.log("Worker: No child ID, skipping", LogType.ERROR)
                return@withContext Result.success()
            }

            val unsentLocations = dao.getUnsentLocations()
            if (unsentLocations.isEmpty()) {
                logManager.log("Worker: No unsent locations", LogType.INFO)
                return@withContext Result.success()
            }

            logManager.log("Worker: Sending ${unsentLocations.size} location(s)", LogType.INFO)

            var failedCount = 0
            for (location in unsentLocations) {
                val result = sendSingleLocation(location, childId, accessToken)
                when {
                    result -> {
                        dao.delete(location.id)
                        logManager.log("Sent: ${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}", LogType.SUCCESS)
                    }
                    else -> {
                        failedCount++
                    }
                }
            }

            if (failedCount > 0) {
                val runAttemptCount = runAttemptCount
                val delaySeconds = calculateBackoffDelay(runAttemptCount)
                logManager.log("Worker: $failedCount failed, retry in ${delaySeconds}s", LogType.ERROR)
                Result.retry()
            } else {
                logManager.log("Worker: All locations synced", LogType.SUCCESS)
                Result.success()
            }
        } catch (e: HttpException) {
            val code = e.code()
            if (code in 500..599) {
                val delaySeconds = calculateBackoffDelay(runAttemptCount)
                logManager.log("Worker: Server error $code, retry in ${delaySeconds}s", LogType.ERROR)
                Result.retry()
            } else {
                logManager.log("Worker: HTTP $code error", LogType.ERROR)
                Result.failure()
            }
        } catch (e: Exception) {
            logManager.log("Worker error: ${e.message}", LogType.ERROR)
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun sendSingleLocation(
        location: com.anis.child.data.local.LocationTelemetryEntity,
        childId: String,
        token: String
    ): Boolean {
        return try {
            val response = apiService.sendTelemetry(
                token = "Bearer $token",
                request = TelemetryRequest(
                    childId = childId,
                    lat = location.latitude,
                    lng = location.longitude
                )
            )
            response.success
        } catch (e: Exception) {
            logManager.log("Send failed: ${e.message}", LogType.ERROR)
            false
        }
    }

    private fun calculateBackoffDelay(runAttemptCount: Int): Long {
        val baseDelay = 30L
        val delay = baseDelay * (1 shl runAttemptCount.coerceAtMost(5))
        return delay.coerceAtMost(MAX_BACKOFF_SECONDS)
    }

    companion object {
        private const val TAG = "LocationTelemetryWorker"
        private const val MAX_RETRY_ATTEMPTS = 5
        private const val MAX_BACKOFF_SECONDS = 60L
        const val WORK_NAME = "location_telemetry_work"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<LocationTelemetryWorker>(
                1, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.MINUTES)
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