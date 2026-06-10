package com.anis.child.ai.di

import android.content.Context
import com.anis.child.ai.AiAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideAiAnalyzer(
        @ApplicationContext context: Context
    ): AiAnalyzer {
        return AiAnalyzer(context)
    }
}
