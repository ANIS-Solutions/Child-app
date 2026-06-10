package com.anis.child.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): SessionEntity?

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    fun getSessionByIdFlow(sessionId: Long): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?

    @Query("UPDATE sessions SET endTime = :endTime, status = 'COMPLETED', totalCaptures = :totalCaptures, blockedCount = :blockedCount, safeCount = :safeCount WHERE id = :sessionId")
    suspend fun endSession(sessionId: Long, endTime: Long, totalCaptures: Int, blockedCount: Int, safeCount: Int)

    @Query("""
        UPDATE sessions SET batteryEnd = :batteryEnd,
        batteryCharging = :batteryCharging,
        cpuTimeMs = :cpuTimeMs,
        cpuUsagePercent = :cpuUsagePercent,
        ramPssMb = :ramPssMb
        WHERE id = :sessionId
    """)
    suspend fun updateDeviceStats(
        sessionId: Long,
        batteryEnd: Int,
        batteryCharging: Boolean,
        cpuTimeMs: Long,
        cpuUsagePercent: Double,
        ramPssMb: Double
    )

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}
