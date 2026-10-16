package com.studiodraug.refproj.app

import com.studiodraug.refproj.core.navigation.LoginRoute
import com.studiodraug.refproj.core.navigation.MainRoute
import com.studiodraug.refproj.core.navigation.OnboardingRoute
import com.studiodraug.refproj.core.testing.runViewModelTest
import com.studiodraug.refproj.domain.auth.AuthRepository
import com.studiodraug.refproj.domain.auth.AuthState
import com.studiodraug.refproj.domain.auth.ObserveAuthStateUseCase
import com.studiodraug.refproj.domain.auth.User
import com.studiodraug.refproj.domain.model.AppResult
import com.studiodraug.refproj.domain.onboarding.ObserveOnboardingCompletedUseCase
import com.studiodraug.refproj.domain.onboarding.OnboardingRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow

// UNIT TEST
class AppViewModelTest {
    private val onboarding = FakeOnboardingRepository()
    private val auth = FakeAuthRepository()

    private fun viewModel() = AppViewModel(ObserveOnboardingCompletedUseCase(onboarding), ObserveAuthStateUseCase(auth))

    @Test
    fun whileTheSessionIsUnknown_itIsLoading() =
        runViewModelTest {
            onboarding.isCompleted.value = true
            assertEquals(AppUiState.Loading, viewModel().state.value)
        }

    @Test
    fun onboardingComesFirst_evenBeforeTheSessionIsKnown() =
        runViewModelTest {
            assertEquals(AppUiState.Ready(OnboardingRoute), viewModel().state.value)
        }

    @Test
    fun theDestinationFollowsOnboardingAndTheSession() =
        runViewModelTest {
            val viewModel = viewModel()
            auth.authState.value = AuthState.SignedOut
            assertEquals(AppUiState.Ready(OnboardingRoute), viewModel.state.value)

            onboarding.isCompleted.value = true
            assertEquals(AppUiState.Ready(LoginRoute), viewModel.state.value)

            auth.authState.value = AuthState.SignedIn(User("u1", "Ada", "ada@example.com"))
            assertEquals(AppUiState.Ready(MainRoute), viewModel.state.value)

            auth.authState.value = AuthState.SignedOut
            assertEquals(AppUiState.Ready(LoginRoute), viewModel.state.value)
        }

    private class FakeOnboardingRepository : OnboardingRepository {
        override val isCompleted = MutableStateFlow(false)

        override suspend fun markCompleted() {
            isCompleted.value = true
        }

        override suspend fun reset() {
            isCompleted.value = false
        }
    }

    private class FakeAuthRepository : AuthRepository {
        override val authState = MutableStateFlow<AuthState>(AuthState.Unknown)

        override suspend fun login(
            email: String,
            password: String,
        ): AppResult<User> = error("Not used")

        override suspend fun logout() = Unit
    }
}
