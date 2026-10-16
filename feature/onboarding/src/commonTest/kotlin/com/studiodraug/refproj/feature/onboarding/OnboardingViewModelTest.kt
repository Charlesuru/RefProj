package com.studiodraug.refproj.feature.onboarding

import com.studiodraug.refproj.core.observability.AnalyticsEvent
import com.studiodraug.refproj.core.observability.AnalyticsTracker
import com.studiodraug.refproj.core.testing.FakeAnalyticsTracker
import com.studiodraug.refproj.core.testing.runViewModelTest
import com.studiodraug.refproj.domain.consent.ConsentRepository
import com.studiodraug.refproj.domain.consent.SetAnalyticsConsentUseCase
import com.studiodraug.refproj.domain.onboarding.CompleteOnboardingUseCase
import com.studiodraug.refproj.domain.onboarding.OnboardingRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow

// UNIT TEST
class OnboardingViewModelTest {
    private val onboarding = FakeOnboardingRepository()
    private val consent = FakeConsentRepository()
    private val analytics = FakeAnalyticsTracker()

    /** The consent at the moment each event was logged. */
    private val consentAtEvent = mutableListOf<Boolean>()
    private val recordingTracker =
        object : AnalyticsTracker by analytics {
            override fun logEvent(event: AnalyticsEvent) {
                consentAtEvent += consent.analyticsConsent.value
                analytics.logEvent(event)
            }
        }

    private fun viewModel() =
        OnboardingViewModel(CompleteOnboardingUseCase(onboarding), SetAnalyticsConsentUseCase(consent), recordingTracker)

    @Test
    fun consentIsOffByDefault_andFinishingStoresThatChoice() =
        runViewModelTest {
            val viewModel = viewModel()
            assertFalse(viewModel.state.value.analyticsConsent)

            viewModel.onAction(OnboardingAction.Finish)

            assertEquals(listOf(false), consent.choices)
            assertTrue(onboarding.isCompleted.value)
        }

    @Test
    fun anOptIn_isStoredBeforeTheCompletionEventIsLogged() =
        runViewModelTest {
            val viewModel = viewModel()
            viewModel.onAction(OnboardingAction.AnalyticsConsentChanged(granted = true))
            viewModel.onAction(OnboardingAction.Finish)

            assertEquals(listOf(true), consent.choices)
            assertEquals(listOf(AnalyticsEvent("tutorial_complete")), analytics.events)
            assertEquals(listOf(true), consentAtEvent)
        }

    @Test
    fun finishingTwice_completesOnce() =
        runViewModelTest {
            val viewModel = viewModel()
            viewModel.onAction(OnboardingAction.Finish)
            viewModel.onAction(OnboardingAction.Finish)
            assertEquals(1, consent.choices.size)
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

    private class FakeConsentRepository : ConsentRepository {
        override val analyticsConsent = MutableStateFlow(false)
        val choices = mutableListOf<Boolean>()

        override suspend fun setAnalyticsConsent(granted: Boolean) {
            choices += granted
            analyticsConsent.value = granted
        }
    }
}
