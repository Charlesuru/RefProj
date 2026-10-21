package com.studiodraug.refproj.feature.tasks

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.core.testing.PlatformTest
import com.studiodraug.refproj.domain.tasks.Task
import com.studiodraug.refproj.domain.tasks.TaskTitleError
import com.studiodraug.refproj.feature.tasks.resources.Res
import com.studiodraug.refproj.feature.tasks.resources.tasks_add
import com.studiodraug.refproj.feature.tasks.resources.tasks_delete
import com.studiodraug.refproj.feature.tasks.resources.tasks_empty_title
import com.studiodraug.refproj.feature.tasks.resources.tasks_error_blank
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import org.jetbrains.compose.resources.getString

// UI TEST

/** The task list: empty state, ticking and deleting tasks, adding with validation. */
@OptIn(ExperimentalTestApi::class)
class TasksScreenTest : PlatformTest() {
    private val open = Task(id = 1, title = "Buy milk", isDone = false, createdAt = Instant.fromEpochMilliseconds(0))
    private val done = Task(id = 2, title = "Call Ada", isDone = true, createdAt = Instant.fromEpochMilliseconds(0))

    @Test
    fun noTasks_showsTheEmptyState() =
        runComposeUiTest {
            setContent { RefTheme { TasksScreen(state = TasksUiState(isLoading = false), onAction = {}) } }

            onNodeWithText(getString(Res.string.tasks_empty_title)).assertExists()
        }

    @Test
    fun tasks_showTheirState_andCanBeTickedAndDeleted() =
        runComposeUiTest {
            val actions = mutableListOf<TasksAction>()
            setContent {
                RefTheme {
                    TasksScreen(state = TasksUiState(tasks = listOf(open, done), isLoading = false), onAction = {
                        actions +=
                            it
                    })
                }
            }

            onNodeWithText("Buy milk").assertIsOff().performClick()
            onNodeWithText("Call Ada").assertIsOn()
            onNodeWithContentDescription(getString(Res.string.tasks_delete, "Call Ada")).performClick()

            assertEquals(listOf(TasksAction.DoneChanged(1, true), TasksAction.Delete(2)), actions)
        }

    @Test
    fun adding_sendsAdd_andShowsTheValidationError() =
        runComposeUiTest {
            val actions = mutableListOf<TasksAction>()
            setContent {
                RefTheme {
                    TasksScreen(state = TasksUiState(isLoading = false, titleError = TaskTitleError.Blank), onAction = { actions += it })
                }
            }

            onNodeWithText(getString(Res.string.tasks_error_blank)).assertExists()
            onNode(hasText(getString(Res.string.tasks_add)) and hasClickAction()).performClick()

            assertEquals(listOf<TasksAction>(TasksAction.Add), actions)
        }
}
