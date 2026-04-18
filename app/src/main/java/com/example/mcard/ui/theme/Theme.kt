package com.example.mcard.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BlackWhiteColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = LightGray,
    onPrimaryContainer = Black,
    secondary = DarkGray,
    onSecondary = White,
    secondaryContainer = LightGray,
    onSecondaryContainer = Black,
    tertiary = DarkGray,
    onTertiary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = BackgroundGray,
    onSurfaceVariant = DarkGray,
    outline = LightGray,
    outlineVariant = LightGray
)

@Composable
fun McardTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = BlackWhiteColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
