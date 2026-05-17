package com.anis.child.network

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object NetworkProvider {

    private var appContext: Context? = null
    private var okHttpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun provideOkHttpClient(): OkHttpClient {
        if (okHttpClient == null) {
            val ctx = appContext
                ?: throw IllegalStateException("NetworkProvider not initialized. Call init(context) in Application.onCreate().")
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(ctx))
                .addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("Content-Type", "application/json")
                        .method(original.method, original.body)
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(loggingInterceptor)
                .connectTimeout(ApiConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(ApiConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()
        }
        return okHttpClient!!
    }

    fun provideApiService(): ApiService {
        if (apiService == null) {
            val contentType = "application/json".toMediaType()
            retrofit = Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(provideOkHttpClient())
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
            apiService = retrofit!!.create(ApiService::class.java)
        }
        return apiService!!
    }
}