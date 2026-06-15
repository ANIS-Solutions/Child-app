package com.anis.child.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    // Core palette (always available, non-composable)
    val primary01 = Color(0xFF2196F3)
    val primary02 = Color(0xFF004879)
    val primary500 = Color(0xFF2196F3)
    val secondary500 = Color(0xFF859EA2)
    val borderAndHighlight = Color(0xFF656565)
    val success500 = Color(0xFF4CAF50)
    val warning500 = Color(0xFFFFEB3B)
    val error500 = Color(0xFFF44336)
    val game500 = Color(0xFFE91E63)
    val entertainment500 = Color(0xFF8BC34A)
    val adult500 = Color(0xFF000000)
    val chartText = Color(0xFF757575)
    val chartPrimary = Color(0xFF3F51B5)
    val surfaceOverlay = Color(0xFF929090)
    val separation = Color(0xFF686868)

    // Light theme
    val textPrimary = Color(0xFF012943)
    val textSecondary = Color(0xFF004A7B)
    val textDisabled = Color(0xFFBDBDBD)
    val surface50 = Color(0xFFFEFFFE)
    val blur = Color.White.copy(alpha = 0.6f)

    // Dark theme (always these values, used as constants)
    val darkTextPrimary = Color(0xFFFFFFFF)
    val darkTextSecondary = Color(0xFFB0B0B0)
    val darkBackground = Color(0xFF121212)
    val darkSurface = Color(0xFF1E1E1E)
    val darkSurfaceElevated = Color(0xFF252525)
    val darkOnSurface = Color(0xFFE0E0E0)

    // Theme-aware aliases (composable, switch based on system dark theme)
    @Composable
    fun themedTextPrimary(): Color = if (isSystemInDarkTheme()) darkTextPrimary else textPrimary

    @Composable
    fun themedTextSecondary(): Color = if (isSystemInDarkTheme()) darkTextSecondary else textSecondary

    @Composable
    fun themedSurface(): Color = if (isSystemInDarkTheme()) darkBackground else surface50

    @Composable
    fun themedBlur(): Color = if (isSystemInDarkTheme()) Color.Black.copy(alpha = 0.6f) else blur
}
