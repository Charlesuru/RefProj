package com.studiodraug.refproj.core.observability

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** One line of the debug menu's log viewer. [throwable] is the stack trace, if any. */
data class LogEntry(
    val timestamp: Instant,
    val severity: Severity,
    val tag: String,
    val message: String,
    val throwable: String? = null,
)

/**
 * Keeps the most recent log lines for the debug menu's log viewer. Installed only while the debug menu
 * is enabled (never in a PROD release build) and in memory only: nothing is written to disk or sent.
 * Thread-safe — Kermit calls writers from any thread.
 */
class LogBuffer(
    private val capacity: Int = DEFAULT_CAPACITY,
    private val clock: Clock = Clock.System,
) : LogWriter() {
    private val mutableEntries = MutableStateFlow<List<LogEntry>>(emptyList())

    /** Oldest first, at most [capacity] entries. */
    val entries: StateFlow<List<LogEntry>> = mutableEntries.asStateFlow()

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?,
    ) {
        val entry = LogEntry(clock.now(), severity, tag, message, throwable?.stackTraceToString())
        mutableEntries.update { (it + entry).takeLast(capacity) }
    }

    fun clear() {
        mutableEntries.value = emptyList()
    }

    companion object {
        const val DEFAULT_CAPACITY = 500
    }
}
