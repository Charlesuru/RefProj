package com.studiodraug.refproj.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.studiodraug.refproj.core.navigation.LoginRoute
import com.studiodraug.refproj.core.navigation.MainRoute
import com.studiodraug.refproj.core.navigation.OnboardingRoute
import com.studiodraug.refproj.domain.auth.AuthState
import com.studiodraug.refproj.domain.auth.ObserveAuthStateUseCase
import com.studiodraug.refproj.domain.onboarding.ObserveOnboardingCompletedUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn

sealed interface AppUiState {
    /** The stored session is still being read (a few milliseconds); nothing is shown yet. */
    data object Loading : AppUiState

    /** [destination] is [OnboardingRoute], [LoginRoute] or [MainRoute]. */
    data class Ready(
        val destination: NavKey,
    ) : AppUiState
}

/**
 * Decides the top-level destination from the app's state, so the UI never has to navigate after
 * finishing onboarding, logging in or out — or when a session is restored or expires.
 */
class AppViewModel(
    observeOnboardingCompleted: ObserveOnboardingCompletedUseCase,
    observeAuthState: ObserveAuthStateUseCase,
) : ViewModel() {
    val state: StateFlow<AppUiState> =
        combine(observeOnboardingCompleted(), observeAuthState()) { onboarded, auth ->
            when {
                !onboarded -> AppUiState.Ready(OnboardingRoute)
                auth is AuthState.SignedIn -> AppUiState.Ready(MainRoute)
                auth == AuthState.SignedOut -> AppUiState.Ready(LoginRoute)
                else -> AppUiState.Loading
            }
        }.distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Eagerly, AppUiState.Loading)
}
