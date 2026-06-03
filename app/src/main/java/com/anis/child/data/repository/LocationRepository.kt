package com.anis.child.data.repository

import com.anis.child.data.PreferenceManager
import com.anis.child.data.TelemetryRequest
import com.anis.child.data.TelemetryResponse
import com.anis.child.network.ApiResult
import com.anis.child.network.ApiService
import com.anis.child.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val apiService: ApiService
) {
    suspend fun sendTelemetry(lat: Double, lng: Double): ApiResult<TelemetryResponse> {
        val childId = preferenceManager.childId
        if (childId.isNullOrEmpty()) {
            return ApiResult.Error("No child ID available")
        }
        val token = preferenceManager.accessToken
        if (token.isNullOrEmpty()) {
            return ApiResult.Error("No access token available")
        }
        return safeApiCall {
            apiService.sendTelemetry(
                childId = childId,
                request = TelemetryRequest(lat = lat, lng = lng)
            )
        }
    }
}
