package com.studiodraug.refproj.core.observability

import co.touchlab.kermit.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

// UNIT TEST
class LogBufferTest {
    private val now = Instant.fromEpochMilliseconds(1_000)
    private val buffer = LogBuffer(capacity = 3, clock = FixedClock(now))

    @Test
    fun entries_keepTheLine_withTimeSeverityTagAndStackTrace() {
        buffer.log(Severity.Error, "failed", "Sync", IllegalStateException("boom"))

        val entry = buffer.entries.value.single()
        assertEquals(LogEntry(now, Severity.Error, "Sync", "failed", entry.throwable), entry)
        assertTrue(assertNotNull(entry.throwable).contains("boom"))
    }

    @Test
    fun whenFull_theOldestLinesAreDropped() {
        (1..5).forEach { buffer.log(Severity.Info, "line $it", "Test", null) }

        assertEquals(listOf("line 3", "line 4", "line 5"), buffer.entries.value.map { it.message })
    }

    @Test
    fun clear_removesEverything() {
        buffer.log(Severity.Info, "line", "Test", null)
        buffer.clear()

        assertTrue(buffer.entries.value.isEmpty())
    }

    private class FixedClock(
        private val instant: Instant,
    ) : Clock {
        override fun now(): Instant = instant
    }
}
