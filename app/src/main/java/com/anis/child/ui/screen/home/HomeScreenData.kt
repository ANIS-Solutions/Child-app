package com.anis.child.ui.screen.home

import com.anis.child.data.AppUsageInfo
import java.util.Calendar

data class HomeScreenData(
    val todayMinutes: Int = 0,
    val dailyLimit: Int = 0,
    val weeklyTotal: Int = 0,
    val weeklyAverage: Int = 0,
    val topApps: List<AppUsageInfo> = emptyList(),
    val weeklyTopApps: List<AppUsageInfo> = emptyList(),
    val hasUsagePermission: Boolean = false
)

fun parseAgeFromDob(dob: String): Int? {
    return try {
        val year = dob.substring(0, 4).toInt()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        currentYear - year
    } catch (_: Exception) {
        null
    }
}
