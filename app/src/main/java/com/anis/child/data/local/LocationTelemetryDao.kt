package com.anis.child.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationTelemetryDao {

    @Insert
    suspend fun insert(location: LocationTelemetryEntity): Long

    @Query("SELECT * FROM location_telemetry WHERE isSent = 0 ORDER BY timestamp ASC")
    suspend fun getUnsentLocations(): List<LocationTelemetryEntity>

    @Query("SELECT * FROM location_telemetry ORDER BY timestamp DESC")
    fun getAllLocations(): Flow<List<LocationTelemetryEntity>>

    @Query("UPDATE location_telemetry SET isSent = 1 WHERE id = :id")
    suspend fun markAsSent(id: Long)

    @Query("DELETE FROM location_telemetry WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM location_telemetry WHERE isSent = 1")
    suspend fun deleteAllSent()

    @Query("SELECT COUNT(*) FROM location_telemetry WHERE isSent = 0")
    suspend fun getUnsentCount(): Int
}