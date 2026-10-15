package com.studiodraug.refproj.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Colour tokens. Features never use these directly: they read semantic roles from
// MaterialTheme.colorScheme or RefTheme.colors. Every content/container pair is checked for
// WCAG AA contrast in ColorContrastTest.

internal val LightColorScheme: ColorScheme =
    lightColorScheme(
        primary = Color(0xFF1F5EFF),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDCE1FF),
        onPrimaryContainer = Color(0xFF00164F),
        inversePrimary = Color(0xFFB5C4FF),
        secondary = Color(0xFF006B5B),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFF9EF2DD),
        onSecondaryContainer = Color(0xFF00201A),
        tertiary = Color(0xFF6B5778),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF2DAFF),
        onTertiaryContainer = Color(0xFF251431),
        background = Color(0xFFF8F9FC),
        onBackground = Color(0xFF1A1B20),
        surface = Color(0xFFF8F9FC),
        onSurface = Color(0xFF1A1B20),
        surfaceVariant = Color(0xFFE1E2EC),
        onSurfaceVariant = Color(0xFF44464F),
        surfaceTint = Color(0xFF1F5EFF),
        inverseSurface = Color(0xFF2F3036),
        inverseOnSurface = Color(0xFFF1F0F7),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF757780),
        outlineVariant = Color(0xFFC5C6D0),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFFF8F9FC),
        surfaceContainer = Color(0xFFECEDF4),
        surfaceContainerHigh = Color(0xFFE6E7EE),
        surfaceContainerHighest = Color(0xFFE1E2E8),
        surfaceContainerLow = Color(0xFFF2F3F9),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceDim = Color(0xFFD9DAE0),
    )

internal val DarkColorScheme: ColorScheme =
    darkColorScheme(
        primary = Color(0xFFB5C4FF),
        onPrimary = Color(0xFF002A78),
        primaryContainer = Color(0xFF1A44B8),
        onPrimaryContainer = Color(0xFFDCE1FF),
        inversePrimary = Color(0xFF1F5EFF),
        secondary = Color(0xFF82D5C2),
        onSecondary = Color(0xFF003730),
        secondaryContainer = Color(0xFF005045),
        onSecondaryContainer = Color(0xFF9EF2DD),
        tertiary = Color(0xFFD6BEE4),
        onTertiary = Color(0xFF3B2948),
        tertiaryContainer = Color(0xFF523F5F),
        onTertiaryContainer = Color(0xFFF2DAFF),
        background = Color(0xFF121318),
        onBackground = Color(0xFFE2E2E9),
        surface = Color(0xFF121318),
        onSurface = Color(0xFFE2E2E9),
        surfaceVariant = Color(0xFF44464F),
        onSurfaceVariant = Color(0xFFC5C6D0),
        surfaceTint = Color(0xFFB5C4FF),
        inverseSurface = Color(0xFFE2E2E9),
        inverseOnSurface = Color(0xFF2F3036),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = Color(0xFF8F909A),
        outlineVariant = Color(0xFF44464F),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFF38393F),
        surfaceContainer = Color(0xFF1E1F25),
        surfaceContainerHigh = Color(0xFF292A2F),
        surfaceContainerHighest = Color(0xFF34343A),
        surfaceContainerLow = Color(0xFF1A1B21),
        surfaceContainerLowest = Color(0xFF0D0E13),
        surfaceDim = Color(0xFF121318),
    )

/**
 * Semantic colours Material 3 does not define. Read via `RefTheme.colors`.
 * Each role comes as a colour/on-colour pair plus a container pair, like the Material roles.
 */
@Immutable
data class RefExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
)

internal val LightExtendedColors =
    RefExtendedColors(
        success = Color(0xFF1B6D36),
        onSuccess = Color(0xFFFFFFFF),
        successContainer = Color(0xFFA6F5B5),
        onSuccessContainer = Color(0xFF00210B),
        warning = Color(0xFF8A5100),
        onWarning = Color(0xFFFFFFFF),
        warningContainer = Color(0xFFFFDCBE),
        onWarningContainer = Color(0xFF2C1600),
    )

internal val DarkExtendedColors =
    RefExtendedColors(
        success = Color(0xFF8BD89A),
        onSuccess = Color(0xFF00391A),
        successContainer = Color(0xFF005228),
        onSuccessContainer = Color(0xFFA6F5B5),
        warning = Color(0xFFFFB870),
        onWarning = Color(0xFF4A2800),
        warningContainer = Color(0xFF693C00),
        onWarningContainer = Color(0xFFFFDCBE),
    )
