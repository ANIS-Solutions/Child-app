package com.anis.child.network

import com.anis.child.data.AppPackage
import com.anis.child.data.AppsBulkResponse
import com.anis.child.data.ChildMeResponse
import com.anis.child.data.FcmTokenRequest
import com.anis.child.data.FcmTokenResponse
import com.anis.child.data.PairingRequest
import com.anis.child.data.PairingResponse
import com.anis.child.data.TelemetryRequest
import com.anis.child.data.TelemetryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST(ApiConfig.Endpoints.PAIR_DEVICE)
    suspend fun pairDevice(@Body request: PairingRequest): PairingResponse

    @POST(ApiConfig.Endpoints.SEND_TELEMETRY)
    suspend fun sendTelemetry(
        @Path("childId") childId: String,
        @Body request: TelemetryRequest
    ): TelemetryResponse

    @POST(ApiConfig.Endpoints.REGISTER_FCM_TOKEN)
    suspend fun registerFcmToken(@Body request: FcmTokenRequest): FcmTokenResponse

    @POST(ApiConfig.Endpoints.ADD_BULK_APPS)
    suspend fun sendAppsBulk(@Body apps: List<AppPackage>): AppsBulkResponse

    @GET(ApiConfig.Endpoints.CHILDREN_ME)
    suspend fun getChildMe(): ChildMeResponse
}