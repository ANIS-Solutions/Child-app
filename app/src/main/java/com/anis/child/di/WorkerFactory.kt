package com.anis.child.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.anis.child.data.LogManager
import com.anis.child.data.PreferenceManager
import com.anis.child.data.local.LocationTelemetryDao
import com.anis.child.data.repository.LocationRepository
import com.anis.child.worker.AiFilterWatchdogWorker
import com.anis.child.worker.LocationTelemetryWorker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnisWorkerFactory @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val locationRepository: LocationRepository,
    private val dao: LocationTelemetryDao,
    private val logManager: LogManager
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            AiFilterWatchdogWorker::class.java.name ->
                AiFilterWatchdogWorker(
                    appContext, workerParameters, preferenceManager
                )
            LocationTelemetryWorker::class.java.name ->
                LocationTelemetryWorker(
                    appContext, workerParameters, locationRepository, dao, logManager
                )
            else -> null
        }
    }
}
