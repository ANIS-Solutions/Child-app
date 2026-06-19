package com.anis.child.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
data class RewardUpdateRequest(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("pointsCost") val pointsCost: Int,
    @SerialName("redemptionType") val redemptionType: String,
    @SerialName("deadline") val deadline: String? = null
)

@Serializable
data class RewardUpdateResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null
)
