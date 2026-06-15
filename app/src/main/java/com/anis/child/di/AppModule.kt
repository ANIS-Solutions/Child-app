package com.anis.child.di

import android.content.Context
import com.anis.child.data.LogManager
import com.anis.child.data.PreferenceManager
import com.anis.child.data.local.AppDatabase
import com.anis.child.data.local.AppRestrictionDao
import com.anis.child.data.local.LocationTelemetryDao
import com.anis.child.data.local.AnalysisResultDao
import com.anis.child.data.local.SessionDao
import com.anis.child.data.local.ContentFilterRuleDao
import com.anis.child.data.local.NotificationInterceptDao
import com.anis.child.data.local.RewardDao
import com.anis.child.data.local.ScreenTimeConfigDao
import com.anis.child.data.local.SessionSyncDao
import com.anis.child.data.local.TaskDao
import com.anis.child.network.ApiService
import com.anis.child.network.AppLoggingInterceptor
import com.anis.child.network.AuthInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import okhttp3.MediaType.Companion.toMediaType
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun providePreferenceManager(@ApplicationContext context: Context): PreferenceManager {
        return PreferenceManager(context)
    }

    @Provides
    @Singleton
    fun provideLogManager(@ApplicationContext context: Context): LogManager {
        return LogManager(context)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideLocationTelemetryDao(database: AppDatabase): LocationTelemetryDao {
        return database.locationTelemetryDao()
    }

    @Provides
    @Singleton
    fun provideAppRestrictionDao(database: AppDatabase): AppRestrictionDao {
        return database.appRestrictionDao()
    }

    @Provides
    @Singleton
    fun provideScreenTimeConfigDao(database: AppDatabase): ScreenTimeConfigDao {
        return database.screenTimeConfigDao()
    }

    @Provides
    @Singleton
    fun provideRewardDao(database: AppDatabase): RewardDao {
        return database.rewardDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideAnalysisResultDao(database: AppDatabase): AnalysisResultDao {
        return database.analysisResultDao()
    }

    @Provides
    @Singleton
    fun provideSessionDao(database: AppDatabase): SessionDao {
        return database.sessionDao()
    }

    @Provides
    @Singleton
    fun provideContentFilterRuleDao(database: AppDatabase): ContentFilterRuleDao {
        return database.contentFilterRuleDao()
    }

    @Provides
    @Singleton
    fun provideSessionSyncDao(database: AppDatabase): SessionSyncDao {
        return database.sessionSyncDao()
    }

    @Provides
    @Singleton
    fun provideNotificationInterceptDao(database: AppDatabase): NotificationInterceptDao {
        return database.notificationInterceptDao()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        appLoggingInterceptor: AppLoggingInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(appLoggingInterceptor)
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

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://api.anis.solutions/api/v1/")
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
