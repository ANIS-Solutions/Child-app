package com.anis.child.di

import android.content.Context
import com.anis.child.content.AppBlockAccessibilityService
import com.anis.child.service.AppRestrictionService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface AppRestrictionController {
    fun start()
    fun stop()
}

interface BlockedAppsController {
    fun openAccessibilitySettings()
    fun sendUpdateBlockedApps()
}

class AppRestrictionControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppRestrictionController {
    override fun start() = AppRestrictionService.start(context)
    override fun stop() = AppRestrictionService.stop(context)
}

class BlockedAppsControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BlockedAppsController {
    override fun openAccessibilitySettings() = AppBlockAccessibilityService.openAccessibilitySettings(context)
    override fun sendUpdateBlockedApps() = AppBlockAccessibilityService.sendUpdateBlockedApps(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceControllerModule {
    @Binds
    @Singleton
    abstract fun bindAppRestrictionController(impl: AppRestrictionControllerImpl): AppRestrictionController

    @Binds
    @Singleton
    abstract fun bindBlockedAppsController(impl: BlockedAppsControllerImpl): BlockedAppsController
}
