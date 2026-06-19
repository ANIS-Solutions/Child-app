package com.anis.child.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
