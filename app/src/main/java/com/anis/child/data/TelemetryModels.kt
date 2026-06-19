package com.anis.child.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TelemetryRequest(
    @SerialName("lat") val lat: Double,
    @SerialName("lng") val lng: Double
)

@Serializable
data class TelemetryResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null
)

@Serializable
data class FcmTokenRequest(
    @SerialName("fcmToken") val fcmToken: String
)

@Serializable
data class FcmTokenResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null
)
