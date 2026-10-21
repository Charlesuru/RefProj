package com.studiodraug.refproj.feature.profile

import com.studiodraug.refproj.core.ui.ScreenPreview
import com.studiodraug.refproj.core.ui.ScreenPreviewGroup

/** Sample states for the debug menu's screen catalog. */
internal val profilePreviews =
    ScreenPreviewGroup(
        title = "Profile",
        previews =
            listOf(
                ScreenPreview("Signed in") { ProfileScreen(state = signedIn, onAction = {}) },
                ScreenPreview("Confirm logout") { ProfileScreen(state = signedIn.copy(showLogoutConfirmation = true), onAction = {}) },
                ScreenPreview("Logging out") { ProfileScreen(state = signedIn.copy(isLoggingOut = true), onAction = {}) },
            ),
    )

private val signedIn = ProfileUiState(displayName = "Ada", email = "ada@example.com", analyticsConsent = true, version = "1.0.0 (42)")
