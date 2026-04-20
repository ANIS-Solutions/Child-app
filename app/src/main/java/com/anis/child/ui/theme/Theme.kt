package com.anis.child.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.primary01,
    onPrimary = AppColors.darkTextPrimary,
    secondary = AppColors.secondary500,
    onSecondary = AppColors.darkTextPrimary,
    tertiary = AppColors.game500,
    onTertiary = AppColors.darkTextPrimary,
    background = AppColors.darkBackground,
    onBackground = AppColors.darkTextPrimary,
    surface = AppColors.darkSurface,
    onSurface = AppColors.darkOnSurface,
    surfaceVariant = AppColors.darkSurfaceElevated,
    onSurfaceVariant = AppColors.darkTextSecondary,
    error = AppColors.error500,
    onError = AppColors.darkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.primary01,
    onPrimary = AppColors.darkTextPrimary,
    secondary = AppColors.secondary500,
    onSecondary = AppColors.darkTextPrimary,
    tertiary = AppColors.game500,
    onTertiary = AppColors.darkTextPrimary,
    background = AppColors.surface50,
    onBackground = AppColors.textPrimary,
    surface = AppColors.surface50,
    onSurface = AppColors.textPrimary,
    surfaceVariant = AppColors.surfaceOverlay,
    onSurfaceVariant = AppColors.textSecondary,
    error = AppColors.error500,
    onError = AppColors.darkTextPrimary
)

@Composable
fun ANISTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}