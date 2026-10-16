package com.studiodraug.refproj.data.auth

import com.studiodraug.refproj.core.network.ApiError
import com.studiodraug.refproj.core.network.ApiResult
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

/**
 * Stand-in for the authentication backend until a real one exists (`docs/features.md`). Knows one
 * test account and simulates network latency, so loading states are visible.
 *
 * The test credentials are public demo values for this fake, not a secret: no real backend accepts them.
 */
internal class FakeAuthApi(
    private val latency: Duration = DEFAULT_LATENCY,
) : AuthApi {
    override suspend fun login(
        email: String,
        password: String,
    ): ApiResult<LoginResponse> {
        delay(latency)
        return when {
            email.equals(OFFLINE_EMAIL, ignoreCase = true) -> {
                ApiResult.Failure(ApiError.Network)
            }

            email.equals(TEST_EMAIL, ignoreCase = true) && password == TEST_PASSWORD -> {
                ApiResult.Success(
                    LoginResponse(
                        token = randomToken(),
                        userId = TEST_USER_ID,
                        displayName = TEST_DISPLAY_NAME,
                        email = TEST_EMAIL,
                    ),
                )
            }

            else -> {
                ApiResult.Failure(ApiError.Http(HTTP_UNAUTHORIZED))
            }
        }
    }

    // Not cryptographically secure, which is fine for a fake: a real backend issues real tokens.
    private fun randomToken(): String = Random.nextBytes(TOKEN_BYTES).joinToString("") { it.toUByte().toString(HEX).padStart(2, '0') }

    companion object {
        const val TEST_EMAIL = "test@example.com"
        const val TEST_PASSWORD = "Password1"

        /** Logging in with this address simulates a network failure. */
        const val OFFLINE_EMAIL = "offline@example.com"

        private const val TEST_USER_ID = "test-user-1"
        private const val TEST_DISPLAY_NAME = "Test User"
        private const val HTTP_UNAUTHORIZED = 401
        private const val TOKEN_BYTES = 32
        private const val HEX = 16
        private val DEFAULT_LATENCY = 800.milliseconds
    }
}
