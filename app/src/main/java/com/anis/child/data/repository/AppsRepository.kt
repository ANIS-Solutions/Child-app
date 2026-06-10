package com.anis.child.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.anis.child.data.AppPackage
import com.anis.child.data.AppsBulkResponse
import com.anis.child.data.PackagesIdRequest
import com.anis.child.data.PreferenceManager
import com.anis.child.network.ApiResult
import com.anis.child.network.ApiService
import com.anis.child.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager,
    private val apiService: ApiService
) {
    private val packageManager = context.packageManager

    fun getInstalledApps(): List<AppPackage> {
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }
        val resolved = packageManager.queryIntentActivities(intent, 0)
        return resolved
            .filter {
                try {
                    val ai = packageManager.getApplicationInfo(it.activityInfo.packageName, 0)
                    (ai.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                } catch (_: Exception) {
                    false
                }
            }
            .map { AppPackage(packageId = it.activityInfo.packageName) }
    }

    suspend fun sendAppsList(apps: List<AppPackage>): ApiResult<AppsBulkResponse> {
        val token = preferenceManager.accessToken
        if (token.isNullOrEmpty()) {
            return ApiResult.Error("No access token available")
        }
        val packageIds = apps.map { it.packageId }
        return safeApiCall {
            apiService.sendAppsBulk(PackagesIdRequest(packageIds))
        }
    }
}
