package com.anis.child.data

data class AppUsageInfo(
    val packageName: String,
    val label: String,
    val totalTimeInForegroundMs: Long,
    val category: String = "General"
)

data class ScreenTimeSummary(
    val todayTotalMinutes: Int,
    val dailyLimitMinutes: Int,
    val remainingMinutes: Int,
    val isLimitReached: Boolean,
    val isBedtime: Boolean,
    val isStudyHours: Boolean,
    val isTemporarilyRestricted: Boolean,
    val extraTimeEarned: Int
)

enum class BlockReason { NOT_BLOCKED, PARENT_BLOCK, TIME_LIMIT, GLOBAL_LIMIT }

fun isTimeInRange(
    hour: Int, minute: Int,
    startHour: Int, startMinute: Int,
    endHour: Int, endMinute: Int
): Boolean {
    val nowMinutes = hour * 60 + minute
    val startMinutes = startHour * 60 + startMinute
    val endMinutes = endHour * 60 + endMinute

    return if (startMinutes <= endMinutes) {
        nowMinutes in startMinutes..endMinutes
    } else {
        nowMinutes >= startMinutes || nowMinutes <= endMinutes
    }
}
