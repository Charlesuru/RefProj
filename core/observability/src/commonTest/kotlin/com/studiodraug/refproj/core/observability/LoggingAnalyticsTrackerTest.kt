package com.studiodraug.refproj.core.observability

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import kotlin.test.Test
import kotlin.test.assertEquals

// UNIT TEST
class LoggingAnalyticsTrackerTest {
    private val logs = LogBuffer()
    private val delegate = RecordingTracker()
    private val tracker =
        LoggingAnalyticsTracker(
            delegate = delegate,
            logger = Logger(StaticConfig(minSeverity = Severity.Verbose, logWriterList = listOf(logs))),
        )

    @Test
    fun everyCall_isLogged_andForwarded() {
        tracker.logEvent(AnalyticsEvent("task_added"))
        tracker.logEvent(AnalyticsEvent("login", mapOf("method" to "password")))
        tracker.logScreenView("home")
        tracker.setCollectionEnabled(false)

        assertEquals(
            listOf("Event task_added", "Event login {method=password}", "Screen home", "Collection off"),
            logs.entries.value.map { it.message },
        )
        assertEquals(
            setOf("Analytics"),
            logs.entries.value
                .map { it.tag }
                .toSet(),
        )
        assertEquals(listOf("task_added", "login"), delegate.events.map { it.name })
        assertEquals(listOf("home"), delegate.screens)
    }

    @Test
    fun theUserId_isForwarded_butNeverLogged() {
        tracker.setUserId("user-42")
        tracker.setUserId(null)

        assertEquals(listOf("User ID set", "User ID cleared"), logs.entries.value.map { it.message })
        assertEquals(listOf<String?>("user-42", null), delegate.userIds)
    }

    private class RecordingTracker : AnalyticsTracker {
        val events = mutableListOf<AnalyticsEvent>()
        val screens = mutableListOf<String>()
        val userIds = mutableListOf<String?>()

        override fun logEvent(event: AnalyticsEvent) {
            events += event
        }

        override fun logScreenView(screenName: String) {
            screens += screenName
        }

        override fun setUserId(id: String?) {
            userIds += id
        }

        override fun setCollectionEnabled(enabled: Boolean) = Unit
    }
}
