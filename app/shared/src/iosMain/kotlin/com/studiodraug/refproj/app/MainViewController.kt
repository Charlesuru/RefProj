package com.studiodraug.refproj.app

import androidx.compose.ui.window.ComposeUIViewController
import com.studiodraug.refproj.app.di.startApp
import com.studiodraug.refproj.core.config.AppConfig
import com.studiodraug.refproj.core.observability.AnalyticsTracker
import com.studiodraug.refproj.core.observability.BridgedCrashReporter
import com.studiodraug.refproj.core.observability.NativeCrashReporter
import com.studiodraug.refproj.core.observability.NoOpAnalyticsTracker
import com.studiodraug.refproj.core.observability.NoOpCrashReporter
import com.studiodraug.refproj.core.observability.installUnhandledExceptionReporting
import platform.UIKit.UIViewController

/**
 * Called once from Swift at launch (`RefProjApp.init`), before any UI. [config] is built from
 * Info.plist, which the active xcconfig in iosApp/Configs populates. The Firebase implementations
 * are `nil` when the build has no Firebase config.
 */
fun startIosApp(
    config: AppConfig,
    crashReporter: NativeCrashReporter?,
    analyticsTracker: AnalyticsTracker?,
) {
    val reporter = crashReporter?.let(::BridgedCrashReporter) ?: NoOpCrashReporter
    installUnhandledExceptionReporting(reporter)
    startApp(config, reporter, analyticsTracker ?: NoOpAnalyticsTracker)
}

/** Hosts the shared Compose UI. Requires [startIosApp] to have run. */
@Suppress("FunctionName", "unused") // Called from Swift as MainViewControllerKt.MainViewController()
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
