package com.anis.child.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_restrictions")
data class AppRestrictionEntity(
    @PrimaryKey val packageName: String,
    val label: String = "",
    val category: String = "General",
    val isBlocked: Boolean = false,
    val dailyTimeLimitMinutes: Int = 0,
    val isAllowListed: Boolean = false
)
