package com.studiodraug.refproj.app

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.ObservableSettings
import com.studiodraug.refproj.app.di.appModules
import com.studiodraug.refproj.app.shared.resources.Res
import com.studiodraug.refproj.app.shared.resources.app_tab_profile
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.config.AppEnvironment
import com.studiodraug.refproj.core.observability.LogBuffer
import com.studiodraug.refproj.core.storage.SecureStore
import com.studiodraug.refproj.core.testing.FakeAnalyticsTracker
import com.studiodraug.refproj.core.testing.FakeCrashReporter
import com.studiodraug.refproj.core.testing.FakeSecureStore
import com.studiodraug.refproj.core.testing.PlatformTest
import com.studiodraug.refproj.core.testing.testAppConfig
import com.studiodraug.refproj.domain.tasks.Task
import com.studiodraug.refproj.domain.tasks.TaskRepository
import com.studiodraug.refproj.feature.debug.DebugTestTags
import com.studiodraug.refproj.feature.login.LoginTestTags
import com.studiodraug.refproj.feature.onboarding.OnboardingTestTags
import com.studiodraug.refproj.feature.profile.ProfileTestTags
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.getString
import org.koin.compose.KoinIsolatedContext
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module

// UI TEST

/**
 * End to end through the shared app on both platforms: the real Koin graph, navigation, ViewModels,
 * use cases and the fake auth backend. Only device storage and the database are in memory. Controls are
 * found by their test tags, so the flow does not depend on the device language.
 */
@OptIn(ExperimentalTestApi::class)
class AppFlowTest : PlatformTest() {
    private val graphs = mutableListOf<KoinApplication>()

    @AfterTest
    fun tearDown() = graphs.forEach(KoinApplication::close)

    /** The app's graph in an isolated Koin context: no global state shared between tests. */
    private fun ComposeUiTest.startApp(config: AppConfig) {
        val graph =
            koinApplication {
                allowOverride(true)
                modules(appModules(config, FakeCrashReporter(), FakeAnalyticsTracker(), LogBuffer().takeIf { config.isDebugMenuEnabled }))
                modules(
                    module {
                        single<ObservableSettings> { MapSettings() }
                        single<SecureStore> { FakeSecureStore() }
                        single<TaskRepository> { InMemoryTaskRepository() }
                    },
                )
            }.also(graphs::add)
        setContent { KoinIsolatedContext(context = graph) { App() } }
    }

    @Test
    fun devBuild_firstLaunch_onboardingThenPrefilledLogin_reachesTheTabs_andTheDebugMenu() =
        runComposeUiTest {
            startApp(testAppConfig(environment = AppEnvironment.DEV, isDebugBuild = true))

            awaitTag(OnboardingTestTags.SKIP).performClick()
            awaitTag(OnboardingTestTags.FINISH).performClick()
            // The debug menu pre-fills the test account in non-PROD builds.
            awaitTag(LoginTestTags.SUBMIT).performClick()
            await(hasText(getString(Res.string.app_tab_profile))).performClick()
            awaitTag(ProfileTestTags.LOGOUT)

            onNodeWithTag(DebugTestTags.BADGE).performClick()
            awaitTag(DebugTestTags.MENU)
        }

    @Test
    fun prodRelease_hasNoDebugMenu_andLogsInWithTypedCredentials() =
        runComposeUiTest {
            startApp(testAppConfig(environment = AppEnvironment.PROD, isDebugBuild = false))

            awaitTag(OnboardingTestTags.SKIP).performClick()
            awaitTag(OnboardingTestTags.FINISH).performClick()
            awaitTag(LoginTestTags.EMAIL).performTextInput("test@example.com")
            onNodeWithTag(LoginTestTags.PASSWORD).performTextInput("Password1")
            onNodeWithTag(DebugTestTags.BADGE).assertDoesNotExist()
            onNodeWithTag(LoginTestTags.SUBMIT).performClick()

            await(hasText(getString(Res.string.app_tab_profile))).performClick()
            awaitTag(ProfileTestTags.LOGOUT)
            onNodeWithTag(DebugTestTags.BADGE).assertDoesNotExist()
        }

    private fun ComposeUiTest.awaitTag(tag: String) = await(hasTestTag(tag))

    /** Waits for a node across screen changes and the fake backend's latency. */
    private fun ComposeUiTest.await(matcher: androidx.compose.ui.test.SemanticsMatcher) =
        onNode(matcher).also {
            waitUntil(
                conditionDescription = "$matcher",
                timeoutMillis = TIMEOUT_MS,
            ) { onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
        }

    private class InMemoryTaskRepository : TaskRepository {
        private val state = MutableStateFlow(emptyList<Task>())
        override val tasks: StateFlow<List<Task>> = state

        override suspend fun add(title: String): Unit =
            state.update { it + Task(id = it.size + 1L, title = title, isDone = false, createdAt = Instant.fromEpochMilliseconds(0)) }

        override suspend fun setDone(
            id: Long,
            done: Boolean,
        ) = state.update { tasks -> tasks.map { if (it.id == id) it.copy(isDone = done) else it } }

        override suspend fun delete(id: Long) = state.update { tasks -> tasks.filterNot { it.id == id } }

        override suspend fun deleteAll() = state.update { emptyList() }
    }

    private companion object {
        const val TIMEOUT_MS = 10_000L
    }
}
