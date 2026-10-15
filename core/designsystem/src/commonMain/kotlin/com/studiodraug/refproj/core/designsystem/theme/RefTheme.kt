package com.studiodraug.refproj.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalRefExtendedColors = staticCompositionLocalOf { LightExtendedColors }
private val LocalRefSpacing = staticCompositionLocalOf { RefSpacing() }
private val LocalRefSizes = staticCompositionLocalOf { RefSizes() }
private val LocalRefElevation = staticCompositionLocalOf { RefElevation() }

/**
 * Root theme. Every screen and preview must be wrapped in [RefTheme].
 *
 * Material roles (colours, typography, shapes) come from [MaterialTheme]; tokens Material does not
 * have (extended colours, spacing, sizes, elevation) from the [RefTheme] object.
 */
@Composable
fun RefTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalRefExtendedColors provides if (darkTheme) DarkExtendedColors else LightExtendedColors,
        LocalRefSpacing provides RefSpacing(),
        LocalRefSizes provides RefSizes(),
        LocalRefElevation provides RefElevation(),
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = RefTypography,
            shapes = RefShapes,
            content = content,
        )
    }
}

/** Design tokens beyond Material 3: `RefTheme.spacing.lg`, `RefTheme.colors.success`, … */
object RefTheme {
    val colors: RefExtendedColors
        @Composable @ReadOnlyComposable
        get() = LocalRefExtendedColors.current

    val spacing: RefSpacing
        @Composable @ReadOnlyComposable
        get() = LocalRefSpacing.current

    val sizes: RefSizes
        @Composable @ReadOnlyComposable
        get() = LocalRefSizes.current

    val elevation: RefElevation
        @Composable @ReadOnlyComposable
        get() = LocalRefElevation.current
}
