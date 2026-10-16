package com.studiodraug.refproj.data.consent

import app.cash.turbine.test
import com.studiodraug.refproj.core.testing.FakeAnalyticsTracker
import com.studiodraug.refproj.core.testing.inMemoryKeyValueStore
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

// INTEGRATION TEST

/** Consent against the real KeyValueStore implementation: opt-in default and applying it to analytics. */
class ConsentRepositoryImplTest {
    private val store = inMemoryKeyValueStore()
    private val analytics = FakeAnalyticsTracker()

    @Test
    fun withoutAChoice_analyticsIsOff() =
        runTest {
            val repository = ConsentRepositoryImpl(store, analytics)
            assertFalse(analytics.collectionEnabled)
            repository.analyticsConsent.test { assertFalse(awaitItem()) }
        }

    @Test
    fun theChoiceIsApplied_andReappliedOnTheNextStart() =
        runTest {
            ConsentRepositoryImpl(store, analytics).setAnalyticsConsent(true)
            assertTrue(analytics.collectionEnabled)

            val nextStart = FakeAnalyticsTracker()
            ConsentRepositoryImpl(store, nextStart).analyticsConsent.test { assertTrue(awaitItem()) }
            assertTrue(nextStart.collectionEnabled)
        }

    @Test
    fun withdrawingConsent_turnsAnalyticsOff() =
        runTest {
            val repository = ConsentRepositoryImpl(store, analytics)
            repository.setAnalyticsConsent(true)
            repository.setAnalyticsConsent(false)
            assertFalse(analytics.collectionEnabled)
        }
}
