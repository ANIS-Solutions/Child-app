package com.anis.child.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screen_time_config")
data class ScreenTimeConfigEntity(
    @PrimaryKey val id: Int = 1,
    val dailyLimitMinutes: Int = 0,
    val bedtimeStartHour: Int = 21,
    val bedtimeStartMinute: Int = 0,
    val bedtimeEndHour: Int = 7,
    val bedtimeEndMinute: Int = 0,
    val studyStartHour: Int = 8,
    val studyStartMinute: Int = 0,
    val studyEndHour: Int = 14,
    val studyEndMinute: Int = 0,
    val temporaryRestrictionUntil: Long? = null,
    val extraTimeEarnedMinutes: Int = 0
)
