package com.studiodraug.refproj.feature.profile

import androidx.lifecycle.viewModelScope
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.ui.MviViewModel
import com.studiodraug.refproj.domain.auth.LogoutUseCase
import com.studiodraug.refproj.domain.auth.ObserveCurrentUserUseCase
import com.studiodraug.refproj.domain.consent.ObserveAnalyticsConsentUseCase
import com.studiodraug.refproj.domain.consent.SetAnalyticsConsentUseCase
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val analyticsConsent: Boolean = false,
    val version: String = "",
    val showLogoutConfirmation: Boolean = false,
    val isLoggingOut: Boolean = false,
)

sealed interface ProfileAction {
    data class AnalyticsConsentChanged(
        val granted: Boolean,
    ) : ProfileAction

    data object LogoutClicked : ProfileAction

    data object LogoutConfirmed : ProfileAction

    data object LogoutDismissed : ProfileAction
}

/** After logout the app shows the login screen by itself (`AppViewModel`), so there is no navigation event. */
class ProfileViewModel(
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeAnalyticsConsent: ObserveAnalyticsConsentUseCase,
    private val setAnalyticsConsent: SetAnalyticsConsentUseCase,
    private val logout: LogoutUseCase,
    config: AppConfig,
) : MviViewModel<ProfileUiState, ProfileAction, Nothing>(
        ProfileUiState(version = "${config.versionName} (${config.buildNumber})"),
    ) {
    init {
        viewModelScope.launch {
            // Keeps the last user while logging out, so the screen does not flash empty.
            observeCurrentUser().filterNotNull().collect { user ->
                updateState { copy(displayName = user.displayName, email = user.email) }
            }
        }
        viewModelScope.launch {
            observeAnalyticsConsent().collect { granted -> updateState { copy(analyticsConsent = granted) } }
        }
    }

    override fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.AnalyticsConsentChanged -> viewModelScope.launch { setAnalyticsConsent(action.granted) }
            ProfileAction.LogoutClicked -> updateState { copy(showLogoutConfirmation = true) }
            ProfileAction.LogoutDismissed -> updateState { copy(showLogoutConfirmation = false) }
            ProfileAction.LogoutConfirmed -> logOut()
        }
    }

    private fun logOut() {
        if (currentState.isLoggingOut) return
        updateState { copy(showLogoutConfirmation = false, isLoggingOut = true) }
        viewModelScope.launch { logout() }
    }
}
