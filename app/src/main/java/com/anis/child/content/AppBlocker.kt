package com.anis.child.content

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.anis.child.data.ContentFilterManager
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.PreferenceManager
import com.anis.child.data.ScreenTimeManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBlocker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenTimeManager: ScreenTimeManager,
    private val contentFilterManager: ContentFilterManager,
    private val preferenceManager: PreferenceManager,
    private val logManager: LogManager
) {
    private val packageManager: PackageManager = context.packageManager

    fun checkAndBlock(packageName: String, accessibilityOverlay: Boolean = true) {
        if (packageName.isEmpty() || packageName == context.packageName) {
            hideOverlay()
            return
        }

        if (isSystemApp(packageName) || packageName == "com.android.settings") {
            hideOverlay()
            return
        }

        if (preferenceManager.isAiLockdownActive) {
            showOverlay(packageName, accessibilityOverlay)
            return
        }

        val isBlocked = kotlinx.coroutines.runBlocking {
            screenTimeManager.isAppBlocked(packageName)
        }

        if (isBlocked) {
            showOverlay(packageName, accessibilityOverlay)
            logManager.log("Blocked app: $packageName", LogType.ERROR)
        } else {
            hideOverlay()
        }
    }

    suspend fun checkWithContentFilter(packageName: String, accessibilityOverlay: Boolean = false) {
        if (packageName.isEmpty() || packageName == context.packageName) {
            BlockingOverlayManager.hideOverlay()
            return
        }

        if (isSystemApp(packageName) || packageName == "com.android.settings") {
            BlockingOverlayManager.hideOverlay()
            return
        }

        if (preferenceManager.isAiLockdownActive) {
            BlockingOverlayManager.showOverlay(context, packageName, accessibilityOverlay)
            return
        }

        val rules = contentFilterManager.checkText(packageName)
        if (rules.isBlocked) {
            BlockingOverlayManager.showOverlay(context, packageName, accessibilityOverlay)
            logManager.log("Blocked app via content filter: $packageName", LogType.ERROR)
        } else {
            BlockingOverlayManager.hideOverlay()
        }
    }

    private fun showOverlay(packageName: String, accessibilityOverlay: Boolean) {
        BlockingOverlayManager.showOverlay(context, packageName, accessibilityOverlay)
    }

    private fun hideOverlay() {
        BlockingOverlayManager.hideOverlay()
    }

    private fun isSystemApp(pkg: String): Boolean {
        return try {
            packageManager.getApplicationInfo(pkg, 0).flags and ApplicationInfo.FLAG_SYSTEM != 0
        } catch (_: Exception) {
            false
        }
    }
}
