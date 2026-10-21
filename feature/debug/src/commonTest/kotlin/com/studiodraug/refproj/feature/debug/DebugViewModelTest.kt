package com.studiodraug.refproj.feature.debug

import co.touchlab.kermit.Logger
import com.studiodraug.refproj.core.featureflags.FeatureFlag
import com.studiodraug.refproj.core.featureflags.FeatureFlagOverrides
import com.studiodraug.refproj.core.featureflags.FeatureFlagState
import com.studiodraug.refproj.core.featureflags.FeatureFlags
import com.studiodraug.refproj.core.testing.FakeCrashReporter
import com.studiodraug.refproj.core.testing.runViewModelTest
import com.studiodraug.refproj.core.testing.testAppConfig
import com.studiodraug.refproj.domain.auth.AuthRepository
import com.studiodraug.refproj.domain.auth.AuthState
import com.studiodraug.refproj.domain.auth.Credentials
import com.studiodraug.refproj.domain.auth.GetTestAccountUseCase
import com.studiodraug.refproj.domain.auth.LoginUseCase
import com.studiodraug.refproj.domain.auth.LogoutUseCase
import com.studiodraug.refproj.domain.auth.ObserveAuthStateUseCase
import com.studiodraug.refproj.domain.auth.TestAccountProvider
import com.studiodraug.refproj.domain.auth.User
import com.studiodraug.refproj.domain.consent.ConsentRepository
import com.studiodraug.refproj.domain.debug.ClearLocalDataUseCase
import com.studiodraug.refproj.domain.debug.SignInWithTestAccountUseCase
import com.studiodraug.refproj.domain.model.AppResult
import com.studiodraug.refproj.domain.model.DataError
import com.studiodraug.refproj.domain.onboarding.OnboardingRepository
import com.studiodraug.refproj.domain.onboarding.ResetOnboardingUseCase
import com.studiodraug.refproj.domain.tasks.Task
import com.studiodraug.refproj.domain.tasks.TaskRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// UNIT TEST
class DebugViewModelTest {
    private val flags = FakeFeatureFlags()
    private val auth = FakeAuthRepository()
    private val onboarding = FakeOnboardingRepository()
    private val consent = FakeConsentRepository()
    private val tasks = FakeTaskRepository()
    private val crashReporter = FakeCrashReporter()
    private var testAccount: Credentials? = Credentials("test@example.com", "Password1")

    private val viewModel by lazy {
        val accounts = TestAccountProvider { testAccount }
        val logout = LogoutUseCase(auth, tasks)
        DebugViewModel(
            environment = EnvironmentInfo.from(testAppConfig(), device = "Test device"),
            featureFlags = flags,
            flagOverrides = flags,
            shortcuts =
                AppStateShortcuts(
                    observeAuthState = ObserveAuthStateUseCase(auth),
                    getTestAccount = GetTestAccountUseCase(accounts),
                    signInWithTestAccount = SignInWithTestAccountUseCase(accounts, LoginUseCase(auth), onboarding),
                    resetOnboarding = ResetOnboardingUseCase(onboarding),
                    logout = logout,
                    clearLocalData = ClearLocalDataUseCase(onboarding, logout, consent),
                ),
            crashReporter = crashReporter,
            logger = Logger,
        )
    }

    @Test
    fun showsTheEnvironment_theFlags_andTheTestAccount() =
        runViewModelTest {
            val state = viewModel.state.value
            assertEquals("DEV", state.environment.environment)
            assertEquals("1.0.0-test (1)", state.environment.version)
            assertEquals(listOf(FeatureFlag.HOME_BUILD_INFO), state.flags.map { it.flag })
            assertEquals("test@example.com", state.testAccountEmail)
        }

    @Test
    fun withoutATestAccount_thereIsNoEmailToShow() =
        runViewModelTest {
            testAccount = null
            assertNull(viewModel.state.value.testAccountEmail)
        }

    @Test
    fun flagOverrides_areSetRemovedAndCleared() =
        runViewModelTest {
            viewModel.onAction(DebugAction.FlagChanged(FeatureFlag.HOME_BUILD_INFO, enabled = false))
            assertEquals(
                FeatureFlagState(FeatureFlag.HOME_BUILD_INFO, enabled = false, FeatureFlagState.Source.OVERRIDE),
                viewModel.state.value.flags
                    .single(),
            )

            viewModel.onAction(DebugAction.FlagOverrideRemoved(FeatureFlag.HOME_BUILD_INFO))
            assertEquals(
                FeatureFlagState.Source.DEFAULT,
                viewModel.state.value.flags
                    .single()
                    .source,
            )

            viewModel.onAction(DebugAction.FlagChanged(FeatureFlag.HOME_BUILD_INFO, enabled = false))
            viewModel.onAction(DebugAction.AllFlagOverridesRemoved)
            assertEquals(
                FeatureFlagState.Source.DEFAULT,
                viewModel.state.value.flags
                    .single()
                    .source,
            )
        }

    @Test
    fun signInWithTestAccount_signsIn_andSkipsOnboarding() =
        runViewModelTest {
            viewModel.onAction(DebugAction.SignInWithTestAccount)

            assertEquals(listOf("test@example.com"), auth.loginEmails)
            assertTrue(onboarding.isCompleted.value)
            assertTrue(viewModel.state.value.isSignedIn)
        }

    @Test
    fun signInWithTestAccount_whenItFails_saysSo() =
        runViewModelTest {
            auth.nextResult = AppResult.Failure(DataError.Network)
            viewModel.onAction(DebugAction.SignInWithTestAccount)

            assertEquals(DebugMessage.TestSignInFailed, viewModel.state.value.message)
            assertFalse(viewModel.state.value.isSigningIn)
        }

