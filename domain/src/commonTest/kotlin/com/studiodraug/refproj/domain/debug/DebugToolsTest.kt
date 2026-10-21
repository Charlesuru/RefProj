package com.studiodraug.refproj.domain.debug

import com.studiodraug.refproj.domain.FakeAuthRepository
import com.studiodraug.refproj.domain.FakeConsentRepository
import com.studiodraug.refproj.domain.FakeOnboardingRepository
import com.studiodraug.refproj.domain.TEST_USER
import com.studiodraug.refproj.domain.auth.AuthState
import com.studiodraug.refproj.domain.auth.Credentials
import com.studiodraug.refproj.domain.auth.GetTestAccountUseCase
import com.studiodraug.refproj.domain.auth.LoginResult
import com.studiodraug.refproj.domain.auth.LoginUseCase
import com.studiodraug.refproj.domain.auth.LogoutUseCase
import com.studiodraug.refproj.domain.auth.TestAccountProvider
import com.studiodraug.refproj.domain.model.AppResult
import com.studiodraug.refproj.domain.model.DataError
import com.studiodraug.refproj.domain.tasks.FakeTaskRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

// UNIT TEST
class DebugToolsTest {
    private val auth = FakeAuthRepository()
    private val onboarding = FakeOnboardingRepository()
    private val consent = FakeConsentRepository(granted = true)
    private val tasks = FakeTaskRepository()

    private fun signInWithTestAccount(account: Credentials? = ACCOUNT) =
        SignInWithTestAccountUseCase(TestAccountProvider { account }, LoginUseCase(auth), onboarding)

    @Test
    fun signInWithTestAccount_signsInWithItsCredentials_andSkipsOnboarding() =
        runTest {
            assertEquals(LoginResult.Success(TEST_USER), signInWithTestAccount()())

            assertEquals(listOf(ACCOUNT.email to ACCOUNT.password), auth.loginCalls)
            assertTrue(onboarding.isCompleted.value)
        }

    @Test
    fun signInWithTestAccount_whenLoginFails_leavesOnboardingAlone() =
        runTest {
            auth.nextResult = AppResult.Failure(DataError.Network)

            assertEquals(LoginResult.Failure(DataError.Network), signInWithTestAccount()())
            assertFalse(onboarding.isCompleted.value)
        }

    @Test
    fun signInWithTestAccount_withoutATestAccount_doesNothing() =
        runTest {
            assertNull(signInWithTestAccount(account = null)())
            assertTrue(auth.loginCalls.isEmpty())
        }

    @Test
    fun clearLocalData_returnsToAFreshInstall() =
        runTest {
            auth.login(TEST_USER.email, "secret")
            onboarding.markCompleted()
            tasks.add("Private")

            ClearLocalDataUseCase(onboarding, LogoutUseCase(auth, tasks), consent)()

            assertFalse(onboarding.isCompleted.value)
            assertEquals(AuthState.SignedOut, auth.authState.value)
            assertTrue(tasks.tasks.value.isEmpty())
            assertFalse(consent.analyticsConsent.value)
        }

    @Test
    fun testAccount_comesFromTheProvider_andNeverPrintsThePassword() {
        assertEquals(ACCOUNT, GetTestAccountUseCase { ACCOUNT }())
        assertFalse(ACCOUNT.toString().contains(ACCOUNT.password))
    }

    private companion object {
        val ACCOUNT = Credentials(email = "test@example.com", password = "Password1")
    }
}
