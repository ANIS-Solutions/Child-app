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

@Serializable
data class AppPackage(
    @SerialName("packageId") val packageId: String
)

@Serializable
data class PackagesIdRequest(
    @SerialName("packagesId") val packagesId: List<String>
)

@Serializable
data class AppsBulkResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null
)

@Serializable
data class ChildQuestsResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<QuestData>? = null,
    @SerialName("message") val message: String? = null
)

@Serializable
data class QuestData(
    @SerialName("_id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("rewardValue") val rewardValue: Int = 0,
    @SerialName("state") val state: String = "pending"
)

@Serializable
data class ChildRewardsResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<RewardData>? = null,
    @SerialName("message") val message: String? = null
)

@Serializable
data class RewardData(
    @SerialName("_id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("pointCost") val pointCost: Int = 0,
    @SerialName("type") val type: String = "",
    @SerialName("state") val state: String = "earned"
)

@Serializable
data class QuestUpdateRequest(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("type") val type: String,
    @SerialName("points") val points: Int,
    @SerialName("stats") val stats: String,
    @SerialName("deadline") val deadline: String? = null
)

@Serializable
data class RewardUpdateRequest(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("pointsCost") val pointsCost: Int,
    @SerialName("redemptionType") val redemptionType: String,
    @SerialName("deadline") val deadline: String? = null
)

@Serializable
data class QuestUpdateResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null
)

@Serializable
data class RewardUpdateResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null
)

@Serializable
data class SessionHighlight(
    val resultId: Long,
    val sessionId: Long,
    val timestamp: Long,
    val embedding: List<Double>
)

@Serializable
data class SessionSyncMetadata(
    val childId: String,
    val totalSessions: Int,
    @SerialName("image-highlights") val imageHighlights: List<SessionHighlight>,
    @SerialName("list-of-embedding") val listOfEmbedding: List<List<Double>>
)

@Serializable
data class ChildMeResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: ChildMeData? = null,
    @SerialName("message") val message: String? = null
)

@Serializable
data class ChildMeData(
    @SerialName("id") val id: String = "",
    @SerialName("firstName") val firstName: String = "",
    @SerialName("gender") val gender: String? = null,
    @SerialName("hobbies") val hobbies: List<String>? = null,
    @SerialName("dob") val dob: String? = null,
    @SerialName("isActive") val isActive: Boolean? = null,
    @SerialName("deviceId") val deviceId: String? = null,
    @SerialName("deviceName") val deviceName: String? = null
)