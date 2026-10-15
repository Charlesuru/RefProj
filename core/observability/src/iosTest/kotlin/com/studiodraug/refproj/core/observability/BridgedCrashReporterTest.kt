package com.studiodraug.refproj.core.observability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// UNIT TEST
class BridgedCrashReporterTest {
    @Test
    fun kotlinException_isPassedToSwiftAsNameReasonAndStack() {
        val native = RecordingNativeCrashReporter()

        BridgedCrashReporter(native).recordNonFatal(IllegalStateException("sync failed"))

        val (name, reason, stack) = native.errors.single()
        assertEquals("kotlin.IllegalStateException", name)
        assertEquals("sync failed", reason)
        assertTrue(stack.isNotEmpty(), "Kotlin stack trace should be included")
    }

    private class RecordingNativeCrashReporter : NativeCrashReporter {
        val errors = mutableListOf<Triple<String, String, List<String>>>()

        override fun log(message: String) = Unit

        override fun recordError(
            name: String,
            reason: String,
            stackTrace: List<String>,
        ) {
            errors += Triple(name, reason, stackTrace)
        }

        override fun setUserId(id: String?) = Unit

        override fun setCustomKey(
            key: String,
            value: String,
        ) = Unit
    }
}
