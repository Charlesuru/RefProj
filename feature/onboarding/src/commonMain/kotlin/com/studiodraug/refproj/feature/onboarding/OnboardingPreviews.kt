package com.studiodraug.refproj.feature.onboarding

import com.studiodraug.refproj.core.ui.ScreenPreview
import com.studiodraug.refproj.core.ui.ScreenPreviewGroup

/** Sample states for the debug menu's screen catalog. */
internal val onboardingPreviews =
    ScreenPreviewGroup(
        title = "Onboarding",
        previews =
            listOf(
                ScreenPreview("Start") { OnboardingScreen(state = OnboardingUiState(), onAction = {}) },
                ScreenPreview("Finishing") {
                    OnboardingScreen(state = OnboardingUiState(analyticsConsent = true, isFinishing = true), onAction = {})
                },
            ),
    )
