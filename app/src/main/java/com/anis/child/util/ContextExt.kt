package com.anis.child.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings

fun Context.registerReceiverSafe(receiver: BroadcastReceiver, filter: IntentFilter) {
    registerReceiver(
        receiver, filter,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Context.RECEIVER_EXPORTED
        else 0
    )
}

fun Context.resolveDeviceId(): String {
    return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
}
