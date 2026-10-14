package com.studiodraug.refproj.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Placeholder palette. Expanded into full design tokens (color, typography, spacing,
// shapes, components) in step 4.
private val LightColors =
    lightColorScheme(
        primary = Color(0xFF1F5EFF),
        onPrimary = Color.White,
        secondary = Color(0xFF00A88E),
        background = Color(0xFFF8F9FC),
        surface = Color.White,
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFF9DB6FF),
        onPrimary = Color(0xFF002A80),
        secondary = Color(0xFF4FDBC1),
        background = Color(0xFF0F1115),
        surface = Color(0xFF171A20),
    )

/** Root theme. Every screen and preview must be wrapped in [RefTheme]. */
@Composable
fun RefTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
