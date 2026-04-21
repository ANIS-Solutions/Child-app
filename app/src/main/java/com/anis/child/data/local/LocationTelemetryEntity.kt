package com.anis.child.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_telemetry")
data class LocationTelemetryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val isSent: Boolean = false
)