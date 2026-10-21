package com.studiodraug.refproj.data.auth

import co.touchlab.kermit.Logger
import com.studiodraug.refproj.core.testing.FakeAnalyticsTracker
import com.studiodraug.refproj.core.testing.FakeCrashReporter
import com.studiodraug.refproj.core.testing.FakeSecureStore
import com.studiodraug.refproj.domain.auth.AuthState
import com.studiodraug.refproj.domain.model.AppResult
import com.studiodraug.refproj.domain.model.DataError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

// INTEGRATION TEST

/** Repository + the fake backend + an in-memory secure store: login, restore after restart, logout. */
class AuthRepositoryImplTest {
    private val secureStore = FakeSecureStore()
    private val crashReporter = FakeCrashReporter()
    private val analytics = FakeAnalyticsTracker()

    private fun repository() =
        AuthRepositoryImpl(FakeAuthApi(latency = Duration.ZERO), secureStore, crashReporter, analytics, Logger)

    /** The state once the stored session has been read. */
    private suspend fun AuthRepositoryImpl.settledState() = authState.first { it != AuthState.Unknown }

    @Test
    fun startsSignedOut_whenNothingIsStored() =
        runTest {
            assertEquals(AuthState.SignedOut, repository().settledState())
        }

    @Test
    fun login_storesTheSession_andARestartRestoresIt() =
        runTest {
            val result = repository().login(FakeAuthApi.TEST_EMAIL, FakeAuthApi.TEST_PASSWORD)
            val user = assertIs<AppResult.Success<*>>(result).value

            assertEquals(AuthState.SignedIn(assertIs(user)), repository().settledState())
            assertEquals("test-user-1", crashReporter.userId)
            assertEquals("test-user-1", analytics.userId)
        }

    @Test
    fun testAccount_isAcceptedByTheFakeBackend() =
        runTest {
            val account = FakeAuthTestAccount.testAccount()
            assertIs<AppResult.Success<*>>(repository().login(account.email, account.password))
        }

    @Test
    fun login_withWrongPassword_isUnauthorized_andStoresNothing() =
        runTest {
            val result = repository().login(FakeAuthApi.TEST_EMAIL, "wrong")
            assertEquals(AppResult.Failure(DataError.Unauthorized), result)
            assertTrue(secureStore.values.isEmpty())
        }

    @Test
    fun login_offline_isANetworkError() =
        runTest {
            assertEquals(AppResult.Failure(DataError.Network), repository().login(FakeAuthApi.OFFLINE_EMAIL, "x"))
        }

    @Test
    fun logout_clearsTheSessionAndTheUserIds() =
        runTest {
            val repository = repository()
            repository.login(FakeAuthApi.TEST_EMAIL, FakeAuthApi.TEST_PASSWORD)
            repository.logout()

            assertTrue(secureStore.values.isEmpty())
            assertNull(crashReporter.userId)
            assertNull(analytics.userId)
            assertEquals(AuthState.SignedOut, repository().settledState())
        }

    @Test
    fun aPartialSession_isDiscarded() =
        runTest {
            secureStore.put("auth.token", "token-without-profile")
            assertEquals(AuthState.SignedOut, repository().settledState())
            assertTrue(secureStore.values.isEmpty())
        }
}
