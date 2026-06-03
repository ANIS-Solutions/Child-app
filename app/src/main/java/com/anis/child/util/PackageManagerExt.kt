package com.anis.child.util

import android.content.pm.PackageManager

fun PackageManager.getAppLabel(packageName: String): String {
    return try {
        val ai = getApplicationInfo(packageName, 0)
        getApplicationLabel(ai).toString()
    } catch (_: Exception) {
        packageName
    }
}
