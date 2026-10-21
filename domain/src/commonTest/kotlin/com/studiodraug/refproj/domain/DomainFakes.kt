package com.studiodraug.refproj.domain

import com.studiodraug.refproj.domain.auth.AuthRepository
import com.studiodraug.refproj.domain.auth.AuthState
import com.studiodraug.refproj.domain.auth.User
import com.studiodraug.refproj.domain.consent.ConsentRepository
import com.studiodraug.refproj.domain.model.AppResult
import com.studiodraug.refproj.domain.onboarding.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// In-memory repositories shared by the domain tests.

internal val TEST_USER = User(id = "u1", displayName = "Ada", email = "ada@example.com")

/** Records login calls; a successful [nextResult] signs in. */
internal class FakeAuthRepository : AuthRepository {
    private val state = MutableStateFlow<AuthState>(AuthState.SignedOut)
    override val authState: StateFlow<AuthState> = state
    val loginCalls = mutableListOf<Pair<String, String>>()
    var nextResult: AppResult<User> = AppResult.Success(TEST_USER)

    override suspend fun login(
        email: String,
        password: String,
    ): AppResult<User> {
        loginCalls += email to password
        (nextResult as? AppResult.Success)?.let { state.value = AuthState.SignedIn(it.value) }
        return nextResult
    }

    override suspend fun logout() {
        state.value = AuthState.SignedOut
    }
}

internal class FakeOnboardingRepository(
    completed: Boolean = false,
) : OnboardingRepository {
    private val state = MutableStateFlow(completed)
    override val isCompleted: StateFlow<Boolean> = state

    override suspend fun markCompleted() {
        state.value = true
    }

    override suspend fun reset() {
        state.value = false
    }
}

internal class FakeConsentRepository(
    granted: Boolean = false,
) : ConsentRepository {
    override val analyticsConsent = MutableStateFlow(granted)

    override suspend fun setAnalyticsConsent(granted: Boolean) {
        analyticsConsent.value = granted
    }
}
