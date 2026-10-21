package com.studiodraug.refproj.feature.tasks

import com.studiodraug.refproj.core.ui.ScreenPreview
import com.studiodraug.refproj.core.ui.ScreenPreviewGroup
import com.studiodraug.refproj.domain.tasks.Task
import com.studiodraug.refproj.domain.tasks.TaskTitleError
import kotlin.time.Instant

/** Sample states for the debug menu's screen catalog. */
internal val tasksPreviews =
    ScreenPreviewGroup(
        title = "Tasks",
        previews =
            listOf(
                ScreenPreview("Loading") { TasksScreen(state = TasksUiState(isLoading = true), onAction = {}) },
                ScreenPreview("Empty") { TasksScreen(state = TasksUiState(isLoading = false), onAction = {}) },
                ScreenPreview("List") { TasksScreen(state = withTasks, onAction = {}) },
                ScreenPreview("Title too long") {
                    TasksScreen(
                        state = withTasks.copy(newTitle = "A".repeat(Task.MAX_TITLE_LENGTH + 1), titleError = TaskTitleError.TooLong),
                        onAction = {},
                    )
                },
            ),
    )

private val withTasks =
    TasksUiState(
        isLoading = false,
        tasks =
            listOf(
                Task(id = 1, title = "Buy milk", isDone = false, createdAt = Instant.fromEpochMilliseconds(0)),
                Task(id = 2, title = "Call the dentist", isDone = false, createdAt = Instant.fromEpochMilliseconds(0)),
                Task(id = 3, title = "Book train tickets", isDone = true, createdAt = Instant.fromEpochMilliseconds(0)),
            ),
    )
