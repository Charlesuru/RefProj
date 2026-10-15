package com.studiodraug.refproj.data.onboarding

import app.cash.turbine.test
import com.studiodraug.refproj.core.testing.inMemoryKeyValueStore
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

// INTEGRATION TEST

/** Repository against the real KeyValueStore implementation (in-memory backing map). */
class OnboardingRepositoryImplTest {
    private val store = inMemoryKeyValueStore()
    private val repository = OnboardingRepositoryImpl(store)

    @Test
    fun state_persistsAcrossRepositoryInstances() =
        runTest {
            repository.markCompleted()
            OnboardingRepositoryImpl(store).isCompleted.test {
                assertTrue(awaitItem())
            }
        }

    @Test
    fun reset_returnsToNotCompleted() =
        runTest {
            repository.isCompleted.test {
                assertFalse(awaitItem())
                repository.markCompleted()
                assertTrue(awaitItem())
                repository.reset()
                assertFalse(awaitItem())
            }
        }
}
