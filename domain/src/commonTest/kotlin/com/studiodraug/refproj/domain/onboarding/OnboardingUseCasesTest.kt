package com.studiodraug.refproj.domain.onboarding

import app.cash.turbine.test
import com.studiodraug.refproj.domain.FakeOnboardingRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
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

    @Test
    fun resetting_onboarding_showsItAgain() =
        runTest {
            CompleteOnboardingUseCase(repository)()
            ResetOnboardingUseCase(repository)()
            ObserveOnboardingCompletedUseCase(repository)().test {
                assertFalse(awaitItem())
            }
        }
}
