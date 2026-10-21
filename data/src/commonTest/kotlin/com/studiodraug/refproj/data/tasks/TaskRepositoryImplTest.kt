package com.studiodraug.refproj.data.tasks

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.studiodraug.refproj.core.testing.PlatformTest
import com.studiodraug.refproj.data.database.inMemoryTestDatabase
import com.studiodraug.refproj.domain.tasks.Task
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

// INTEGRATION TEST

/** Repository against a real Room database in memory: generated DAO, SQL, schema and the Flow invalidation. */
class TaskRepositoryImplTest : PlatformTest() {
    private val database = inMemoryTestDatabase()
    private var now = Instant.fromEpochMilliseconds(1_000)
    private val clock =
        object : Clock {
            override fun now(): Instant = now
        }
    private val repository = TaskRepositoryImpl(database.taskDao(), clock)

    @AfterTest
    fun tearDown() = database.close()

    @Test
    fun add_storesAnOpenTask_withTheCurrentTime() =
        runTest {
            repository.add("Buy milk")
            val task = repository.tasks.first().single()
            assertEquals("Buy milk", task.title)
            assertEquals(false, task.isDone)
            assertEquals(now, task.createdAt)
        }

    @Test
    fun tasks_areOrderedOpenFirst_newestFirst() =
        runTest {
            listOf("old", "middle", "new").forEach { title ->
                repository.add(title)
                now += 1.seconds
            }
            val middle = repository.tasks.first().first { it.title == "middle" }
            repository.setDone(middle.id, done = true)

            assertEquals(listOf("new", "old", "middle"), repository.tasks.first().map { it.title })
        }

    @Test
    fun changes_areEmitted_andDeleteAllEmptiesTheTable() =
        runTest {
            repository.tasks.test {
                awaitUntil { it.isEmpty() }
                repository.add("A")
                val added = awaitUntil { it.size == 1 }.single()
                repository.delete(added.id)
                awaitUntil { it.isEmpty() }
                repository.add("B")
                repository.add("C")
                awaitUntil { it.size == 2 }
                repository.deleteAll()
                awaitUntil { it.isEmpty() }
                cancelAndIgnoreRemainingEvents()
            }
        }

    /**
     * Room may merge quick successive changes into one emission or repeat a state, so wait for the
     * expected state instead of counting emissions.
     */
    private suspend fun ReceiveTurbine<List<Task>>.awaitUntil(expected: (List<Task>) -> Boolean): List<Task> {
        while (true) {
            val tasks = awaitItem()
            if (expected(tasks)) return tasks
        }
    }
}
