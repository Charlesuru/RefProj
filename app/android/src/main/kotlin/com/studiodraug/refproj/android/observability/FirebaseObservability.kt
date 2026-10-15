package com.studiodraug.refproj.android.observability

import android.content.Context
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.observability.AnalyticsEvent
import com.studiodraug.refproj.core.observability.AnalyticsTracker
import com.studiodraug.refproj.core.observability.CrashReporter
import com.studiodraug.refproj.core.observability.NoOpAnalyticsTracker
import com.studiodraug.refproj.core.observability.NoOpCrashReporter

/**
 * Native Firebase behind the shared observability interfaces.
 *
 * Collection is off by default (AndroidManifest meta-data) and switched on here for release builds
 * only, so debug builds never pollute production dashboards. Without `google-services.json`
 * Firebase is not initialised and the no-op implementations are used.
 */
internal class FirebaseObservability private constructor(
    val crashReporter: CrashReporter,
    val analyticsTracker: AnalyticsTracker,
) {
    companion object {
        fun create(
            context: Context,
            config: AppConfig,
        ): FirebaseObservability {
            if (FirebaseApp.getApps(context).isEmpty()) {
                return FirebaseObservability(NoOpCrashReporter, NoOpAnalyticsTracker)
            }
            val collect = !config.isDebugBuild
            val crashlytics = FirebaseCrashlytics.getInstance().apply { isCrashlyticsCollectionEnabled = collect }
            val analytics = FirebaseAnalytics.getInstance(context).apply { setAnalyticsCollectionEnabled(collect) }
            crashlytics.setCustomKey("environment", config.environment.name)
            return FirebaseObservability(CrashlyticsCrashReporter(crashlytics), FirebaseAnalyticsTracker(analytics))
        }
    }
}

private class CrashlyticsCrashReporter(
    private val crashlytics: FirebaseCrashlytics,
) : CrashReporter {
    override fun log(message: String) = crashlytics.log(message)

    override fun recordNonFatal(throwable: Throwable) = crashlytics.recordException(throwable)

    override fun setUserId(id: String?) = crashlytics.setUserId(id.orEmpty())

    override fun setCustomKey(
        key: String,
        value: String,
    ) = crashlytics.setCustomKey(key, value)
}

private class FirebaseAnalyticsTracker(
    private val analytics: FirebaseAnalytics,
) : AnalyticsTracker {
    override fun logEvent(event: AnalyticsEvent) =
        analytics.logEvent(event.name, Bundle().apply { event.parameters.forEach { (key, value) -> putString(key, value) } })

    override fun logScreenView(screenName: String) =
        analytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply { putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName) },
        )

    override fun setUserId(id: String?) = analytics.setUserId(id)

    override fun setCollectionEnabled(enabled: Boolean) = analytics.setAnalyticsCollectionEnabled(enabled)
}
