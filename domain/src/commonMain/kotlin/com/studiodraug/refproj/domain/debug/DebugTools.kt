package com.studiodraug.refproj.domain.debug

import com.studiodraug.refproj.domain.auth.LoginResult
import com.studiodraug.refproj.domain.auth.LoginUseCase
import com.studiodraug.refproj.domain.auth.LogoutUseCase
import com.studiodraug.refproj.domain.auth.TestAccountProvider
import com.studiodraug.refproj.domain.consent.ConsentRepository
import com.studiodraug.refproj.domain.onboarding.OnboardingRepository

// Shortcuts for the debug menu. They only combine existing business operations, so the app reaches
// the same states a user would — the debug menu never writes storage behind a repository's back.

/**
 * Jumps straight into the logged-in app: signs in with the environment's test account and, only if
 * that worked, marks onboarding as done. In this order the app goes from its current screen directly
 * to the tab shell, without passing the login screen.
 *
 * Returns `null` when the environment has no test account.
 */
class SignInWithTestAccountUseCase(
    private val testAccounts: TestAccountProvider,
    private val login: LoginUseCase,
    private val onboarding: OnboardingRepository,
) {
    suspend operator fun invoke(): LoginResult? {
        val account = testAccounts.testAccount() ?: return null
        val result = login(account.email, account.password)
        if (result is LoginResult.Success) onboarding.markCompleted()
        return result
    }
}

/**
 * Returns the app to the state of a fresh install: onboarding not done, signed out (which deletes the
 * user's tasks), analytics consent not given. Onboarding is reset first, so the app shows it at once
 * instead of the login screen in between.
 *
 * Keep this in sync: new data stored on the device must be cleared here as well.
 */
class ClearLocalDataUseCase(
    private val onboarding: OnboardingRepository,
    private val logout: LogoutUseCase,
    private val consent: ConsentRepository,
) {
    suspend operator fun invoke() {
        onboarding.reset()
        logout()
        consent.setAnalyticsConsent(false)
    }
}
