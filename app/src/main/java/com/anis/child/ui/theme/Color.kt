package com.anis.child.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val primary01: Color,
    val secondary500: Color,
    val success500: Color,
    val warning500: Color,
    val error500: Color,
    val game500: Color,
    val entertainment500: Color,
    val surfaceOverlay: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val surface50: Color,
    val darkTextPrimary: Color,
    val darkTextSecondary: Color,
    val darkBackground: Color,
    val darkSurface: Color,
    val darkSurfaceElevated: Color,
    val darkOnSurface: Color,
)

val LightAppColors = AppColors(
    primary01 = Color(0xFF2196F3),
    secondary500 = Color(0xFF859EA2),
    success500 = Color(0xFF4CAF50),
    warning500 = Color(0xFFFFEB3B),
    error500 = Color(0xFFF44336),
    game500 = Color(0xFFE91E63),
    entertainment500 = Color(0xFF8BC34A),
    surfaceOverlay = Color(0xFF929090),
    textPrimary = Color(0xFF012943),
    textSecondary = Color(0xFF004A7B),
    textDisabled = Color(0xFFBDBDBD),
    surface50 = Color(0xFFFEFFFE),
    darkTextPrimary = Color(0xFFFFFFFF),
    darkTextSecondary = Color(0xFFB0B0B0),
    darkBackground = Color(0xFF121212),
    darkSurface = Color(0xFF1E1E1E),
    darkSurfaceElevated = Color(0xFF252525),
    darkOnSurface = Color(0xFFE0E0E0),
)

val DarkAppColors = AppColors(
    primary01 = Color(0xFF2196F3),
    secondary500 = Color(0xFF859EA2),
    success500 = Color(0xFF4CAF50),
    warning500 = Color(0xFFFFEB3B),
    error500 = Color(0xFFF44336),
    game500 = Color(0xFFE91E63),
    entertainment500 = Color(0xFF8BC34A),
    surfaceOverlay = Color(0xFF929090),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFB0B0B0),
    textDisabled = Color(0xFF797979),
    surface50 = Color(0xFF121212),
    darkTextPrimary = Color(0xFFFFFFFF),
    darkTextSecondary = Color(0xFFB0B0B0),
    darkBackground = Color(0xFF121212),
    darkSurface = Color(0xFF1E1E1E),
    darkSurfaceElevated = Color(0xFF252525),
    darkOnSurface = Color(0xFFE0E0E0),
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
