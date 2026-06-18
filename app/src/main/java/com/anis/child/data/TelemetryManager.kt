package com.anis.child.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.anis.child.data.local.LocationTelemetryDao
import com.anis.child.data.local.LocationTelemetryEntity
import com.anis.child.data.repository.LocationRepository
import com.anis.child.network.ApiResult
import com.anis.child.worker.LocationTelemetryWorker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: LocationTelemetryDao,
    private val logManager: LogManager,
    private val locationRepository: LocationRepository
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                CoroutineScope(Dispatchers.IO).launch {
                    val entity = saveAndSend(location.latitude, location.longitude)
                    logManager.log(
                        "Location: ${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                        if (entity == null) LogType.SUCCESS else LogType.LOCATION
                    )
                }
            }
        }
    }

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        LOCATION_UPDATE_INTERVAL
    )
        .setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
        .build()

    fun startMonitoring() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            logManager.log("Location permission not granted", LogType.ERROR)
            return
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            LocationTelemetryWorker.enqueue(context)
            logManager.log("Monitoring started (every 15 min, 100m filter)", LogType.INFO)
        } catch (e: SecurityException) {
            logManager.log("Failed to start monitoring: ${e.message}", LogType.ERROR)
            e.printStackTrace()
        }
    }

    fun stopMonitoring() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        LocationTelemetryWorker.cancel(context)
        logManager.log("Monitoring stopped", LogType.INFO)
    }

    private suspend fun saveAndSend(latitude: Double, longitude: Double): LocationTelemetryEntity? {
        val entity = LocationTelemetryEntity(
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis(),
            isSent = false
        )
        val id = dao.insert(entity)
        val saved = entity.copy(id = id)

        return when (locationRepository.sendTelemetry(latitude, longitude)) {
            is ApiResult.Success -> {
                dao.delete(id)
                logManager.log("Sent immediately: ${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}", LogType.SUCCESS)
                null
            }
            is ApiResult.Error -> {
                logManager.log("Queued for retry: ${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}", LogType.LOCATION)
                saved
            }
        }
    }

    suspend fun saveLocation(latitude: Double, longitude: Double) {
        saveAndSend(latitude, longitude)
    }

    suspend fun getUnsentCount(): Int {
        return dao.getUnsentCount()
    }

    companion object {
        private const val LOCATION_UPDATE_INTERVAL = 15 * 60 * 1000L
        private const val MIN_DISTANCE_METERS = 100f
    }
}
