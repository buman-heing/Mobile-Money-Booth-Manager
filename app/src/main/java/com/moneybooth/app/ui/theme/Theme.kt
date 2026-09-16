package com.moneybooth.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BoothGreen,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = BoothGreenContainerLight,
    onPrimaryContainer = BoothGreen,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    outline = OutlineLight,
    error = MoneyOutRed,
)

private val DarkColors = darkColorScheme(
    primary = BoothGreenDark,
    onPrimary = BoothGreenContainerDark,
    primaryContainer = BoothGreenContainerDark,
    onPrimaryContainer = BoothGreenDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    outline = OutlineDark,
    error = MoneyOutRed,
)

@Composable
fun MoneyBoothTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = MoneyBoothTypography,
        content = content,
    )
}
