package com.studiodraug.refproj.data.auth

import co.touchlab.kermit.Logger
import com.studiodraug.refproj.core.network.ApiResult
import com.studiodraug.refproj.core.observability.AnalyticsTracker
import com.studiodraug.refproj.core.observability.CrashReporter
import com.studiodraug.refproj.core.storage.SecureStore
import com.studiodraug.refproj.data.remote.toDataError
import com.studiodraug.refproj.domain.auth.AuthRepository
import com.studiodraug.refproj.domain.auth.AuthState
import com.studiodraug.refproj.domain.auth.User
import com.studiodraug.refproj.domain.model.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Keeps the session in [SecureStore] (token and profile are secrets / personal data), so it
 * survives restarts. The stored session is read on the first collection of [authState].
 * Sets the pseudonymous user ID on crash reports and analytics while signed in.
 */
internal class AuthRepositoryImpl(
    private val api: AuthApi,
    private val secureStore: SecureStore,
    private val crashReporter: CrashReporter,
    private val analytics: AnalyticsTracker,
    logger: Logger,
) : AuthRepository {
    private val log = logger.withTag("Auth")
    private val state = MutableStateFlow<AuthState>(AuthState.Unknown)

    /** Serialises every change of [state] and the store, so a restore can never undo a login. */
    private val lock = Mutex()

    override val authState: Flow<AuthState> = state.onStart { restoreSession() }

    override suspend fun login(
        email: String,
        password: String,
    ): AppResult<User> =
        when (val result = api.login(email, password)) {
            is ApiResult.Failure -> {
                log.i { "Login failed: ${result.error}" }
                AppResult.Failure(result.error.toDataError())
            }

            is ApiResult.Success -> {
                val response = result.value
                val user = User(id = response.userId, displayName = response.displayName, email = response.email)
                lock.withLock {
                    secureStore.put(KEY_TOKEN, response.token)
                    secureStore.put(KEY_USER_ID, user.id)
                    secureStore.put(KEY_DISPLAY_NAME, user.displayName)
                    secureStore.put(KEY_EMAIL, user.email)
                    signedIn(user)
                }
                log.i { "Signed in" }
                AppResult.Success(user)
            }
        }

    override suspend fun logout() {
        lock.withLock {
            SESSION_KEYS.forEach { secureStore.remove(it) }
            crashReporter.setUserId(null)
            analytics.setUserId(null)
            state.value = AuthState.SignedOut
        }
        log.i { "Signed out" }
    }

    private suspend fun restoreSession() =
        lock.withLock {
            if (state.value != AuthState.Unknown) return@withLock
            val user = readStoredUser()
            if (user != null && secureStore.get(KEY_TOKEN) != null) {
                signedIn(user)
                log.i { "Session restored" }
            } else {
                // A partial session (e.g. one key could not be decrypted) is unusable: start clean.
                SESSION_KEYS.forEach { secureStore.remove(it) }
                state.value = AuthState.SignedOut
            }
        }

    private suspend fun readStoredUser(): User? {
        val id = secureStore.get(KEY_USER_ID)
        val displayName = secureStore.get(KEY_DISPLAY_NAME)
        val email = secureStore.get(KEY_EMAIL)
        return if (id != null && displayName != null && email != null) User(id, displayName, email) else null
    }

    private fun signedIn(user: User) {
        crashReporter.setUserId(user.id)
        analytics.setUserId(user.id)
        state.value = AuthState.SignedIn(user)
    }

    private companion object {
        const val KEY_TOKEN = "auth.token"
        const val KEY_USER_ID = "auth.user_id"
        const val KEY_DISPLAY_NAME = "auth.display_name"
        const val KEY_EMAIL = "auth.email"
        val SESSION_KEYS = listOf(KEY_TOKEN, KEY_USER_ID, KEY_DISPLAY_NAME, KEY_EMAIL)
    }
}
