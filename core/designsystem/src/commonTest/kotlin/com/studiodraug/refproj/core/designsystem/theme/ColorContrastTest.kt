package com.studiodraug.refproj.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertTrue

// UNIT TEST

/**
 * Every text/icon colour must be readable on the colour it is drawn on: WCAG 2.1 AA requires a
 * contrast ratio of at least 4.5:1 for normal text. Fails when a palette change breaks accessibility.
 */
class ColorContrastTest {
    @Test
    fun lightScheme_meetsWcagAa() = assertReadable(LightColorScheme.pairs() + LightExtendedColors.pairs())

    @Test
    fun darkScheme_meetsWcagAa() = assertReadable(DarkColorScheme.pairs() + DarkExtendedColors.pairs())

    private fun assertReadable(pairs: Map<String, Pair<Color, Color>>) {
        val failures =
            pairs
                .mapValues { (_, colors) -> contrast(colors.first, colors.second) }
                .filterValues { it < WCAG_AA_NORMAL_TEXT }
        assertTrue(failures.isEmpty(), "Contrast below $WCAG_AA_NORMAL_TEXT:1 for $failures")
    }

    private fun contrast(
        foreground: Color,
        background: Color,
    ): Float {
        val a = foreground.luminance() + 0.05f
        val b = background.luminance() + 0.05f
        return max(a, b) / min(a, b)
    }

    private fun ColorScheme.pairs() =
        mapOf(
            "primary" to (onPrimary to primary),
            "primaryContainer" to (onPrimaryContainer to primaryContainer),
            "secondary" to (onSecondary to secondary),
            "secondaryContainer" to (onSecondaryContainer to secondaryContainer),
            "tertiary" to (onTertiary to tertiary),
            "tertiaryContainer" to (onTertiaryContainer to tertiaryContainer),
            "error" to (onError to error),
            "errorContainer" to (onErrorContainer to errorContainer),
            "background" to (onBackground to background),
            "surface" to (onSurface to surface),
            "surfaceVariant" to (onSurfaceVariant to surfaceVariant),
            "surfaceContainerHighest" to (onSurface to surfaceContainerHighest),
            "inverseSurface" to (inverseOnSurface to inverseSurface),
            "primaryOnSurface" to (primary to surface),
            "errorOnSurface" to (error to surface),
        )

    private fun RefExtendedColors.pairs() =
        mapOf(
            "success" to (onSuccess to success),
            "successContainer" to (onSuccessContainer to successContainer),
            "warning" to (onWarning to warning),
            "warningContainer" to (onWarningContainer to warningContainer),
        )

    private companion object {
        const val WCAG_AA_NORMAL_TEXT = 4.5f
    }
}
