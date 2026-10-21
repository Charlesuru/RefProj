package com.studiodraug.refproj.feature.login

import com.studiodraug.refproj.core.ui.ScreenPreview
import com.studiodraug.refproj.core.ui.ScreenPreviewGroup
import com.studiodraug.refproj.domain.auth.EmailError
import com.studiodraug.refproj.domain.auth.PasswordError

/** Sample states for the debug menu's screen catalog. */
internal val loginPreviews =
    ScreenPreviewGroup(
        title = "Login",
        previews =
            listOf(
                ScreenPreview("Empty") { LoginScreen(state = LoginUiState(), onAction = {}) },
                ScreenPreview("Field errors") {
                    LoginScreen(
                        state = LoginUiState(email = "ada", emailError = EmailError.Invalid, passwordError = PasswordError.Blank),
                        onAction = {},
                    )
                },
                ScreenPreview("Wrong password") {
                    LoginScreen(state = filledIn.copy(error = LoginError.WrongCredentials), onAction = {})
                },
                ScreenPreview("Network error") { LoginScreen(state = filledIn.copy(error = LoginError.Network), onAction = {}) },
                ScreenPreview("Logging in") { LoginScreen(state = filledIn.copy(isLoading = true), onAction = {}) },
            ),
    )

private val filledIn = LoginUiState(email = "ada@example.com", password = "secret")
