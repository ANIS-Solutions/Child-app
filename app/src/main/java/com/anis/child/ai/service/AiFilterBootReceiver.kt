package com.anis.child.ai.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anis.child.data.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AiFilterBootReceiver : BroadcastReceiver() {

    @Inject lateinit var preferenceManager: PreferenceManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && preferenceManager.isAiFilteringEnabled) {
            AiFilteringService.start(context)
        }
    }
}
