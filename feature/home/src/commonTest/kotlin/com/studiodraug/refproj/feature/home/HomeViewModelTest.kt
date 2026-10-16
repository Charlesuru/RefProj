package com.studiodraug.refproj.feature.home

import app.cash.turbine.test
import com.studiodraug.refproj.core.config.AppEnvironment
import com.studiodraug.refproj.core.featureflags.FeatureFlag
import com.studiodraug.refproj.core.featureflags.FeatureFlagState
import com.studiodraug.refproj.core.featureflags.FeatureFlags
import com.studiodraug.refproj.core.testing.runViewModelTest
import com.studiodraug.refproj.core.testing.testAppConfig
import com.studiodraug.refproj.domain.auth.AuthRepository
import com.studiodraug.refproj.domain.auth.AuthState
import com.studiodraug.refproj.domain.auth.ObserveCurrentUserUseCase
import com.studiodraug.refproj.domain.auth.User
import com.studiodraug.refproj.domain.model.AppResult
import com.studiodraug.refproj.domain.tasks.ObserveTaskSummaryUseCase
import com.studiodraug.refproj.domain.tasks.Task
import com.studiodraug.refproj.domain.tasks.TaskRepository
import com.studiodraug.refproj.domain.tasks.TaskSummary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

// UNIT TEST
class HomeViewModelTest {
    private val buildInfo = MutableStateFlow(true)
    private val flags =
        object : FeatureFlags {
            override fun isEnabled(flag: FeatureFlag) = buildInfo.value

            override fun observe(flag: FeatureFlag): Flow<Boolean> = buildInfo

            override fun observeStates(): Flow<List<FeatureFlagState>> = emptyFlow()
        }
    private val auth = FakeAuthRepository()
    private val tasks = FakeTaskRepository()

    private fun viewModel() =
        HomeViewModel(
            testAppConfig(environment = AppEnvironment.SIT),
            flags,
            ObserveCurrentUserUseCase(auth),
            ObserveTaskSummaryUseCase(tasks),
        )

    @Test
    fun showsTheGreeting_andTheTaskCounts() =
        runViewModelTest {
            viewModel().state.test {
                assertEquals(
                    HomeUiState(
                        displayName = "Ada",
                        summary = TaskSummary(open = 0, done = 0),
                        environmentName = "SIT",
                        version = "1.0.0-test (1)",
                    ),
                    awaitItem(),
                )
                tasks.state.value = listOf(task(1, done = false), task(2, done = true), task(3, done = false))
                assertEquals(TaskSummary(open = 2, done = 1), awaitItem().summary)
            }
        }

    @Test
    fun buildInfo_followsTheFeatureFlag() =
        runViewModelTest {
            viewModel().state.test {
                assertEquals("1.0.0-test (1)", awaitItem().version)
                buildInfo.value = false
                assertNull(awaitItem().version)
            }
        }

    private fun task(
        id: Long,
        done: Boolean,
    ) = Task(id = id, title = "Task $id", isDone = done, createdAt = Instant.fromEpochMilliseconds(0))

    private class FakeAuthRepository : AuthRepository {
        override val authState = MutableStateFlow<AuthState>(AuthState.SignedIn(User("u1", "Ada", "ada@example.com")))

        override suspend fun login(
            email: String,
            password: String,
        ): AppResult<User> = error("Not used")

        override suspend fun logout() = Unit
    }

    private class FakeTaskRepository : TaskRepository {
        val state = MutableStateFlow(emptyList<Task>())
        override val tasks: Flow<List<Task>> = state

        override suspend fun add(title: String) = Unit

        override suspend fun setDone(
            id: Long,
            done: Boolean,
        ) = Unit

        override suspend fun delete(id: Long) = Unit

        override suspend fun deleteAll() = Unit
    }
}
