package com.studiodraug.refproj.feature.profile

import com.studiodraug.refproj.core.testing.runViewModelTest
import com.studiodraug.refproj.core.testing.testAppConfig
import com.studiodraug.refproj.domain.auth.AuthRepository
import com.studiodraug.refproj.domain.auth.AuthState
import com.studiodraug.refproj.domain.auth.LogoutUseCase
import com.studiodraug.refproj.domain.auth.ObserveCurrentUserUseCase
import com.studiodraug.refproj.domain.auth.User
import com.studiodraug.refproj.domain.consent.ConsentRepository
import com.studiodraug.refproj.domain.consent.ObserveAnalyticsConsentUseCase
import com.studiodraug.refproj.domain.consent.SetAnalyticsConsentUseCase
import com.studiodraug.refproj.domain.model.AppResult
import com.studiodraug.refproj.domain.tasks.Task
import com.studiodraug.refproj.domain.tasks.TaskRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow

// UNIT TEST
class ProfileViewModelTest {
    private val auth = FakeAuthRepository()
    private val consent = FakeConsentRepository()
    private val tasks = FakeTaskRepository()

    private fun viewModel() =
        ProfileViewModel(
            ObserveCurrentUserUseCase(auth),
            ObserveAnalyticsConsentUseCase(consent),
            SetAnalyticsConsentUseCase(consent),
            LogoutUseCase(auth, tasks),
            testAppConfig(),
        )

    @Test
    fun showsTheUser_theConsent_andTheVersion() =
        runViewModelTest {
            consent.analyticsConsent.value = true
            val state = viewModel().state.value
            assertEquals("Ada", state.displayName)
            assertEquals("ada@example.com", state.email)
            assertTrue(state.analyticsConsent)
            assertEquals("1.0.0-test (1)", state.version)
        }

    @Test
    fun toggling_storesTheConsent() =
        runViewModelTest {
            val viewModel = viewModel()
            viewModel.onAction(ProfileAction.AnalyticsConsentChanged(granted = true))
            assertTrue(consent.analyticsConsent.value)
            assertTrue(viewModel.state.value.analyticsConsent)
        }

    @Test
    fun logout_needsConfirmation() =
        runViewModelTest {
            val viewModel = viewModel()
            viewModel.onAction(ProfileAction.LogoutClicked)
            assertTrue(viewModel.state.value.showLogoutConfirmation)
            viewModel.onAction(ProfileAction.LogoutDismissed)
            assertFalse(viewModel.state.value.showLogoutConfirmation)
            assertEquals(AuthState.SignedIn(USER), auth.authState.value)
        }

    @Test
    fun confirmedLogout_signsOut_deletesTasks_andKeepsShowingTheUser() =
        runViewModelTest {
            val viewModel = viewModel()
            viewModel.onAction(ProfileAction.LogoutClicked)
            viewModel.onAction(ProfileAction.LogoutConfirmed)

            assertEquals(AuthState.SignedOut, auth.authState.value)
            assertTrue(tasks.deletedAll)
            assertTrue(viewModel.state.value.isLoggingOut)
            assertEquals("Ada", viewModel.state.value.displayName)
        }

    private class FakeAuthRepository : AuthRepository {
        override val authState = MutableStateFlow<AuthState>(AuthState.SignedIn(USER))

        override suspend fun login(
            email: String,
            password: String,
        ): AppResult<User> = AppResult.Success(USER)

        override suspend fun logout() {
            authState.value = AuthState.SignedOut
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
        var deletedAll = false

        override suspend fun add(title: String) = Unit

        override suspend fun setDone(
            id: Long,
            done: Boolean,
        ) = Unit

        override suspend fun delete(id: Long) = Unit

        override suspend fun deleteAll() {
            deletedAll = true
        }
    }

    private companion object {
        val USER = User(id = "u1", displayName = "Ada", email = "ada@example.com")
    }
}
