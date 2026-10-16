package com.studiodraug.refproj.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale on a 4 dp grid. Features use these for padding and gaps instead of dp literals:
 * `Modifier.padding(RefTheme.spacing.lg)`.
 */
@Immutable
data class RefSpacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp,
    /** Horizontal padding between screen content and the screen edge. */
    val screenHorizontal: Dp = 16.dp,
)

/** Fixed component sizes. */
@Immutable
data class RefSizes(
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 24.dp,
    val iconLarge: Dp = 48.dp,
    /** Minimum touch target (Material and Apple HIG both require ~48/44 pt). */
    val minTouchTarget: Dp = 48.dp,
    val buttonHeight: Dp = 48.dp,
    val progressIndicator: Dp = 40.dp,
    val progressIndicatorInline: Dp = 20.dp,
    val progressStrokeInline: Dp = 2.dp,
    val divider: Dp = 1.dp,
    /** Content is centred and capped at this width on tablets and in landscape. */
    val contentMaxWidth: Dp = 600.dp,
    /** Large decorative icon, e.g. on onboarding pages. */
    val illustration: Dp = 96.dp,
    /** One dot of a pager indicator. */
    val pagerIndicatorDot: Dp = 8.dp,
)

/** Elevation tokens (tonal elevation in Material 3). */
@Immutable
data class RefElevation(
    val none: Dp = 0.dp,
    val low: Dp = 1.dp,
    val medium: Dp = 3.dp,
    val high: Dp = 6.dp,
)

internal val RefShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )
