package com.anis.child.network

import android.content.Context
import com.anis.child.data.LogManager
import com.anis.child.data.PreferenceManager
import com.anis.child.data.repository.LocationRepository
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
    private var preferenceManager: PreferenceManager? = null
    private var locationRepository: LocationRepository? = null

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun providePreferenceManager(): PreferenceManager {
        if (preferenceManager == null) {
            preferenceManager = PreferenceManager(appContext!!)
        }
        return preferenceManager!!
    }

    fun provideLocationRepository(): LocationRepository {
        if (locationRepository == null) {
            locationRepository = LocationRepository(
                providePreferenceManager(),
                provideApiService()
            )
        }
        return locationRepository!!
    }

    private fun provideOkHttpClient(): OkHttpClient {
        if (okHttpClient == null) {
            val ctx = appContext
                ?: throw IllegalStateException("NetworkProvider not initialized. Call init(context) in Application.onCreate().")
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val logManager = LogManager(ctx)

            okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(providePreferenceManager()))
                .addInterceptor(AppLoggingInterceptor(logManager))
                .addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("Content-Type", "application/json")
                        .method(original.method, original.body)
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }
        return okHttpClient!!
    }

    fun provideApiService(): ApiService {
        if (apiService == null) {
            val contentType = "application/json".toMediaType()
            retrofit = Retrofit.Builder()
                .baseUrl("https://api.anis.solutions/api/v1/")
                .client(provideOkHttpClient())
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
            apiService = retrofit!!.create(ApiService::class.java)
        }
        return apiService!!
    }
}
