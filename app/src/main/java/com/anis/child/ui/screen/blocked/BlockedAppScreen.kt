package com.anis.child.ui.screen.blocked

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anis.child.data.ScreenTimeManager
import com.anis.child.data.BlockReason
import com.anis.child.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

@Composable
fun BlockedAppScreen(
    appLabel: String,
    reason: BlockReason,
    packageName: String,
    screenTimeManager: ScreenTimeManager,
    onUnblocked: () -> Unit
) {
    val appColors = LocalAppColors.current
    val context = LocalContext.current

    LaunchedEffect(packageName) {
        while (true) {
            delay(2000)
            if (screenTimeManager.getBlockReason(packageName) == BlockReason.NOT_BLOCKED) {
                onUnblocked()
                break
            }
        }
    }

    val imageBitmap = remember(reason) {
        val assetName = if (reason == BlockReason.TIME_LIMIT) "time_limit.png" else "blocked_app.png"
        try {
            context.assets.open(assetName).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }

    val titleText = if (reason == BlockReason.TIME_LIMIT) {
        "You have reached app time limit today"
    } else {
        "This app is restricted"
    }

    val descriptionText = if (reason == BlockReason.TIME_LIMIT) {
        "Please close the app and try again tomorrow"
    } else {
        "The current application has been blocked by your parent\u2019s settings"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.surface50)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            imageBitmap?.let { bmp ->
                Image(
                    bitmap = bmp,
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = titleText,
                color = appColors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = appLabel,
                modifier = Modifier.padding(top = 4.dp),
                color = appColors.textSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = descriptionText,
                modifier = Modifier.padding(top = 8.dp),
                color = appColors.textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
