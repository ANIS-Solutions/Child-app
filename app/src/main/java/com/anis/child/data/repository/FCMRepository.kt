package com.anis.child.data.repository

import android.content.Context
import com.anis.child.data.FcmTokenRequest
import com.anis.child.data.FcmTokenResponse
import com.anis.child.data.PreferenceManager
import com.anis.child.network.ApiResult
import com.anis.child.network.NetworkProvider
import com.anis.child.network.safeApiCall

class FCMRepository(context: Context) {
    private val apiService = NetworkProvider.provideApiService()
    private val preferenceManager = PreferenceManager(context)

    val savedFcmToken: String? get() = preferenceManager.fcmToken

    suspend fun registerToken(fcmToken: String): ApiResult<FcmTokenResponse> {
        preferenceManager.fcmToken = fcmToken
        return safeApiCall {
            apiService.registerFcmToken(FcmTokenRequest(fcmToken))
        }
    }
}
