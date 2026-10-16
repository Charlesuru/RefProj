package com.studiodraug.refproj.domain.tasks

import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** A to-do item of the signed-in user. Stored on the device only; deleted on logout. */
data class Task(
    val id: Long,
    val title: String,
    val isDone: Boolean,
    val createdAt: Instant,
) {
    companion object {
        const val MAX_TITLE_LENGTH = 200
    }
}

/** Counts for the home screen. */
data class TaskSummary(
    val open: Int,
    val done: Int,
)

/** Implemented in the data layer. [tasks] lists open tasks first, newest first within each group. */
interface TaskRepository {
    val tasks: Flow<List<Task>>

    suspend fun add(title: String)

    suspend fun setDone(
        id: Long,
        done: Boolean,
    )

    suspend fun delete(id: Long)

    suspend fun deleteAll()
}

class ObserveTasksUseCase(
    private val repository: TaskRepository,
) {
    operator fun invoke(): Flow<List<Task>> = repository.tasks
}

class ObserveTaskSummaryUseCase(
    private val repository: TaskRepository,
) {
    operator fun invoke(): Flow<TaskSummary> =
        repository.tasks.map { tasks ->
            val done = tasks.count { it.isDone }
            TaskSummary(open = tasks.size - done, done = done)
        }
}

enum class TaskTitleError {
    Blank,
    TooLong,
}

sealed interface AddTaskResult {
    data object Added : AddTaskResult

    data class Invalid(
        val error: TaskTitleError,
    ) : AddTaskResult
}

/** Trims the title and rejects blank or too long ones (see [Task.MAX_TITLE_LENGTH]). */
class AddTaskUseCase(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(title: String): AddTaskResult {
        val trimmed = title.trim()
        return when {
            trimmed.isEmpty() -> {
                AddTaskResult.Invalid(TaskTitleError.Blank)
            }

            trimmed.length > Task.MAX_TITLE_LENGTH -> {
                AddTaskResult.Invalid(TaskTitleError.TooLong)
            }

            else -> {
                repository.add(trimmed)
                AddTaskResult.Added
            }
        }
    }
}

class SetTaskDoneUseCase(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(
        id: Long,
        done: Boolean,
    ) = repository.setDone(id, done)
}

class DeleteTaskUseCase(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}
