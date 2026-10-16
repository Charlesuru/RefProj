package com.studiodraug.refproj.feature.tasks

import com.studiodraug.refproj.core.observability.AnalyticsEvent
import com.studiodraug.refproj.core.testing.FakeAnalyticsTracker
import com.studiodraug.refproj.core.testing.runViewModelTest
import com.studiodraug.refproj.domain.tasks.AddTaskUseCase
import com.studiodraug.refproj.domain.tasks.DeleteTaskUseCase
import com.studiodraug.refproj.domain.tasks.ObserveTasksUseCase
import com.studiodraug.refproj.domain.tasks.SetTaskDoneUseCase
import com.studiodraug.refproj.domain.tasks.Task
import com.studiodraug.refproj.domain.tasks.TaskRepository
import com.studiodraug.refproj.domain.tasks.TaskTitleError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// UNIT TEST
class TasksViewModelTest {
    private val repository = FakeTaskRepository()
    private val analytics = FakeAnalyticsTracker()

    private fun viewModel() =
        TasksViewModel(
            ObserveTasksUseCase(repository),
            AddTaskUseCase(repository),
            SetTaskDoneUseCase(repository),
            DeleteTaskUseCase(repository),
            analytics,
        )

    @Test
    fun isLoading_untilTheFirstListArrives() =
        runViewModelTest {
            repository.delayFirstEmission = true
            val viewModel = viewModel()
            assertTrue(viewModel.state.value.isLoading)
            repository.release()
            assertFalse(viewModel.state.value.isLoading)
        }

    @Test
    fun add_clearsTheField_andShowsTheTask() =
        runViewModelTest {
            val viewModel = viewModel()
            viewModel.onAction(TasksAction.NewTitleChanged("Buy milk"))
            viewModel.onAction(TasksAction.Add)

            assertEquals("", viewModel.state.value.newTitle)
            assertEquals(
                listOf("Buy milk"),
                viewModel.state.value.tasks
                    .map { it.title },
            )
            assertEquals(listOf(AnalyticsEvent("task_added")), analytics.events)
        }

    @Test
    fun aBlankTitle_showsAnError_untilTheUserTypes() =
        runViewModelTest {
            val viewModel = viewModel()
            viewModel.onAction(TasksAction.NewTitleChanged("  "))
            viewModel.onAction(TasksAction.Add)
            assertEquals(TaskTitleError.Blank, viewModel.state.value.titleError)
            assertEquals("  ", viewModel.state.value.newTitle)

            viewModel.onAction(TasksAction.NewTitleChanged("  x"))
            assertNull(viewModel.state.value.titleError)
        }

    @Test
    fun completing_andDeleting_updateTheList() =
        runViewModelTest {
            val viewModel = viewModel()
            viewModel.onAction(TasksAction.NewTitleChanged("A"))
            viewModel.onAction(TasksAction.Add)
            val id =
                viewModel.state.value.tasks
                    .single()
                    .id

            viewModel.onAction(TasksAction.DoneChanged(id, done = true))
            assertTrue(
                viewModel.state.value.tasks
                    .single()
                    .isDone,
            )
            assertEquals(AnalyticsEvent("task_completed"), analytics.events.last())

            viewModel.onAction(TasksAction.Delete(id))
            assertTrue(
                viewModel.state.value.tasks
                    .isEmpty(),
            )
        }

    private class FakeTaskRepository : TaskRepository {
        private val state = MutableStateFlow(emptyList<Task>())
        private val gate = CompletableDeferred<Unit>()
        var delayFirstEmission = false
        private var nextId = 1L

        override val tasks =
            flow {
                if (delayFirstEmission) gate.await()
                emitAll(state)
            }

        fun release() {
            gate.complete(Unit)
        }

        override suspend fun add(title: String) {
            state.value += Task(nextId++, title, isDone = false, createdAt = Instant.fromEpochMilliseconds(0))
        }

        override suspend fun setDone(
            id: Long,
            done: Boolean,
        ) {
            state.value = state.value.map { if (it.id == id) it.copy(isDone = done) else it }
        }

        override suspend fun delete(id: Long) {
            state.value = state.value.filterNot { it.id == id }
        }

        override suspend fun deleteAll() {
            state.value = emptyList()
        }
    }
}
