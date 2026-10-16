package com.studiodraug.refproj.data.tasks

import com.studiodraug.refproj.domain.tasks.Task
import com.studiodraug.refproj.domain.tasks.TaskRepository
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Tasks in the local Room database. */
internal class TaskRepositoryImpl(
    private val dao: TaskDao,
    private val clock: Clock,
) : TaskRepository {
    override val tasks: Flow<List<Task>> = dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun add(title: String) {
        dao.insert(TaskEntity(title = title, isDone = false, createdAtEpochMillis = clock.now().toEpochMilliseconds()))
    }

    override suspend fun setDone(
        id: Long,
        done: Boolean,
    ) = dao.setDone(id, done)

    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun deleteAll() = dao.deleteAll()
}

private fun TaskEntity.toDomain() =
    Task(id = id, title = title, isDone = isDone, createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis))
