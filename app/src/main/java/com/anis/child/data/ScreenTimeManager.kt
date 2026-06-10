package com.anis.child.data

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.anis.child.data.local.AppRestrictionDao
import com.anis.child.data.local.AppRestrictionEntity
import com.anis.child.data.local.ScreenTimeConfigDao
import com.anis.child.data.local.ScreenTimeConfigEntity
import com.anis.child.util.getAppLabel
import kotlinx.coroutines.flow.first
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

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

@Singleton
class ScreenTimeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenTimeConfigDao: ScreenTimeConfigDao,
    private val appRestrictionDao: AppRestrictionDao
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager

    fun hasUsageStatsPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false
        val appList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            0,
            System.currentTimeMillis()
        )
        return appList.isNotEmpty()
    }

    fun openUsageStatsSettings() {
        val intent = Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun getTodayScreenTimeMinutes(): Int {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            endOfDay
        )

        var totalMs = 0L
        for (usageStat in stats) {
            totalMs += usageStat.totalTimeInForeground
        }
        return (totalMs / 60000).toInt()
    }

    fun getWeeklyTotalMinutes(): Int {
        var totalMinutes = 0
        for (i in 0..6) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayStart = dayCal.timeInMillis
            dayCal.add(Calendar.DAY_OF_YEAR, 1)
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, dayStart, dayCal.timeInMillis
            )
            var dayMs = 0L
            for (stat in stats) dayMs += stat.totalTimeInForeground
            totalMinutes += (dayMs / 60000).toInt()
        }
        return totalMinutes
    }

    fun getWeeklyScreenTimeAverage(): Int = getWeeklyTotalMinutes() / 7

    fun getWeeklyAppUsage(): List<AppUsageInfo> {
        val appTotals = mutableMapOf<String, Long>()
        val appLabels = mutableMapOf<String, String>()

        for (i in 0..6) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayStart = dayCal.timeInMillis
            dayCal.add(Calendar.DAY_OF_YEAR, 1)
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, dayStart, dayCal.timeInMillis
            )
            for (stat in stats) {
                appTotals[stat.packageName] = (appTotals[stat.packageName] ?: 0L) + stat.totalTimeInForeground
                appLabels[stat.packageName] = packageManager.getAppLabel(stat.packageName)
            }
        }

        return appTotals.entries
            .filter { it.value > 60000 }
            .sortedByDescending { it.value }
            .map { (pkg, ms) ->
                AppUsageInfo(
                    packageName = pkg,
                    label = appLabels[pkg] ?: pkg,
                    totalTimeInForegroundMs = ms
                )
            }
    }

    fun getAppUsageToday(): List<AppUsageInfo> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            endOfDay
        )

        return stats
            .filter { it.totalTimeInForeground > 60000 }
            .sortedByDescending { it.totalTimeInForeground }
            .map { stat ->
                AppUsageInfo(
                    packageName = stat.packageName,
                    label = packageManager.getAppLabel(stat.packageName),
                    totalTimeInForegroundMs = stat.totalTimeInForeground
                )
            }
    }

    fun getCurrentForegroundApp(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return ""
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 5000
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        return stats.maxByOrNull { it.lastTimeUsed }?.packageName ?: ""
    }

    suspend fun getConfig(): ScreenTimeConfigEntity {
        return screenTimeConfigDao.getConfig() ?: ScreenTimeConfigEntity()
    }

    suspend fun updateConfig(config: ScreenTimeConfigEntity) {
        screenTimeConfigDao.upsert(config)
    }

    suspend fun getAllRestrictions(): List<AppRestrictionEntity> {
        return appRestrictionDao.getAllRestrictions().first()
    }

    suspend fun getSummary(): ScreenTimeSummary {
        val config = getConfig()
        val todayMinutes = getTodayScreenTimeMinutes()
        val effectiveLimit = if (config.dailyLimitMinutes > 0) {
            config.dailyLimitMinutes + config.extraTimeEarnedMinutes
        } else 0
        val limitReached = effectiveLimit > 0 && todayMinutes >= effectiveLimit

        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)

        val isBedtime = isTimeInRange(
            hour, minute,
            config.bedtimeStartHour, config.bedtimeStartMinute,
            config.bedtimeEndHour, config.bedtimeEndMinute
        )

        val isStudyHours = isTimeInRange(
            hour, minute,
            config.studyStartHour, config.studyStartMinute,
            config.studyEndHour, config.studyEndMinute
        )

        val isTemporarilyRestricted = config.temporaryRestrictionUntil != null &&
                System.currentTimeMillis() < config.temporaryRestrictionUntil!!

        return ScreenTimeSummary(
            todayTotalMinutes = todayMinutes,
            dailyLimitMinutes = effectiveLimit,
            remainingMinutes = (effectiveLimit - todayMinutes).coerceAtLeast(0),
            isLimitReached = limitReached,
            isBedtime = isBedtime,
            isStudyHours = isStudyHours,
            isTemporarilyRestricted = isTemporarilyRestricted,
            extraTimeEarned = config.extraTimeEarnedMinutes
        )
    }

    suspend fun isAppBlocked(packageName: String): Boolean {
        val restriction = appRestrictionDao.getRestriction(packageName)
        if (restriction != null && restriction.isBlocked) return true
        val summary = getSummary()
        return summary.isLimitReached || summary.isBedtime || summary.isTemporarilyRestricted
    }

    companion object {
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
    }
}
