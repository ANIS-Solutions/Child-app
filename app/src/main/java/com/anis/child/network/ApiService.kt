package com.anis.child.network

import com.anis.child.data.PairingRequest
import com.anis.child.data.PairingResponse
import com.anis.child.data.TelemetryRequest
import com.anis.child.data.TelemetryResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("/api/v1/children/pair")
    suspend fun pairDevice(@Body request: PairingRequest): PairingResponse

    @POST("/api/v1/locations/telemetry/")
    suspend fun sendTelemetry(
        @Header("Authorization") token: String,
        @Body request: TelemetryRequest
    ): TelemetryResponse
}