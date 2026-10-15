package com.studiodraug.refproj.core.observability

/**
 * Crash and non-fatal error reporting (Firebase Crashlytics in release builds).
 * Implemented natively by each platform shell and injected at startup; shared code only sees this interface.
 *
 * PRIVACY: never pass personal data (names, e-mail, tokens) in messages, keys or exceptions.
 */
interface CrashReporter {
    /** Breadcrumb attached to the next crash or non-fatal report. */
    fun log(message: String)

    /** Reports a caught exception that did not crash the app. */
    fun recordNonFatal(throwable: Throwable)

    /** Pseudonymous user ID (never an e-mail address); `null` after logout. */
    fun setUserId(id: String?)

    fun setCustomKey(
        key: String,
        value: String,
    )
}

/** Used when no crash reporting is configured (local builds without Firebase config, tests). */
object NoOpCrashReporter : CrashReporter {
    override fun log(message: String) = Unit

    override fun recordNonFatal(throwable: Throwable) = Unit

    override fun setUserId(id: String?) = Unit

    override fun setCustomKey(
        key: String,
        value: String,
    ) = Unit
}
