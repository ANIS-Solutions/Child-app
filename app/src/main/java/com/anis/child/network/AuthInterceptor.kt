package com.anis.child.network

import android.content.Context
import com.anis.child.data.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {

    private val preferenceManager = PreferenceManager(context.applicationContext)

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = preferenceManager.accessToken
        val request = if (!token.isNullOrEmpty()) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
