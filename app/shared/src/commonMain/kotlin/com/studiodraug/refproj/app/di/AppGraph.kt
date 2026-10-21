package com.studiodraug.refproj.app.di

import com.studiodraug.refproj.app.AppViewModel
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.featureflags.featureFlagsModule
import com.studiodraug.refproj.core.network.networkModule
import com.studiodraug.refproj.core.observability.AnalyticsTracker
import com.studiodraug.refproj.core.observability.CrashReporter
import com.studiodraug.refproj.core.observability.LogBuffer
import com.studiodraug.refproj.core.observability.Logging
import com.studiodraug.refproj.core.observability.LoggingAnalyticsTracker
import com.studiodraug.refproj.core.observability.NoOpAnalyticsTracker
import com.studiodraug.refproj.core.observability.NoOpCrashReporter
import com.studiodraug.refproj.core.observability.RestrictedAnalyticsTracker
import com.studiodraug.refproj.core.observability.observabilityModule
import com.studiodraug.refproj.core.storage.storageModule
import com.studiodraug.refproj.data.dataModule
import com.studiodraug.refproj.domain.auth.GetTestAccountUseCase
import com.studiodraug.refproj.domain.auth.LoginUseCase
import com.studiodraug.refproj.domain.auth.LogoutUseCase
import com.studiodraug.refproj.domain.auth.ObserveAuthStateUseCase
import com.studiodraug.refproj.domain.auth.ObserveCurrentUserUseCase
import com.studiodraug.refproj.domain.consent.ObserveAnalyticsConsentUseCase
import com.studiodraug.refproj.domain.consent.SetAnalyticsConsentUseCase
import com.studiodraug.refproj.domain.debug.ClearLocalDataUseCase
import com.studiodraug.refproj.domain.debug.SignInWithTestAccountUseCase
import com.studiodraug.refproj.domain.onboarding.CompleteOnboardingUseCase
import com.studiodraug.refproj.domain.onboarding.ObserveOnboardingCompletedUseCase
import com.studiodraug.refproj.domain.onboarding.ResetOnboardingUseCase
import com.studiodraug.refproj.domain.tasks.AddTaskUseCase
import com.studiodraug.refproj.domain.tasks.DeleteTaskUseCase
import com.studiodraug.refproj.domain.tasks.ObserveTaskSummaryUseCase
import com.studiodraug.refproj.domain.tasks.ObserveTasksUseCase
import com.studiodraug.refproj.domain.tasks.SetTaskDoneUseCase
import com.studiodraug.refproj.feature.debug.debugModule
import com.studiodraug.refproj.feature.home.homeModule
import com.studiodraug.refproj.feature.login.loginModule
import com.studiodraug.refproj.feature.onboarding.onboardingModule
import com.studiodraug.refproj.feature.profile.profileModule
import com.studiodraug.refproj.feature.tasks.tasksModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * The composition root: the only place that sees every module. Platform shells call [startApp]
 * once, before any UI is shown.
 */
fun startApp(
    config: AppConfig,
    crashReporter: CrashReporter = NoOpCrashReporter,
    analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker,
    platform: KoinAppDeclaration = {},
) {
    // In memory only, for the debug menu's log viewer; never created in a PROD release build.
    val logBuffer = if (config.isDebugMenuEnabled) LogBuffer() else null
    Logging.configure(config, crashReporter, logBuffer)
    startKoin {
        platform()
        modules(appModules(config, crashReporter, analyticsTracker, logBuffer))
    }
}

/** Every Koin module of the app, in dependency order. Also used by the graph check test. */
internal fun appModules(
    config: AppConfig,
    crashReporter: CrashReporter,
    analyticsTracker: AnalyticsTracker,
    logBuffer: LogBuffer? = null,
): List<Module> =
    listOfNotNull(
        module { single { config } },
        observabilityModule(crashReporter, appAnalyticsTracker(config, analyticsTracker), logBuffer),
        storageModule,
        networkModule,
        featureFlagsModule,
        dataModule,
        domainModule,
        appModule,
        // Features
        onboardingModule,
        loginModule,
        homeModule,
        tasksModule,
        profileModule,
        // Debug menu: never in a PROD release build (ADR 0010).
        debugModule.takeIf { config.isDebugMenuEnabled },
    )

/**
 * Analytics collects only with the user's consent, and never in debug builds. With the debug menu
 * enabled every call is also logged, so the log viewer shows what would be sent.
 */
private fun appAnalyticsTracker(
    config: AppConfig,
    native: AnalyticsTracker,
): AnalyticsTracker =
    RestrictedAnalyticsTracker(
        delegate = if (config.isDebugMenuEnabled) LoggingAnalyticsTracker(native) else native,
        collectionAllowed = !config.isDebugBuild,
    )

/** The root of the UI. */
internal val appModule: Module =
    module {
        viewModelOf(::AppViewModel)
    }

/** Use cases are stateless: a new instance per injection. */
internal val domainModule: Module =
    module {
        factory { ObserveOnboardingCompletedUseCase(get()) }
        factory { CompleteOnboardingUseCase(get()) }
        factory { ResetOnboardingUseCase(get()) }
        factory { ObserveAuthStateUseCase(get()) }
        factory { ObserveCurrentUserUseCase(get()) }
        factory { LoginUseCase(get()) }
        factory { LogoutUseCase(get(), get()) }
        factory { GetTestAccountUseCase(get()) }
        factory { ObserveAnalyticsConsentUseCase(get()) }
        factory { SetAnalyticsConsentUseCase(get()) }
        factory { ObserveTasksUseCase(get()) }
        factory { ObserveTaskSummaryUseCase(get()) }
        factory { AddTaskUseCase(get()) }
        factory { SetTaskDoneUseCase(get()) }
        factory { DeleteTaskUseCase(get()) }
        // Debug tools
        factory { SignInWithTestAccountUseCase(get(), get(), get()) }
        factory { ClearLocalDataUseCase(get(), get(), get()) }
    }
