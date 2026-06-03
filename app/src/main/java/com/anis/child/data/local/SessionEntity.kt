package com.anis.child.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val status: String = "Idle",
    val intervalMs: Long = 1000,
    val totalCaptures: Int = 0,
    val blockedCount: Int = 0,
    val safeCount: Int = 0,
    val batteryStart: Int = 0,
    val batteryEnd: Int? = null,
    val batteryCharging: Boolean = false,
    val cpuTimeMs: Long = 0,
    val cpuUsagePercent: Double = 0.0,
    val ramPssMb: Double = 0.0
)
