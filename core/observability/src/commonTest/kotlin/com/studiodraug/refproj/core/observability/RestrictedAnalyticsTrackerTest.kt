package com.studiodraug.refproj.core.observability

import kotlin.test.Test
import kotlin.test.assertEquals

// UNIT TEST
class RestrictedAnalyticsTrackerTest {
    private class RecordingTracker : AnalyticsTracker {
        val collection = mutableListOf<Boolean>()
        val events = mutableListOf<AnalyticsEvent>()

        override fun logEvent(event: AnalyticsEvent) {
            events += event
        }

        override fun logScreenView(screenName: String) = Unit

        override fun setUserId(id: String?) = Unit

        override fun setCollectionEnabled(enabled: Boolean) {
            collection += enabled
        }
    }

    @Test
    fun releaseBuild_followsTheConsent() {
        val delegate = RecordingTracker()
        val tracker = RestrictedAnalyticsTracker(delegate, collectionAllowed = true)
        tracker.setCollectionEnabled(true)
        tracker.setCollectionEnabled(false)
        assertEquals(listOf(true, false), delegate.collection)
    }

    @Test
    fun debugBuild_neverCollects_evenWithConsent() {
        val delegate = RecordingTracker()
        val tracker = RestrictedAnalyticsTracker(delegate, collectionAllowed = false)
        tracker.setCollectionEnabled(true)
        assertEquals(listOf(false), delegate.collection)
    }

    @Test
    fun events_areForwarded() {
        val delegate = RecordingTracker()
        RestrictedAnalyticsTracker(delegate, collectionAllowed = false).logEvent(AnalyticsEvent("login"))
        assertEquals(listOf(AnalyticsEvent("login")), delegate.events)
    }
}
