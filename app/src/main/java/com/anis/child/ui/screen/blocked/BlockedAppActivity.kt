package com.anis.child.ui.screen.blocked

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import com.anis.child.data.PreferenceManager
import com.anis.child.data.ScreenTimeManager
import com.anis.child.data.ScreenTimeManager.BlockReason
import com.anis.child.ui.theme.ANISTheme
import com.anis.child.ui.theme.ThemeManager
import com.anis.child.util.getAppLabel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BlockedAppActivity : ComponentActivity() {

    @Inject lateinit var screenTimeManager: ScreenTimeManager
    @Inject lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })

        val packageName = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: run {
            finish()
            return
        }

        val reason = (intent.getSerializableExtra(EXTRA_REASON) as? BlockReason) ?: BlockReason.PARENT_BLOCK
        val appLabel = packageManager.getAppLabel(packageName)

        ThemeManager.init(preferenceManager)

        setContent {
            ANISTheme(darkTheme = ThemeManager.isDarkMode) {
                BlockedAppScreen(
                    appLabel = appLabel,
                    reason = reason,
                    packageName = packageName,
                    screenTimeManager = screenTimeManager,
                    onUnblocked = { finish() }
                )
            }
        }
    }

    companion object {
        private const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
        private const val EXTRA_REASON = "block_reason"

        fun createIntent(context: Context, packageName: String, reason: BlockReason = BlockReason.PARENT_BLOCK): Intent {
            return Intent(context, BlockedAppActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_BLOCKED_PACKAGE, packageName)
                putExtra(EXTRA_REASON, reason)
            }
        }
    }
}
