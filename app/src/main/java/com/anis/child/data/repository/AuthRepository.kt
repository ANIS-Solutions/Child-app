package com.anis.child.data.repository

import com.anis.child.data.ChildData
import com.anis.child.data.PairingRequest
import com.anis.child.data.PreferenceManager
import com.anis.child.network.ApiResult
import com.anis.child.network.ApiService
import com.anis.child.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val apiService: ApiService
) {
    val savedFcmToken: String? get() = preferenceManager.fcmToken

    suspend fun pairDevice(request: PairingRequest): ApiResult<ChildData> {
        val enrichedRequest = request.copy(fcmToken = request.fcmToken.ifEmpty {
            preferenceManager.fcmToken ?: ""
        })
        return when (val result = safeApiCall { apiService.pairDevice(enrichedRequest) }) {
            is ApiResult.Success -> {
                val response = result.data
                if (response.success && response.accessToken != null) {
                    preferenceManager.accessToken = response.accessToken
                    preferenceManager.isLoggedIn = true
                    response.data?.let { child ->
                        preferenceManager.childId = child.id
                        preferenceManager.childName = child.name
                    }
                    ApiResult.Success(response.data!!)
                } else {
                    val errorMsg = response.message ?: "Pairing failed"
                    val details = response.errors?.joinToString("\n") { "${it.field}: ${it.message}" }
                    ApiResult.Error(errorMsg, details = details)
                }
            }
            is ApiResult.Error -> result
        }
    }
}
