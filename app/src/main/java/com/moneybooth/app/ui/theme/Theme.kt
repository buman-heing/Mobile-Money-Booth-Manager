package com.moneybooth.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = VioletDeep,
    onPrimary = Color.White,
    primaryContainer = VioletContainerLight,
    onPrimaryContainer = VioletDeep,
    secondary = MoneyIn,
    onSecondary = Color(0xFF062A3B),
    secondaryContainer = Color(0xFFDDF3FF),
    onSecondaryContainer = Color(0xFF0B4A66),
    tertiary = Magenta,
    onTertiary = Color.White,
    background = BgLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceHighLight,
    onSurfaceVariant = OnSurfaceMutedLight,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = SurfaceLowLight,
    surfaceContainer = SurfaceHighLight,
    surfaceContainerHigh = SurfaceHighLight,
    surfaceContainerHighest = SurfaceHighestLight,
    outline = OnSurfaceMutedLight,
    outlineVariant = OutlineLight,
    error = MoneyOut,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Violet,
    onPrimary = Color.White,
    primaryContainer = VioletContainerDark,
    onPrimaryContainer = Color(0xFFD9D2FF),
    secondary = MoneyIn,
    onSecondary = Color(0xFF062A3B),
    secondaryContainer = Color(0xFF123A4F),
    onSecondaryContainer = Color(0xFFBFE9FF),
    tertiary = Magenta,
    onTertiary = Color.White,
    background = BgDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceHighDark,
    onSurfaceVariant = OnSurfaceMutedDark,
    surfaceContainerLowest = BgDark,
    surfaceContainerLow = SurfaceLowDark,
    surfaceContainer = SurfaceHighDark,
    surfaceContainerHigh = SurfaceHighDark,
    surfaceContainerHighest = SurfaceHighestDark,
    outline = OnSurfaceMutedDark,
    outlineVariant = OutlineDark,
    error = MoneyOut,
    onError = Color(0xFF3B0A12),
)

private val MoneyBoothShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun MoneyBoothTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MoneyBoothTypography,
        shapes = MoneyBoothShapes,
        content = content,
    )
}
