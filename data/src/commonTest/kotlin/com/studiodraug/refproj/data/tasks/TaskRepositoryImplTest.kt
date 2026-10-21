package com.studiodraug.refproj.data.tasks

import app.cash.turbine.test
import com.studiodraug.refproj.core.testing.PlatformTest
import com.studiodraug.refproj.data.database.inMemoryTestDatabase
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
                assertTrue(awaitItem().isEmpty())
                repository.add("A")
                val added = awaitItem().single()
                repository.delete(added.id)
                assertTrue(awaitItem().isEmpty())
                repository.add("B")
                repository.add("C")
                skipItems(2)
                repository.deleteAll()
                assertTrue(awaitItem().isEmpty())
            }
        }
}
