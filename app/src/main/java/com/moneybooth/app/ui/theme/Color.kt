package com.moneybooth.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand: electric violet, with a magenta tail for gradients.
val Violet = Color(0xFF7B61FF)
val VioletDeep = Color(0xFF5B45E0)
val Magenta = Color(0xFFB44CFF)
val VioletContainerLight = Color(0xFFE6E0FF)
val VioletContainerDark = Color(0xFF2B2452)

// Semantic money colours (deliberately not green).
val MoneyIn = Color(0xFF38BDF8)
val MoneyOut = Color(0xFFFF6B81)
val WarningAmber = Color(0xFFFFB74D)

// Dark surfaces: near-black navy stack.
val BgDark = Color(0xFF0B0D14)
val SurfaceDark = Color(0xFF12151F)
val SurfaceLowDark = Color(0xFF161A26)
val SurfaceHighDark = Color(0xFF1C2130)
val SurfaceHighestDark = Color(0xFF232838)
val OnSurfaceDark = Color(0xFFEEF0F6)
val OnSurfaceMutedDark = Color(0xFF8A90A8)
val OutlineDark = Color(0xFF2F3548)

// Light surfaces: cool white stack.
val BgLight = Color(0xFFF5F6FB)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceLowLight = Color(0xFFF7F8FC)
val SurfaceHighLight = Color(0xFFEEF0F8)
val SurfaceHighestLight = Color(0xFFE7E9F3)
val OnSurfaceLight = Color(0xFF12141C)
val OnSurfaceMutedLight = Color(0xFF6B7188)
val OutlineLight = Color(0xFFDDE0EC)

val BrandGradient = Brush.linearGradient(listOf(VioletDeep, Violet, Magenta))
