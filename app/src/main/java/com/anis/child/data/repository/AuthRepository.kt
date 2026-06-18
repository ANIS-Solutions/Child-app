package com.anis.child.data.repository

import com.anis.child.data.ChildData
import com.anis.child.data.PairingRequest
import com.anis.child.data.PairingResponse
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
        return processPairingResult(safeApiCall {
            apiService.pairDevice(enrichRequest(request))
        })
    }

    suspend fun repairDevice(request: PairingRequest): ApiResult<ChildData> {
        return processPairingResult(safeApiCall {
            apiService.repairDevice(enrichRequest(request))
        })
    }

    private fun enrichRequest(request: PairingRequest): PairingRequest {
        return request.copy(fcmToken = request.fcmToken.ifEmpty {
            preferenceManager.fcmToken ?: ""
        })
    }

    private fun processPairingResult(result: ApiResult<PairingResponse>): ApiResult<ChildData> {
        return when (result) {
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
