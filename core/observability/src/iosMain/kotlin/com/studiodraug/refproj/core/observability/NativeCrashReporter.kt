package com.studiodraug.refproj.core.observability

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.concurrent.ThreadLocal

/**
 * Crash reporting as Swift implements it (Firebase Crashlytics in `iosApp`). Only plain strings
 * cross the language boundary; [BridgedCrashReporter] turns Kotlin exceptions into them.
 */
interface NativeCrashReporter {
    fun log(message: String)

    /** [stackTrace] holds one Kotlin/Native frame per entry, innermost first. */
    fun recordError(
        name: String,
        reason: String,
        stackTrace: List<String>,
    )

    fun setUserId(id: String?)

    fun setCustomKey(
        key: String,
        value: String,
    )
}

/** [CrashReporter] on top of the Swift implementation. */
class BridgedCrashReporter(
    private val native: NativeCrashReporter,
) : CrashReporter {
    override fun log(message: String) = native.log(message)

    @OptIn(ExperimentalNativeApi::class) // getStackTrace: the Kotlin/Native frames, needed for readable reports.
    override fun recordNonFatal(throwable: Throwable) =
        native.recordError(
            name = throwable::class.qualifiedName ?: "KotlinException",
            reason = throwable.message.orEmpty(),
            stackTrace = throwable.getStackTrace().toList(),
        )

    override fun setUserId(id: String?) = native.setUserId(id)

    override fun setCustomKey(
        key: String,
        value: String,
    ) = native.setCustomKey(key, value)
}

/**
 * Reports an uncaught Kotlin exception (with its Kotlin stack trace) before the app terminates.
 * Crashlytics also captures the crash itself; this adds the readable Kotlin exception to it.
 */
@OptIn(ExperimentalNativeApi::class) // setUnhandledExceptionHook is the only hook Kotlin/Native offers.
fun installUnhandledExceptionReporting(crashReporter: CrashReporter) {
    if (UnhandledExceptionReporting.installed) return
    UnhandledExceptionReporting.installed = true
    val previous =
        setUnhandledExceptionHook { throwable ->
            crashReporter.log("Uncaught Kotlin exception")
            crashReporter.recordNonFatal(throwable)
            terminateWithUnhandledException(throwable)
        }
    check(previous == null) { "Another unhandled exception hook was already installed" }
}

@ThreadLocal
private object UnhandledExceptionReporting {
    var installed = false
}
