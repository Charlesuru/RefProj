package com.studiodraug.refproj.domain.auth

import com.studiodraug.refproj.domain.model.AppResult
import com.studiodraug.refproj.domain.model.DataError
import com.studiodraug.refproj.domain.tasks.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The signed-in user.
 *
 * PRIVACY: [displayName] and [email] are personal data — never log them or send them to analytics.
 * [id] is pseudonymous and may be used as the analytics / crash-reporting user ID.
 */
data class User(
    val id: String,
    val displayName: String,
    val email: String,
)

/** Whether someone is signed in. Starts as [Unknown] until the stored session has been read. */
sealed interface AuthState {
    data object Unknown : AuthState

    data object SignedOut : AuthState

    data class SignedIn(
        val user: User,
    ) : AuthState
}

/** Signs users in and out and keeps the session across app restarts. Implemented in the data layer. */
interface AuthRepository {
    val authState: Flow<AuthState>

    /** [DataError.Unauthorized] means the credentials were rejected. */
    suspend fun login(
        email: String,
        password: String,
    ): AppResult<User>

    suspend fun logout()
}

class ObserveAuthStateUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<AuthState> = repository.authState
}

/** The signed-in user, or `null` while signed out or not yet known. */
class ObserveCurrentUserUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<User?> = repository.authState.map { (it as? AuthState.SignedIn)?.user }
}

enum class EmailError {
    Blank,
    Invalid,
}

enum class PasswordError {
    Blank,
}

/** Outcome of [LoginUseCase]. Input errors are reported per field and never reach the backend. */
sealed interface LoginResult {
    data class Success(
        val user: User,
    ) : LoginResult

    data class InvalidInput(
        val emailError: EmailError?,
        val passwordError: PasswordError?,
    ) : LoginResult

    data class Failure(
        val error: DataError,
    ) : LoginResult
}

/** Validates the credentials locally, then signs in. The e-mail address is trimmed; the password is not. */
class LoginUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): LoginResult {
        val trimmedEmail = email.trim()
        val emailError =
            when {
                trimmedEmail.isEmpty() -> EmailError.Blank
                !EMAIL_PATTERN.matches(trimmedEmail) -> EmailError.Invalid
                else -> null
            }
        val passwordError = if (password.isEmpty()) PasswordError.Blank else null
        if (emailError != null || passwordError != null) return LoginResult.InvalidInput(emailError, passwordError)

        return when (val result = repository.login(trimmedEmail, password)) {
            is AppResult.Success -> LoginResult.Success(result.value)
            is AppResult.Failure -> LoginResult.Failure(result.error)
        }
    }

    private companion object {
        // Deliberately loose: the backend is the authority; this only catches obvious typos.
        val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}

/** An e-mail address and password. [toString] hides the password, so it never ends up in a log. */
data class Credentials(
    val email: String,
    val password: String,
) {
    override fun toString(): String = "Credentials(email=$email, password=***)"
}

/**
 * The test account of the current environment, for debug tools (pre-filled login, "sign in as test user").
 * Implemented in the data layer, next to the backend that knows the account.
 *
 * SECURITY: returns `null` whenever the debug menu is disabled, i.e. always in a PROD release build.
 */
fun interface TestAccountProvider {
    fun testAccount(): Credentials?
}

/** The environment's test account, or `null` when there is none or debug tools are disabled. */
class GetTestAccountUseCase(
    private val provider: TestAccountProvider,
) {
    operator fun invoke(): Credentials? = provider.testAccount()
}

/**
 * Signs out and deletes the user's data from the device first, so the next person on a shared
 * device never sees it.
 */
class LogoutUseCase(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke() {
        taskRepository.deleteAll()
        authRepository.logout()
    }
}
