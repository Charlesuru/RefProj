package com.studiodraug.refproj.domain.onboarding

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest

// UNIT TEST
class OnboardingUseCasesTest {
    private val repository = FakeOnboardingRepository()

    @Test
    fun completing_onboarding_isObserved() =
        runTest {
            ObserveOnboardingCompletedUseCase(repository)().test {
                assertFalse(awaitItem())
                CompleteOnboardingUseCase(repository)()
                assertTrue(awaitItem())
            }
        }

    private class FakeOnboardingRepository : OnboardingRepository {
        private val state = MutableStateFlow(false)
        override val isCompleted: StateFlow<Boolean> = state

        override suspend fun markCompleted() {
            state.value = true
        }

        override suspend fun reset() {
            state.value = false
        }
    }
}
