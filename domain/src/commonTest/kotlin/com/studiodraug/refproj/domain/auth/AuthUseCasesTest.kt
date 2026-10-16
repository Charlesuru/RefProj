package com.studiodraug.refproj.domain.auth

import app.cash.turbine.test
import com.studiodraug.refproj.domain.model.AppResult
import com.studiodraug.refproj.domain.model.DataError
import com.studiodraug.refproj.domain.tasks.FakeTaskRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest

// UNIT TEST
class AuthUseCasesTest {
    private val repository = FakeAuthRepository()
    private val tasks = FakeTaskRepository()
    private val login = LoginUseCase(repository)

    @Test
    fun login_withBlankFields_reportsBothErrors_withoutCallingTheBackend() =
        runTest {
            assertEquals(LoginResult.InvalidInput(EmailError.Blank, PasswordError.Blank), login("  ", ""))
            assertTrue(repository.loginCalls.isEmpty())
        }

    @Test
    fun login_withMalformedEmail_reportsInvalidEmail() =
        runTest {
            listOf("user", "user@", "user@host", "a b@host.se", "@host.se").forEach { email ->
                assertEquals(LoginResult.InvalidInput(EmailError.Invalid, null), login(email, "secret"), email)
            }
        }

    @Test
    fun login_trimsTheEmail_butNotThePassword() =
        runTest {
            login("  user@example.com ", " pw ")
            assertEquals(listOf("user@example.com" to " pw "), repository.loginCalls)
        }

    @Test
    fun login_passesBackendResultsThrough() =
        runTest {
            assertEquals(LoginResult.Success(USER), login(USER.email, "secret"))
            repository.nextResult = AppResult.Failure(DataError.Unauthorized)
            assertEquals(LoginResult.Failure(DataError.Unauthorized), login(USER.email, "wrong"))
        }

    @Test
    fun currentUser_followsTheAuthState() =
        runTest {
            ObserveCurrentUserUseCase(repository)().test {
                assertNull(awaitItem())
                login(USER.email, "secret")
                assertEquals(USER, awaitItem())
                LogoutUseCase(repository, tasks)()
                assertNull(awaitItem())
            }
        }

    @Test
    fun logout_deletesTheUsersTasks() =
        runTest {
            login(USER.email, "secret")
            tasks.add("Private")
            LogoutUseCase(repository, tasks)()
            assertTrue(tasks.tasks.value.isEmpty())
            assertEquals(AuthState.SignedOut, repository.authState.value)
        }

    private class FakeAuthRepository : AuthRepository {
        private val state = MutableStateFlow<AuthState>(AuthState.SignedOut)
        override val authState: StateFlow<AuthState> = state
        val loginCalls = mutableListOf<Pair<String, String>>()
        var nextResult: AppResult<User> = AppResult.Success(USER)

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

    private companion object {
        val USER = User(id = "u1", displayName = "Ada", email = "ada@example.com")
    }
}
