package com.anis.child.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyUsageApp(
    @SerialName("packageName") val packageName: String,
    @SerialName("totalAppTimeMinutes") val totalAppTimeMinutes: Int
)

@Serializable
data class DailyUsageReport(
    @SerialName("date") val date: Long,
    @SerialName("totalScreenTimeMinutes") val totalScreenTimeMinutes: Int,
    @SerialName("apps") val apps: List<DailyUsageApp>
)

@Serializable
data class DailyUsageResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null
)

@Serializable
data class AppUsageRequest(
    @SerialName("duration") val duration: Int,
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("isLive") val isLive: Boolean
)
