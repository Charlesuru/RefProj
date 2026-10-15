package com.studiodraug.refproj.app.di

import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.network.networkModule
import com.studiodraug.refproj.core.observability.AnalyticsTracker
import com.studiodraug.refproj.core.observability.CrashReporter
import com.studiodraug.refproj.core.observability.Logging
import com.studiodraug.refproj.core.observability.NoOpAnalyticsTracker
import com.studiodraug.refproj.core.observability.NoOpCrashReporter
import com.studiodraug.refproj.core.observability.observabilityModule
import com.studiodraug.refproj.core.storage.storageModule
import com.studiodraug.refproj.data.dataModule
import com.studiodraug.refproj.domain.onboarding.CompleteOnboardingUseCase
import com.studiodraug.refproj.domain.onboarding.ObserveOnboardingCompletedUseCase
import com.studiodraug.refproj.feature.home.homeModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
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
    Logging.configure(config, crashReporter)
    startKoin {
        platform()
        modules(appModules(config, crashReporter, analyticsTracker))
    }
}

/** Every Koin module of the app, in dependency order. Also used by the graph check test. */
internal fun appModules(
    config: AppConfig,
    crashReporter: CrashReporter,
    analyticsTracker: AnalyticsTracker,
): List<Module> =
    listOf(
        module { single { config } },
        observabilityModule(crashReporter, analyticsTracker),
        storageModule,
        networkModule,
        dataModule,
        domainModule,
        // Features
        homeModule,
    )

/** Use cases are stateless: a new instance per injection. */
internal val domainModule: Module =
    module {
        factory { ObserveOnboardingCompletedUseCase(get()) }
        factory { CompleteOnboardingUseCase(get()) }
    }
