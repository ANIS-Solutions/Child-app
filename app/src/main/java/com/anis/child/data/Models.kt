package com.anis.child.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PairingRequest(
    @SerialName("token") val token: String,
    @SerialName("fcmToken") val fcmToken: String = "",
    @SerialName("deviceId") val deviceId: String,
    @SerialName("deviceName") val deviceName: String
)

@Serializable
data class PairingResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("status") val status: String? = null,
    @SerialName("accessToken") val accessToken: String? = null,
    @SerialName("data") val data: ChildData? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("errors") val errors: List<ValidationError>? = null
)

@Serializable
data class ValidationError(
    @SerialName("field") val field: String,
    @SerialName("message") val message: String
)

@Serializable
data class ChildData(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String = "",
    @SerialName("age") val age: Int? = null,
    @SerialName("avatarUrl") val avatarUrl: String? = null
)

@Serializable
data class QrData(
    @SerialName("action") val action: String,
    @SerialName("childId") val childId: String? = null,
    @SerialName("token") val token: String
)

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