package com.anis.child.util

import com.anis.child.data.local.LocationTelemetryEntity

fun LocationTelemetryEntity.toFormattedString(): String {
    return String.format("%.6f, %.6f", latitude, longitude)
}

fun formatCoordinate(value: Double, decimals: Int = 6): String {
    return String.format("%.${decimals}f", value)
}
