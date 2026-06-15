package com.anis.child.util

fun formatDuration(ms: Long): String {
    val minutes = (ms / 60000).toInt()
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}
