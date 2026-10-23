package com.xflix.app.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val XflixScheme = darkColorScheme(
    primary = XColors.Red,
    onPrimary = XColors.TextPrimary,
    background = XColors.Black,
    onBackground = XColors.TextPrimary,
    surface = XColors.Black,
    onSurface = XColors.TextPrimary,
    surfaceVariant = XColors.SurfaceHigh,
    onSurfaceVariant = XColors.TextSecondary,
    error = XColors.Warning,
    outline = XColors.Divider,
)

@Composable
fun XflixTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = XflixScheme) {
        CompositionLocalProvider(
            LocalTextStyle provides XType.Body,
            LocalContentColor provides XColors.TextPrimary,
            content = content,
        )
    }
}
