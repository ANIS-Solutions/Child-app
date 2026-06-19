package com.anis.child.ui.screen.settings.components

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun DeviceInfoSection(childId: String?, childName: String?) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = appColors.darkSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader(stringResource(R.string.device_section))
            if (childName != null) {
                InfoRow(stringResource(R.string.device_name), childName)
            }
            if (childId != null) {
                InfoRow(stringResource(R.string.child_id), childId.take(16) + "...")
            }
            InfoRow(stringResource(R.string.device_model), Build.MODEL)
            InfoRow(stringResource(R.string.android_version), "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        }
    }
}
