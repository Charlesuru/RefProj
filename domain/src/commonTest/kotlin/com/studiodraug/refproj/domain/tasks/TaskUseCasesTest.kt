package com.studiodraug.refproj.domain.tasks

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

// UNIT TEST
class TaskUseCasesTest {
    private val repository = FakeTaskRepository()

    @Test
    fun add_trimsTheTitle() =
        runTest {
            assertEquals(AddTaskResult.Added, AddTaskUseCase(repository)("  Buy milk  "))
            assertEquals(listOf("Buy milk"), repository.tasks.value.map { it.title })
        }

    @Test
    fun add_rejectsBlankAndTooLongTitles_withoutStoringThem() =
        runTest {
            val add = AddTaskUseCase(repository)
            assertEquals(AddTaskResult.Invalid(TaskTitleError.Blank), add("   "))
            assertEquals(AddTaskResult.Invalid(TaskTitleError.TooLong), add("x".repeat(Task.MAX_TITLE_LENGTH + 1)))
            assertTrue(repository.tasks.value.isEmpty())
        }

    @Test
    fun add_acceptsATitleOfExactlyTheMaximumLength() =
        runTest {
            assertEquals(AddTaskResult.Added, AddTaskUseCase(repository)("x".repeat(Task.MAX_TITLE_LENGTH)))
        }

    @Test
    fun summary_countsOpenAndDoneTasks() =
        runTest {
            ObserveTaskSummaryUseCase(repository)().test {
                assertEquals(TaskSummary(open = 0, done = 0), awaitItem())
                repository.add("A")
                assertEquals(TaskSummary(open = 1, done = 0), awaitItem())
                repository.add("B")
                assertEquals(TaskSummary(open = 2, done = 0), awaitItem())
                SetTaskDoneUseCase(repository)(id = 1, done = true)
                assertEquals(TaskSummary(open = 1, done = 1), awaitItem())
                DeleteTaskUseCase(repository)(id = 2)
                assertEquals(TaskSummary(open = 0, done = 1), awaitItem())
            }
        }
}

/** In-memory [TaskRepository] with sequential IDs starting at 1. */
internal class FakeTaskRepository : TaskRepository {
    override val tasks: MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())
    private var nextId = 1L

    override suspend fun add(title: String) {
        tasks.value += Task(id = nextId++, title = title, isDone = false, createdAt = Instant.fromEpochMilliseconds(0))
    }

    override suspend fun setDone(
        id: Long,
        done: Boolean,
    ) {
        tasks.value = tasks.value.map { if (it.id == id) it.copy(isDone = done) else it }
    }

    override suspend fun delete(id: Long) {
        tasks.value = tasks.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        tasks.value = emptyList()
    }
}
