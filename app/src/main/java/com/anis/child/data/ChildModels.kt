package com.anis.child.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
