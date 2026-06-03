package com.anis.child.data.repository

import com.anis.child.data.FcmTokenRequest
import com.anis.child.data.FcmTokenResponse
import com.anis.child.data.PreferenceManager
import com.anis.child.network.ApiResult
import com.anis.child.network.ApiService
import com.anis.child.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FCMRepository @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val apiService: ApiService
) {
    val savedFcmToken: String? get() = preferenceManager.fcmToken

    suspend fun registerToken(fcmToken: String): ApiResult<FcmTokenResponse> {
        preferenceManager.fcmToken = fcmToken
        return safeApiCall {
            apiService.registerFcmToken(FcmTokenRequest(fcmToken))
        }
    }
}
