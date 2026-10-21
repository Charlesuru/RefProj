package com.studiodraug.refproj.feature.login

import com.studiodraug.refproj.core.observability.AnalyticsEvent
import com.studiodraug.refproj.core.testing.FakeAnalyticsTracker
import com.studiodraug.refproj.core.testing.runViewModelTest
import com.studiodraug.refproj.domain.auth.AuthRepository
import com.studiodraug.refproj.domain.auth.AuthState
import com.studiodraug.refproj.domain.auth.Credentials
import com.studiodraug.refproj.domain.auth.EmailError
import com.studiodraug.refproj.domain.auth.GetTestAccountUseCase
import com.studiodraug.refproj.domain.auth.LoginUseCase
import com.studiodraug.refproj.domain.auth.PasswordError
import com.studiodraug.refproj.domain.auth.User
import com.studiodraug.refproj.domain.model.AppResult
import com.studiodraug.refproj.domain.model.DataError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

// UNIT TEST
class LoginViewModelTest {
    private val repository = FakeAuthRepository()
    private val analytics = FakeAnalyticsTracker()
    private var testAccount: Credentials? = null
    private val viewModel by lazy { LoginViewModel(LoginUseCase(repository), analytics, GetTestAccountUseCase { testAccount }) }

    private fun enter(
        email: String,
        password: String,
    ) {
        viewModel.onAction(LoginAction.EmailChanged(email))
        viewModel.onAction(LoginAction.PasswordChanged(password))
    }

    @Test
    fun withoutATestAccount_theFormStartsEmpty() =
        runViewModelTest {
            assertEquals(LoginUiState(), viewModel.state.value)
        }

    @Test
    fun withATestAccount_theFormStartsFilledIn() =
        runViewModelTest {
            testAccount = Credentials("test@example.com", "Password1")
            assertEquals("test@example.com", viewModel.state.value.email)
            assertEquals("Password1", viewModel.state.value.password)
        }

    @Test
    fun emptyFields_showFieldErrors_withoutCallingTheBackend() =
        runViewModelTest {
            viewModel.onAction(LoginAction.Submit)

            val state = viewModel.state.value
            assertEquals(EmailError.Blank, state.emailError)
            assertEquals(PasswordError.Blank, state.passwordError)
            assertFalse(state.isLoading)
            assertEquals(0, repository.calls)
        }

    @Test
    fun typing_clearsTheFieldError() =
        runViewModelTest {
            viewModel.onAction(LoginAction.Submit)
            viewModel.onAction(LoginAction.EmailChanged("a"))
            assertNull(viewModel.state.value.emailError)
            assertEquals(PasswordError.Blank, viewModel.state.value.passwordError)
        }

    @Test
    fun whileLoggingIn_itIsLoading_andASecondSubmitIsIgnored() =
        runViewModelTest {
            repository.gate = CompletableDeferred()
            enter("ada@example.com", "secret")
            viewModel.onAction(LoginAction.Submit)
            viewModel.onAction(LoginAction.Submit)

            assertTrue(viewModel.state.value.isLoading)
            assertEquals(1, repository.calls)
            repository.gate?.complete(Unit)
        }

    @Test
    fun success_logsTheLoginEvent_andKeepsLoadingUntilTheShellAppears() =
        runViewModelTest {
            enter("ada@example.com", "secret")
            viewModel.onAction(LoginAction.Submit)

            assertTrue(viewModel.state.value.isLoading)
            assertEquals(listOf(AnalyticsEvent("login", mapOf("method" to "password"))), analytics.events)
        }

    @Test
    fun failures_areMappedToMessages() =
        runViewModelTest {
            mapOf(
                DataError.Unauthorized to LoginError.WrongCredentials,
                DataError.Network to LoginError.Network,
                DataError.Server to LoginError.Unknown,
            ).forEach { (dataError, expected) ->
                repository.result = AppResult.Failure(dataError)
                enter("ada@example.com", "secret")
                viewModel.onAction(LoginAction.Submit)

                assertEquals(expected, viewModel.state.value.error)
                assertFalse(viewModel.state.value.isLoading)
            }
            assertTrue(analytics.events.isEmpty())
        }

    private class FakeAuthRepository : AuthRepository {
        override val authState = MutableStateFlow<AuthState>(AuthState.SignedOut)
        var result: AppResult<User> = AppResult.Success(User("u1", "Ada", "ada@example.com"))
        var gate: CompletableDeferred<Unit>? = null
        var calls = 0

        override suspend fun login(
            email: String,
            password: String,
        ): AppResult<User> {
            calls++
            gate?.await()
            return result
        }

        override suspend fun logout() = Unit
    }
}
