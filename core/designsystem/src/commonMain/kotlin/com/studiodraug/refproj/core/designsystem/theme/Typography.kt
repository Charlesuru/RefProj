package com.studiodraug.refproj.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// Type scale tokens (Material 3 roles). The platform system font is used on purpose: no font
// licence to manage, and it follows the user's accessibility settings on both platforms.
// Swap FontFamily.Default for a bundled brand font (composeResources/font) when one exists.

private val BrandFontFamily = FontFamily.Default

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacing: TextUnit = 0.sp,
) = TextStyle(
    fontFamily = BrandFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing,
)

internal val RefTypography =
    Typography(
        displayLarge = style(57, 64, FontWeight.Normal, (-0.25).sp),
        displayMedium = style(45, 52, FontWeight.Normal),
        displaySmall = style(36, 44, FontWeight.Normal),
        headlineLarge = style(32, 40, FontWeight.SemiBold),
        headlineMedium = style(28, 36, FontWeight.SemiBold),
        headlineSmall = style(24, 32, FontWeight.SemiBold),
        titleLarge = style(22, 28, FontWeight.SemiBold),
        titleMedium = style(16, 24, FontWeight.Medium, 0.15.sp),
        titleSmall = style(14, 20, FontWeight.Medium, 0.1.sp),
        bodyLarge = style(16, 24, FontWeight.Normal, 0.5.sp),
        bodyMedium = style(14, 20, FontWeight.Normal, 0.25.sp),
        bodySmall = style(12, 16, FontWeight.Normal, 0.4.sp),
        labelLarge = style(14, 20, FontWeight.Medium, 0.1.sp),
        labelMedium = style(12, 16, FontWeight.Medium, 0.5.sp),
        labelSmall = style(11, 16, FontWeight.Medium, 0.5.sp),
    )
