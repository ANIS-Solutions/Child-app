package com.anis.child.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anis.child.R
import com.anis.child.ui.theme.LocalAppColors
import com.anis.child.ui.theme.ThemeManager
import androidx.compose.ui.res.stringResource

@Composable
fun HomeScreenHeader(
    childName: String,
    currentLanguage: String,
    onLanguageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(appColors.primary01.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = childName.firstOrNull()?.uppercase() ?: "C",
                color = appColors.primary01,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = stringResource(R.string.home_greeting, childName),
                color = appColors.textPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                color = appColors.textSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.weight(1f))

        IconButton(onClick = onLanguageClick) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(appColors.primary01.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentLanguage == "ar") "\u0639" else "EN",
                    color = appColors.primary01,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        IconButton(onClick = { ThemeManager.toggle() }) {
            Icon(
                imageVector = if (ThemeManager.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = if (ThemeManager.isDarkMode) stringResource(R.string.switch_to_light_mode) else stringResource(R.string.switch_to_dark_mode),
                tint = appColors.textSecondary
            )
        }
    }
}
