package com.anis.child.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
data class QuestUpdateRequest(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("type") val type: String,
    @SerialName("points") val points: Int,
    @SerialName("stats") val stats: String,
    @SerialName("deadline") val deadline: String? = null
)

@Serializable
data class QuestUpdateResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null
)
