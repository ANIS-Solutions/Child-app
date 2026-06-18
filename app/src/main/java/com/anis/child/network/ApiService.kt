package com.anis.child.network

import com.anis.child.data.AppUsageRequest
import com.anis.child.data.AppsBulkResponse
import com.anis.child.data.ChildMeResponse
import com.anis.child.data.ChildQuestsResponse
import com.anis.child.data.ChildRewardsResponse
import com.anis.child.data.FcmTokenRequest
import com.anis.child.data.FcmTokenResponse
import com.anis.child.data.PackagesIdRequest
import com.anis.child.data.PairingRequest
import com.anis.child.data.PairingResponse
import com.anis.child.data.QuestUpdateRequest
import com.anis.child.data.QuestUpdateResponse
import com.anis.child.data.RewardUpdateRequest
import com.anis.child.data.RewardUpdateResponse
import com.anis.child.data.TelemetryRequest
import com.anis.child.data.TelemetryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @POST(ApiConfig.Endpoints.PAIR_DEVICE)
    suspend fun pairDevice(@Body request: PairingRequest): PairingResponse

    @POST(ApiConfig.Endpoints.REPAIR_DEVICE)
    suspend fun repairDevice(@Body request: PairingRequest): PairingResponse

    @POST(ApiConfig.Endpoints.SEND_TELEMETRY)
    suspend fun sendTelemetry(
        @Path("childId") childId: String,
        @Body request: TelemetryRequest
    ): TelemetryResponse

    @POST(ApiConfig.Endpoints.REGISTER_FCM_TOKEN)
    suspend fun registerFcmToken(@Body request: FcmTokenRequest): FcmTokenResponse

    @POST(ApiConfig.Endpoints.ADD_BULK_APPS)
    suspend fun sendAppsBulk(@Body request: PackagesIdRequest): AppsBulkResponse

    @GET(ApiConfig.Endpoints.CHILDREN_ME)
    suspend fun getChildMe(): ChildMeResponse

    @GET(ApiConfig.Endpoints.MY_QUESTS)
    suspend fun getMyQuests(): ChildQuestsResponse

    @GET(ApiConfig.Endpoints.QUESTS)
    suspend fun getChildQuests(@Path("childId") childId: String): ChildQuestsResponse

    @GET(ApiConfig.Endpoints.REWARDS)
    suspend fun getChildRewards(@Path("childId") childId: String): ChildRewardsResponse

    @PATCH(ApiConfig.Endpoints.UPDATE_QUEST)
    suspend fun updateQuest(
        @Path("childId") childId: String,
        @Path("questId") questId: String,
        @Body request: QuestUpdateRequest
    ): QuestUpdateResponse

    @Multipart
    @POST(ApiConfig.Endpoints.CHILD_SESSION)
    suspend fun syncSessions(
        @Part("childId") childId: RequestBody,
        @Part("totalSessions") totalSessions: RequestBody,
        @Part("image-highlights") imageHighlights: RequestBody,
        @Part("list-of-embedding") listOfEmbedding: RequestBody,
        @Part images: Array<MultipartBody.Part>
    ): Response<ResponseBody>

    @PATCH(ApiConfig.Endpoints.UPDATE_REWARD)
    suspend fun updateReward(
        @Path("childId") childId: String,
        @Path("rewardId") rewardId: String,
        @Body request: RewardUpdateRequest
    ): RewardUpdateResponse

    @POST(ApiConfig.Endpoints.REDEEM_REWARD)
    suspend fun redeemReward(@Path("rewardId") rewardId: String): Response<ResponseBody>

    @POST(ApiConfig.Endpoints.APP_USAGE)
    suspend fun sendAppUsage(
        @Path("packageName") packageName: String,
        @Body request: AppUsageRequest
    ): Response<ResponseBody>
}