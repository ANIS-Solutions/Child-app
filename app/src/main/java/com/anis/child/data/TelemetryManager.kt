package com.anis.child.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.anis.child.data.local.AppDatabase
import com.anis.child.data.local.LocationTelemetryEntity
import com.anis.child.worker.LocationTelemetryWorker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TelemetryManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val database = AppDatabase.getInstance(context)
    private val dao = database.locationTelemetryDao()
    
    private val logManager = LogManager(context)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                CoroutineScope(Dispatchers.IO).launch {
                    saveLocation(location.latitude, location.longitude)
                    logManager.log(
                        "Location: ${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                        LogType.LOCATION
                    )
                }
            }
        }
    }

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        LOCATION_UPDATE_INTERVAL
    )
        .setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL)
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
            logManager.log("Monitoring started (every 1 hour)", LogType.INFO)
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

    suspend fun saveLocation(latitude: Double, longitude: Double) {
        val entity = LocationTelemetryEntity(
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis(),
            isSent = false
        )
        dao.insert(entity)
    }

    suspend fun getUnsentCount(): Int {
        return dao.getUnsentCount()
    }

    companion object {
        private const val LOCATION_UPDATE_INTERVAL = 60 * 60 * 1000L
        private const val FASTEST_LOCATION_INTERVAL = 30 * 60 * 1000L
        private const val MIN_DISTANCE_METERS = 100f
    }
}