package com.studiodraug.refproj.core.observability

import co.touchlab.kermit.Logger
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin bindings for logging, crash reporting and analytics. The platform shell passes its native
 * (Firebase) implementations; without them the no-op ones are used. [logBuffer] is bound only when
 * given (debug menu enabled).
 */
fun observabilityModule(
    crashReporter: CrashReporter = NoOpCrashReporter,
    analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker,
    logBuffer: LogBuffer? = null,
): Module =
    module {
        single<Logger> { Logger }
        single<CrashReporter> { crashReporter }
        single<AnalyticsTracker> { analyticsTracker }
        if (logBuffer != null) single { logBuffer }
    }
