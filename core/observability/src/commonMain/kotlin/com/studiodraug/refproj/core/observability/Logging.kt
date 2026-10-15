package com.studiodraug.refproj.core.observability

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import com.studiodraug.refproj.core.config.AppConfig

/**
 * Structured logging via Kermit. Call [configure] once at startup, before anything logs.
 *
 *  - Debug builds: everything from `Debug` up, to Logcat / OSLog.
 *  - Release builds: `Info` and up to Logcat / OSLog; `Warn` and up also become crash report breadcrumbs,
 *    and errors with an exception become non-fatal reports.
 *
 * Code logs through an injected `Logger` with a tag: `logger.withTag("Network").i { "…" }`.
 * SECURITY: never log secrets, tokens or personal data — logs end up in crash reports.
 */
object Logging {
    fun configure(
        config: AppConfig,
        crashReporter: CrashReporter,
    ) {
        Logger.setMinSeverity(if (config.isDebugBuild) Severity.Debug else Severity.Info)
        Logger.setLogWriters(platformLogWriter(), CrashReporterLogWriter(crashReporter))
        Logger.setTag("RefProj")
    }
}

/** Forwards warnings as breadcrumbs and errors with an exception as non-fatal reports. */
internal class CrashReporterLogWriter(
    private val crashReporter: CrashReporter,
) : LogWriter() {
    override fun isLoggable(
        tag: String,
        severity: Severity,
    ): Boolean = severity >= Severity.Warn

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?,
    ) {
        crashReporter.log("${severity.name.first()}/$tag: $message")
        if (throwable != null && severity >= Severity.Error) crashReporter.recordNonFatal(throwable)
    }
}