    @Test
    fun showOnboarding_resetsOnboarding_andKeepsTheSession() =
        runViewModelTest {
            auth.login("test@example.com", "Password1")
            onboarding.markCompleted()
            viewModel.onAction(DebugAction.ShowOnboarding)

            assertFalse(onboarding.isCompleted.value)
            assertIs<AuthState.SignedIn>(auth.authState.value)
        }

    @Test
    fun signOut_signsOut() =
        runViewModelTest {
            auth.login("test@example.com", "Password1")
            viewModel.onAction(DebugAction.SignOut)

            assertEquals(AuthState.SignedOut, auth.authState.value)
            assertFalse(viewModel.state.value.isSignedIn)
        }

    @Test
    fun clearData_asksFirst_thenClearsEverythingIncludingFlagOverrides() =
        runViewModelTest {
            auth.login("test@example.com", "Password1")
            onboarding.markCompleted()
            consent.analyticsConsent.value = true
            tasks.tasks.value = listOf(TASK)
            viewModel.onAction(DebugAction.FlagChanged(FeatureFlag.HOME_BUILD_INFO, enabled = false))

            viewModel.onAction(DebugAction.ClearDataClicked)
            assertEquals(DebugConfirmation.ClearData, viewModel.state.value.confirmation)
            assertTrue(onboarding.isCompleted.value)

            viewModel.onAction(DebugAction.Confirmed)
            assertNull(viewModel.state.value.confirmation)
            assertFalse(onboarding.isCompleted.value)
            assertEquals(AuthState.SignedOut, auth.authState.value)
            assertFalse(consent.analyticsConsent.value)
            assertTrue(tasks.tasks.value.isEmpty())
            assertEquals(
                FeatureFlagState.Source.DEFAULT,
                viewModel.state.value.flags
                    .single()
                    .source,
            )
        }

    @Test
    fun dismissingTheConfirmation_doesNothing() =
        runViewModelTest {
            onboarding.markCompleted()
            viewModel.onAction(DebugAction.ClearDataClicked)
            viewModel.onAction(DebugAction.ConfirmationDismissed)

            assertNull(viewModel.state.value.confirmation)
            assertTrue(onboarding.isCompleted.value)
        }

    @Test
    fun crash_asksFirst_thenThrows() =
        runViewModelTest {
            viewModel.onAction(DebugAction.CrashClicked)
            assertEquals(DebugConfirmation.Crash, viewModel.state.value.confirmation)

            assertFailsWith<DebugTestCrashException> { viewModel.onAction(DebugAction.Confirmed) }
        }

    @Test
    fun sendNonFatal_reportsIt() =
        runViewModelTest {
            viewModel.onAction(DebugAction.SendNonFatal)

            assertIs<DebugNonFatalException>(crashReporter.nonFatals.single())
            assertEquals(DebugMessage.NonFatalSent, viewModel.state.value.message)
        }

    /** Every flag defaults to on; overrides win. */
    private class FakeFeatureFlags :
        FeatureFlags,
        FeatureFlagOverrides {
        private val overrides = MutableStateFlow(emptyMap<FeatureFlag, Boolean>())

        override fun isEnabled(flag: FeatureFlag) = overrides.value[flag] ?: true

        override fun observe(flag: FeatureFlag): Flow<Boolean> = overrides.map { it[flag] ?: true }

        override fun observeStates(): Flow<List<FeatureFlagState>> =
            overrides.map { values ->
                FeatureFlag.entries.map { flag ->
                    val override = values[flag]
                    if (override == null) {
                        FeatureFlagState(flag, enabled = true, FeatureFlagState.Source.DEFAULT)
                    } else {
                        FeatureFlagState(flag, override, FeatureFlagState.Source.OVERRIDE)
                    }
                }
            }

        override fun setOverride(
            flag: FeatureFlag,
            enabled: Boolean?,
        ) {
            overrides.value = if (enabled == null) overrides.value - flag else overrides.value + (flag to enabled)
        }

        override fun clearOverrides() {
            overrides.value = emptyMap()
        }
    }

    private class FakeAuthRepository : AuthRepository {
        override val authState = MutableStateFlow<AuthState>(AuthState.SignedOut)
        val loginEmails = mutableListOf<String>()
        var nextResult: AppResult<User> = AppResult.Success(User("test-user-1", "Test User", "test@example.com"))

        override suspend fun login(
            email: String,
            password: String,
        ): AppResult<User> {
            loginEmails += email
            (nextResult as? AppResult.Success)?.let { authState.value = AuthState.SignedIn(it.value) }
            return nextResult
        }

        override suspend fun logout() {
            authState.value = AuthState.SignedOut
        }
    }

    private class FakeOnboardingRepository : OnboardingRepository {
        override val isCompleted = MutableStateFlow(false)

        override suspend fun markCompleted() {
            isCompleted.value = true
        }

        override suspend fun reset() {
            isCompleted.value = false
        }
    }

    private class FakeConsentRepository : ConsentRepository {
        override val analyticsConsent = MutableStateFlow(false)

        override suspend fun setAnalyticsConsent(granted: Boolean) {
            analyticsConsent.value = granted
        }
    }

    private class FakeTaskRepository : TaskRepository {
        override val tasks = MutableStateFlow(emptyList<Task>())

        override suspend fun add(title: String) = Unit

        override suspend fun setDone(
            id: Long,
            done: Boolean,
        ) = Unit

        override suspend fun delete(id: Long) = Unit

        override suspend fun deleteAll() {
            tasks.value = emptyList()
        }
    }

    private companion object {
        val TASK = Task(id = 1, title = "Private", isDone = false, createdAt = Instant.fromEpochMilliseconds(0))
    }
}
