package com.anis.child.data.repository

import android.content.Context
import com.anis.child.data.PreferenceManager
import com.anis.child.data.TelemetryRequest
import com.anis.child.data.TelemetryResponse
import com.anis.child.network.ApiResult
import com.anis.child.network.NetworkProvider
import com.anis.child.network.safeApiCall

class LocationRepository(context: Context) {
    private val apiService = NetworkProvider.provideApiService()
    private val preferenceManager = PreferenceManager(context)

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
