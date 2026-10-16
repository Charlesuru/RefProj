package com.studiodraug.refproj.feature.login

import androidx.lifecycle.viewModelScope
import com.studiodraug.refproj.core.observability.AnalyticsEvent
import com.studiodraug.refproj.core.observability.AnalyticsTracker
import com.studiodraug.refproj.core.ui.MviViewModel
import com.studiodraug.refproj.domain.auth.EmailError
import com.studiodraug.refproj.domain.auth.LoginResult
import com.studiodraug.refproj.domain.auth.LoginUseCase
import com.studiodraug.refproj.domain.auth.PasswordError
import com.studiodraug.refproj.domain.model.DataError
import kotlinx.coroutines.launch

/** Why a login attempt failed, as shown to the user. */
enum class LoginError {
    WrongCredentials,
    Network,
    Unknown,
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
    val error: LoginError? = null,
    val isLoading: Boolean = false,
)

sealed interface LoginAction {
    data class EmailChanged(
        val value: String,
    ) : LoginAction

    data class PasswordChanged(
        val value: String,
    ) : LoginAction

    data object Submit : LoginAction
}

/**
 * No navigation event on success: the app switches to the logged-in shell when the session
 * changes (`AppViewModel`), which also covers a session restored at startup.
 */
class LoginViewModel(
    private val login: LoginUseCase,
    private val analytics: AnalyticsTracker,
) : MviViewModel<LoginUiState, LoginAction, Nothing>(LoginUiState()) {
    override fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.EmailChanged -> updateState { copy(email = action.value, emailError = null, error = null) }
            is LoginAction.PasswordChanged -> updateState { copy(password = action.value, passwordError = null, error = null) }
            LoginAction.Submit -> submit()
        }
    }

    private fun submit() {
        if (currentState.isLoading) return
        updateState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = login(currentState.email, currentState.password)) {
                is LoginResult.Success -> {
                    analytics.logEvent(AnalyticsEvent(name = "login", parameters = mapOf("method" to "password")))
                    // Stays loading until the app shows the logged-in shell.
                }

                is LoginResult.InvalidInput -> {
                    updateState { copy(isLoading = false, emailError = result.emailError, passwordError = result.passwordError) }
                }

                is LoginResult.Failure -> {
                    updateState { copy(isLoading = false, error = result.error.toLoginError()) }
                }
            }
        }
    }
}

private fun DataError.toLoginError(): LoginError =
    when (this) {
        DataError.Unauthorized -> LoginError.WrongCredentials
        DataError.Network -> LoginError.Network
        DataError.NotFound, DataError.Server -> LoginError.Unknown
    }
