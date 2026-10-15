package com.studiodraug.refproj.core.observability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// UNIT TEST
class AnalyticsEventTest {
    @Test
    fun validEvent_isAccepted() {
        val event = AnalyticsEvent("onboarding_completed", mapOf("page_count" to "3"))
        assertEquals("onboarding_completed", event.name)
    }

    @Test
    fun invalidNames_areRejected() {
        listOf("", "Login", "1st_open", "has-dash", "a".repeat(41)).forEach { name ->
            assertFailsWith<IllegalArgumentException>(name) { AnalyticsEvent(name) }
        }
    }

    @Test
    fun invalidParameters_areRejected() {
        assertFailsWith<IllegalArgumentException> { AnalyticsEvent("event", mapOf("Bad Key" to "x")) }
        assertFailsWith<IllegalArgumentException> { AnalyticsEvent("event", mapOf("key" to "x".repeat(101))) }
        assertFailsWith<IllegalArgumentException> {
            AnalyticsEvent("event", (1..26).associate { "key_$it" to "x" })
        }
    }
}
