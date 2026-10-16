package com.studiodraug.refproj.feature.tasks

import androidx.lifecycle.viewModelScope
import com.studiodraug.refproj.core.observability.AnalyticsEvent
import com.studiodraug.refproj.core.observability.AnalyticsTracker
import com.studiodraug.refproj.core.ui.MviViewModel
import com.studiodraug.refproj.domain.tasks.AddTaskResult
import com.studiodraug.refproj.domain.tasks.AddTaskUseCase
import com.studiodraug.refproj.domain.tasks.DeleteTaskUseCase
import com.studiodraug.refproj.domain.tasks.ObserveTasksUseCase
import com.studiodraug.refproj.domain.tasks.SetTaskDoneUseCase
import com.studiodraug.refproj.domain.tasks.Task
import com.studiodraug.refproj.domain.tasks.TaskTitleError
import kotlinx.coroutines.launch

data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val newTitle: String = "",
    val titleError: TaskTitleError? = null,
)

sealed interface TasksAction {
    data class NewTitleChanged(
        val value: String,
    ) : TasksAction

    data object Add : TasksAction

    data class DoneChanged(
        val id: Long,
        val done: Boolean,
    ) : TasksAction

    data class Delete(
        val id: Long,
    ) : TasksAction
}

class TasksViewModel(
    observeTasks: ObserveTasksUseCase,
    private val addTask: AddTaskUseCase,
    private val setTaskDone: SetTaskDoneUseCase,
    private val deleteTask: DeleteTaskUseCase,
    private val analytics: AnalyticsTracker,
) : MviViewModel<TasksUiState, TasksAction, Nothing>(TasksUiState()) {
    init {
        viewModelScope.launch {
            observeTasks().collect { tasks -> updateState { copy(tasks = tasks, isLoading = false) } }
        }
    }

    override fun onAction(action: TasksAction) {
        when (action) {
            is TasksAction.NewTitleChanged -> {
                updateState { copy(newTitle = action.value, titleError = null) }
            }

            TasksAction.Add -> {
                add()
            }

            is TasksAction.DoneChanged -> {
                viewModelScope.launch {
                    setTaskDone(action.id, action.done)
                    if (action.done) analytics.logEvent(AnalyticsEvent("task_completed"))
                }
            }

            is TasksAction.Delete -> {
                viewModelScope.launch { deleteTask(action.id) }
            }
        }
    }

    private fun add() {
        val title = currentState.newTitle
        viewModelScope.launch {
            when (val result = addTask(title)) {
                AddTaskResult.Added -> {
                    updateState { copy(newTitle = "", titleError = null) }
                    analytics.logEvent(AnalyticsEvent("task_added"))
                }

                is AddTaskResult.Invalid -> {
                    updateState { copy(titleError = result.error) }
                }
            }
        }
    }
}
