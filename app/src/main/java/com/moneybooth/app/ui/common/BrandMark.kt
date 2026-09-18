package com.moneybooth.app.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moneybooth.app.ui.theme.BrandGradient

/**
 * The in-app logo: a gradient tile with a coin ring and a cash-in / cash-out exchange arrow.
 * Mirrors res/drawable/ic_launcher_foreground so the launcher and the app read as one mark.
 */
@Composable
fun BrandMark(modifier: Modifier = Modifier, size: Dp = 64.dp) {
    Box(
        modifier = modifier.size(size).background(BrandGradient, RoundedCornerShape(size * 0.28f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size * 0.62f)) {
            val w = this.size.width
            val stroke = w * 0.11f
            val ring = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)

            drawCircle(color = Color.White.copy(alpha = 0.35f), radius = w / 2 - stroke / 2, style = ring)

            val arrow = Path().apply {
                moveTo(w * 0.30f, w * 0.68f)
                lineTo(w * 0.70f, w * 0.32f)
                moveTo(w * 0.46f, w * 0.32f)
                lineTo(w * 0.70f, w * 0.32f)
                lineTo(w * 0.70f, w * 0.56f)
            }
            drawPath(arrow, color = Color.White, style = ring)

            drawCircle(color = Color.White, radius = stroke * 0.7f, center = Offset(w * 0.30f, w * 0.68f))
        }
    }
}
