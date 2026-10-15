package com.studiodraug.refproj.core.observability

import co.touchlab.kermit.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// UNIT TEST
class CrashReporterLogWriterTest {
    private val reporter = RecordingCrashReporter()
    private val writer = CrashReporterLogWriter(reporter)

    @Test
    fun onlyWarningsAndAbove_areForwarded() {
        assertFalse(writer.isLoggable("tag", Severity.Info))
        assertTrue(writer.isLoggable("tag", Severity.Warn))
        assertTrue(writer.isLoggable("tag", Severity.Error))
    }

    @Test
    fun errorWithThrowable_isBreadcrumbAndNonFatal() {
        val error = IllegalStateException("boom")
        writer.log(Severity.Error, "failed", "Sync", error)

        assertEquals(listOf("E/Sync: failed"), reporter.breadcrumbs)
        assertEquals(listOf<Throwable>(error), reporter.nonFatals)
    }

    @Test
    fun warningWithThrowable_isOnlyBreadcrumb() {
        writer.log(Severity.Warn, "slow", "Network", IllegalStateException())

        assertEquals(listOf("W/Network: slow"), reporter.breadcrumbs)
        assertTrue(reporter.nonFatals.isEmpty())
    }

    private class RecordingCrashReporter : CrashReporter {
        val breadcrumbs = mutableListOf<String>()
        val nonFatals = mutableListOf<Throwable>()

        override fun log(message: String) {
            breadcrumbs += message
        }

        override fun recordNonFatal(throwable: Throwable) {
            nonFatals += throwable
        }

        override fun setUserId(id: String?) = Unit

        override fun setCustomKey(
            key: String,
            value: String,
        ) = Unit
    }
}
