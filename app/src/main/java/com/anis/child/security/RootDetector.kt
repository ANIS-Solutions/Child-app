package com.anis.child.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

object RootDetector {

    fun detect(context: Context): SecurityState {
        if (isEmulator()) return SecurityState.Emulator
        if (isDebuggable(context)) return SecurityState.Debuggable
        if (isRooted(context)) return SecurityState.Rooted
        return SecurityState.Ok
    }

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk" == Build.PRODUCT ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu")
    }

    private fun isDebuggable(context: Context): Boolean {
        return (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun isRooted(context: Context): Boolean {
        return checkSuBinary() || checkRootPackages(context) || checkTestKeys() || checkDangerousProps()
    }

    private fun checkSuBinary(): Boolean {
        val paths = arrayOf(
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun checkRootPackages(context: Context): Boolean {
        val rootPackages = arrayOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.topjohnwu.magisk",
            "io.va.exposed",
            "de.robv.android.xposed.installer"
        )
        return rootPackages.any { pkg ->
            try {
                context.packageManager.getPackageInfo(pkg, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    private fun checkTestKeys(): Boolean {
        return Build.TAGS?.contains("test-keys") == true
    }

    private fun checkDangerousProps(): Boolean {
        val dangerousProps = mapOf(
            "ro.debuggable" to "1",
            "ro.secure" to "0"
        )
        for ((prop, value) in dangerousProps) {
            try {
                val buildProp = File("/system/build.prop")
                if (buildProp.exists()) {
                    val content = buildProp.readText()
                    if (content.contains("$prop=$value")) return true
                }
            } catch (_: Exception) {}
        }
        return false
    }
}
