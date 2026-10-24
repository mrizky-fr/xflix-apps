package com.xflix.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object XColors {
    val Black = Color(0xFF000000)
    val Surface = Color(0xFF121212)
    val SurfaceRaised = Color(0xFF1B1B1B)
    val SurfaceHigh = Color(0xFF262626)
    val Input = Color(0xFF2E2E2E)
    val Divider = Color(0xFF2A2A2A)
    val Outline = Color(0x66FFFFFF)

    val Red = Color(0xFFE50914)
    val RedPressed = Color(0xFFC11119)
    val RedDeep = Color(0xFF8C0A10)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB3B3B3)
    val TextMuted = Color(0xFF808080)
    val TextDisabled = Color(0xFF595959)

    // Oranye untuk pesan error
    val Warning = Color(0xFFE87C03)
    val Positive = Color(0xFF46D369)
    val Download = Color(0xFF2E7BFF)
}

object Space {
    val xxs = 2.dp
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

// Ukuran dibuat lebih rapat
object Dimens {
    val Gutter = 12.dp
    val RowGap = 18.dp
    val PosterWidth = 102.dp
    val PosterGap = 6.dp
    val ButtonHeight = 40.dp
    val Radius = 4.dp
    val CardRadius = 6.dp
    val TopBarHeight = 48.dp
    val BottomBarHeight = 56.dp
}

object Motion {
    val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    const val Short = 160
    const val Medium = 280
    const val Long = 420
}
