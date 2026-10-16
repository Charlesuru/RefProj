package com.studiodraug.refproj.feature.onboarding

import androidx.lifecycle.viewModelScope
import com.studiodraug.refproj.core.observability.AnalyticsEvent
import com.studiodraug.refproj.core.observability.AnalyticsTracker
import com.studiodraug.refproj.core.ui.MviViewModel
import com.studiodraug.refproj.domain.consent.SetAnalyticsConsentUseCase
import com.studiodraug.refproj.domain.onboarding.CompleteOnboardingUseCase
import kotlinx.coroutines.launch

/** The current pager page is UI state and lives in the screen; this holds what outlives the pager. */
data class OnboardingUiState(
    /** Opt-in: off until the user turns it on (GDPR). */
    val analyticsConsent: Boolean = false,
    val isFinishing: Boolean = false,
)

sealed interface OnboardingAction {
    data class AnalyticsConsentChanged(
        val granted: Boolean,
    ) : OnboardingAction

    data object Finish : OnboardingAction
}

/**
 * Stores the consent choice and marks onboarding as done. No navigation event: the app shows the
 * next destination as soon as onboarding is completed (`AppViewModel`).
 */
class OnboardingViewModel(
    private val completeOnboarding: CompleteOnboardingUseCase,
    private val setAnalyticsConsent: SetAnalyticsConsentUseCase,
    private val analytics: AnalyticsTracker,
) : MviViewModel<OnboardingUiState, OnboardingAction, Nothing>(OnboardingUiState()) {
    override fun onAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.AnalyticsConsentChanged -> updateState { copy(analyticsConsent = action.granted) }
            OnboardingAction.Finish -> finish()
        }
    }

    private fun finish() {
        if (currentState.isFinishing) return
        updateState { copy(isFinishing = true) }
        viewModelScope.launch {
            // Consent first, so the completion event is only collected when the user agreed.
            setAnalyticsConsent(currentState.analyticsConsent)
            analytics.logEvent(AnalyticsEvent(EVENT_COMPLETED))
            completeOnboarding()
        }
    }

    private companion object {
        /** Firebase's recommended event name for a finished onboarding. */
        const val EVENT_COMPLETED = "tutorial_complete"
    }
}
