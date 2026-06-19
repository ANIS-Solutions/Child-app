package com.anis.child.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import java.util.Locale

private val DarkColorScheme = darkColorScheme(
    primary = DarkAppColors.primary01,
    onPrimary = DarkAppColors.darkTextPrimary,
    secondary = DarkAppColors.secondary500,
    onSecondary = DarkAppColors.darkTextPrimary,
    tertiary = DarkAppColors.game500,
    onTertiary = DarkAppColors.darkTextPrimary,
    background = DarkAppColors.darkBackground,
    onBackground = DarkAppColors.darkTextPrimary,
    surface = DarkAppColors.darkSurface,
    onSurface = DarkAppColors.darkOnSurface,
    surfaceVariant = DarkAppColors.darkSurfaceElevated,
    onSurfaceVariant = DarkAppColors.darkTextSecondary,
    error = DarkAppColors.error500,
    onError = DarkAppColors.darkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = LightAppColors.primary01,
    onPrimary = LightAppColors.darkTextPrimary,
    secondary = LightAppColors.secondary500,
    onSecondary = LightAppColors.darkTextPrimary,
    tertiary = LightAppColors.game500,
    onTertiary = LightAppColors.darkTextPrimary,
    background = LightAppColors.surface50,
    onBackground = LightAppColors.textPrimary,
    surface = LightAppColors.surface50,
    onSurface = LightAppColors.textPrimary,
    surfaceVariant = LightAppColors.surfaceOverlay,
    onSurfaceVariant = LightAppColors.textSecondary,
    error = LightAppColors.error500,
    onError = LightAppColors.darkTextPrimary
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
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val layoutDirection = when (Locale.getDefault().language) {
        "ar" -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalLayoutDirection provides layoutDirection
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
