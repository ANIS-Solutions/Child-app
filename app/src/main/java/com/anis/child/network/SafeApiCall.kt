package com.anis.child.network

import java.io.IOException
import retrofit2.HttpException

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        ApiResult.Error(
            message = "HTTP ${e.code()}: ${e.message()}",
            code = e.code(),
            details = errorBody
        )
    } catch (e: IOException) {
        ApiResult.Error(message = "Network error: ${e.message}")
    } catch (e: Exception) {
        ApiResult.Error(message = e.message ?: "Unknown error")
    }
}
