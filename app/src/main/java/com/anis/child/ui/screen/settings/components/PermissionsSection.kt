package com.anis.child.ui.screen.settings.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun PermissionsSection() {
    val appColors = LocalAppColors.current
    val context = LocalContext.current

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
            SectionHeader(stringResource(R.string.permissions_section))

            val permissions = listOf(
                Manifest.permission.CAMERA to stringResource(R.string.permission_camera),
                Manifest.permission.ACCESS_FINE_LOCATION to stringResource(R.string.permission_precise_location),
                Manifest.permission.ACCESS_COARSE_LOCATION to stringResource(R.string.permission_approx_location),
                Manifest.permission.INTERNET to stringResource(R.string.permission_internet)
            )

            permissions.forEach { (permission, label) ->
                val isGranted = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
                PermissionRow(name = label, isGranted = isGranted)
            }
        }
    }
}
