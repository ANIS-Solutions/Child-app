package com.anis.child.content

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.anis.child.data.ContentFilterManager
import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import com.anis.child.data.ScreenTimeManager
import com.anis.child.data.BlockReason
import com.anis.child.ui.screen.blocked.BlockedAppActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBlocker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenTimeManager: ScreenTimeManager,
    private val contentFilterManager: ContentFilterManager,
    private val logManager: LogManager
) {
    private val packageManager: PackageManager = context.packageManager
    private var blockedAppShown: String? = null

    suspend fun checkAndBlock(packageName: String, accessibilityOverlay: Boolean = true) {
        if (packageName.isEmpty() || packageName == context.packageName) {
            blockedAppShown = null
            hideOverlay()
            return
        }

        if (isSystemApp(packageName) || packageName == "com.android.settings") {
            blockedAppShown = null
            hideOverlay()
            return
        }

        val reason = screenTimeManager.getBlockReason(packageName)

        if (reason != BlockReason.NOT_BLOCKED) {
            if (blockedAppShown != packageName) {
                blockedAppShown = packageName
                launchBlockScreen(packageName, reason)
            }
            logManager.log("Blocked app: $packageName reason=$reason", LogType.ERROR)
            return
        }

        hideOverlay()
    }

    private fun launchBlockScreen(packageName: String, reason: BlockReason) {
        BlockingOverlayManager.showOverlay(context, packageName, reason = reason)
        try {
            val intent = BlockedAppActivity.createIntent(context, packageName, reason)
            context.startActivity(intent)
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.killBackgroundProcesses(packageName)
        } catch (_: Exception) {}
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

        val reason = screenTimeManager.getBlockReason(packageName)
        if (reason != BlockReason.NOT_BLOCKED) {
            launchBlockScreen(packageName, reason)
            logManager.log("Blocked app: $packageName reason=$reason", LogType.ERROR)
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
