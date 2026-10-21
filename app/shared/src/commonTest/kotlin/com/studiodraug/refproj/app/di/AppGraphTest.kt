package com.studiodraug.refproj.app.di

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.ObservableSettings
import com.studiodraug.refproj.app.AppViewModel
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.config.AppEnvironment
import com.studiodraug.refproj.core.featureflags.FeatureFlagOverrides
import com.studiodraug.refproj.core.featureflags.FeatureFlags
import com.studiodraug.refproj.core.observability.LogBuffer
import com.studiodraug.refproj.core.storage.KeyValueStore
import com.studiodraug.refproj.core.storage.SecureStore
import com.studiodraug.refproj.core.testing.FakeAnalyticsTracker
import com.studiodraug.refproj.core.testing.FakeCrashReporter
import com.studiodraug.refproj.core.testing.FakeSecureStore
import com.studiodraug.refproj.core.testing.runViewModelTest
import com.studiodraug.refproj.core.testing.testAppConfig
import com.studiodraug.refproj.domain.auth.AuthRepository
import com.studiodraug.refproj.domain.auth.GetTestAccountUseCase
import com.studiodraug.refproj.domain.auth.LoginUseCase
import com.studiodraug.refproj.domain.auth.LogoutUseCase
import com.studiodraug.refproj.domain.auth.ObserveAuthStateUseCase
import com.studiodraug.refproj.domain.auth.ObserveCurrentUserUseCase
import com.studiodraug.refproj.domain.auth.TestAccountProvider
import com.studiodraug.refproj.domain.consent.ConsentRepository
import com.studiodraug.refproj.domain.consent.ObserveAnalyticsConsentUseCase
import com.studiodraug.refproj.domain.consent.SetAnalyticsConsentUseCase
import com.studiodraug.refproj.domain.debug.ClearLocalDataUseCase
import com.studiodraug.refproj.domain.debug.SignInWithTestAccountUseCase
import com.studiodraug.refproj.domain.onboarding.CompleteOnboardingUseCase
import com.studiodraug.refproj.domain.onboarding.ObserveOnboardingCompletedUseCase
import com.studiodraug.refproj.domain.onboarding.OnboardingRepository
import com.studiodraug.refproj.domain.onboarding.ResetOnboardingUseCase
import com.studiodraug.refproj.domain.tasks.AddTaskUseCase
import com.studiodraug.refproj.domain.tasks.DeleteTaskUseCase
import com.studiodraug.refproj.domain.tasks.ObserveTaskSummaryUseCase
import com.studiodraug.refproj.domain.tasks.ObserveTasksUseCase
import com.studiodraug.refproj.domain.tasks.SetTaskDoneUseCase
import com.studiodraug.refproj.domain.tasks.Task
import com.studiodraug.refproj.domain.tasks.TaskRepository
import com.studiodraug.refproj.feature.home.HomeViewModel
import com.studiodraug.refproj.feature.login.LoginViewModel
import com.studiodraug.refproj.feature.onboarding.OnboardingViewModel
import com.studiodraug.refproj.feature.profile.ProfileViewModel
import com.studiodraug.refproj.feature.tasks.TasksViewModel
import io.ktor.client.HttpClient
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import org.koin.dsl.module

// INTEGRATION TEST

/**
 * Builds the real Koin graph and resolves the important types, on Android and iOS. Catches a
 * missing binding at test time instead of as a crash on launch. Only what needs a device is replaced:
 * platform storage (SharedPreferences / NSUserDefaults / Keystore) and the Room database behind
 * [TaskRepository] (covered by `TaskRepositoryImplTest`).
 */
class AppGraphTest {
    private val config = testAppConfig(isDebugBuild = false)
    private val analytics = FakeAnalyticsTracker()
    private val graphs = mutableListOf<Koin>()
    private val koin = graph(config, LogBuffer())

    /** The app's graph as [startApp] builds it, with the device-only parts replaced. */
    private fun graph(
        config: AppConfig,
        logBuffer: LogBuffer?,
    ): Koin =
        koinApplication {
            allowOverride(true)
            modules(appModules(config, FakeCrashReporter(), analytics, logBuffer))
            modules(
                module {
                    single<ObservableSettings> { MapSettings() }
                    single<SecureStore> { FakeSecureStore() }
                    single<TaskRepository> { NoTaskRepository }
                },
            )
        }.koin.also(graphs::add)

    @AfterTest
    fun tearDown() = graphs.forEach(Koin::close)

    @Test
    fun graph_resolvesEveryEntryPoint() {
        assertSame(config, koin.get<AppConfig>())
        koin.get<KeyValueStore>()
        koin.get<HttpClient>()
        koin.get<FeatureFlags>()
        koin.get<FeatureFlagOverrides>()
        koin.get<OnboardingRepository>()
        koin.get<ObserveOnboardingCompletedUseCase>()
        koin.get<CompleteOnboardingUseCase>()
        koin.get<AuthRepository>()
        koin.get<ObserveAuthStateUseCase>()
        koin.get<ObserveCurrentUserUseCase>()
        koin.get<LoginUseCase>()
        koin.get<LogoutUseCase>()
        koin.get<ConsentRepository>()
        koin.get<ObserveAnalyticsConsentUseCase>()
        koin.get<SetAnalyticsConsentUseCase>()
        koin.get<ObserveTasksUseCase>()
        koin.get<ObserveTaskSummaryUseCase>()
        koin.get<AddTaskUseCase>()
        koin.get<SetTaskDoneUseCase>()
        koin.get<DeleteTaskUseCase>()
        koin.get<ResetOnboardingUseCase>()
        koin.get<GetTestAccountUseCase>()
        koin.get<SignInWithTestAccountUseCase>()
        koin.get<ClearLocalDataUseCase>()
    }

    @Test
    fun debugTools_areAvailableInEveryNonProdEnvironment() {
        AppEnvironment.entries.filterNot { it.isProduction }.forEach { environment ->
            val graph = graph(testAppConfig(environment, isDebugBuild = false), LogBuffer())
            assertNotNull(graph.get<TestAccountProvider>().testAccount(), "$environment")
            assertNotNull(graph.getOrNull<LogBuffer>(), "$environment")
        }
    }

    @Test
    fun prodReleaseBuild_hasNoDebugTools() {
        val graph = graph(testAppConfig(AppEnvironment.PROD, isDebugBuild = false), logBuffer = null)

        assertNull(graph.get<TestAccountProvider>().testAccount())
        assertNull(graph.getOrNull<LogBuffer>())
    }

    @Test
    fun graph_createsEveryViewModel() =
        runViewModelTest {
            koin.get<AppViewModel>()
            koin.get<OnboardingViewModel>()
            koin.get<LoginViewModel>()
            koin.get<HomeViewModel>()
            koin.get<TasksViewModel>()
            koin.get<ProfileViewModel>()
        }

    @Test
    fun startup_appliesTheStoredConsent_soAnalyticsIsOffUntilTheUserOptsIn() {
        assertFalse(analytics.collectionEnabled)
    }

    private object NoTaskRepository : TaskRepository {
        override val tasks: Flow<List<Task>> = emptyFlow()

        override suspend fun add(title: String) = Unit

        override suspend fun setDone(
            id: Long,
            done: Boolean,
        ) = Unit

        override suspend fun delete(id: Long) = Unit

        override suspend fun deleteAll() = Unit
    }
}
