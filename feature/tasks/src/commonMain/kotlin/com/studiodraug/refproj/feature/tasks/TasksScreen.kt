package com.studiodraug.refproj.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.studiodraug.refproj.core.designsystem.component.RefButton
import com.studiodraug.refproj.core.designsystem.component.RefCheckboxListItem
import com.studiodraug.refproj.core.designsystem.component.RefContentWidth
import com.studiodraug.refproj.core.designsystem.component.RefDivider
import com.studiodraug.refproj.core.designsystem.component.RefEmptyState
import com.studiodraug.refproj.core.designsystem.component.RefLoadingState
import com.studiodraug.refproj.core.designsystem.component.RefTextField
import com.studiodraug.refproj.core.designsystem.component.RefTopAppBar
import com.studiodraug.refproj.core.designsystem.icon.RefIcons
import com.studiodraug.refproj.core.designsystem.theme.RefTheme
import com.studiodraug.refproj.domain.tasks.Task
import com.studiodraug.refproj.domain.tasks.TaskTitleError
import com.studiodraug.refproj.feature.tasks.resources.Res
import com.studiodraug.refproj.feature.tasks.resources.tasks_add
import com.studiodraug.refproj.feature.tasks.resources.tasks_delete
import com.studiodraug.refproj.feature.tasks.resources.tasks_empty_message
import com.studiodraug.refproj.feature.tasks.resources.tasks_empty_title
import com.studiodraug.refproj.feature.tasks.resources.tasks_error_blank
import com.studiodraug.refproj.feature.tasks.resources.tasks_error_too_long
import com.studiodraug.refproj.feature.tasks.resources.tasks_new_label
import com.studiodraug.refproj.feature.tasks.resources.tasks_title
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource

@Composable
fun TasksScreen(
    state: TasksUiState,
    onAction: (TasksAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { RefTopAppBar(title = stringResource(Res.string.tasks_title)) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        RefContentWidth(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                NewTaskRow(state = state, onAction = onAction)
                when {
                    state.isLoading -> {
                        RefLoadingState()
                    }

                    state.tasks.isEmpty() -> {
                        RefEmptyState(
                            title = stringResource(Res.string.tasks_empty_title),
                            message = stringResource(Res.string.tasks_empty_message),
                        )
                    }

                    else -> {
                        TaskList(tasks = state.tasks, onAction = onAction)
                    }
                }
            }
        }
    }
}

@Composable
private fun NewTaskRow(
    state: TasksUiState,
    onAction: (TasksAction) -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = RefTheme.spacing.screenHorizontal, vertical = RefTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(RefTheme.spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        RefTextField(
            value = state.newTitle,
            onValueChange = { onAction(TasksAction.NewTitleChanged(it)) },
            label = stringResource(Res.string.tasks_new_label),
            modifier = Modifier.weight(1f),
            error =
                when (state.titleError) {
                    TaskTitleError.Blank -> stringResource(Res.string.tasks_error_blank)
                    TaskTitleError.TooLong -> stringResource(Res.string.tasks_error_too_long, Task.MAX_TITLE_LENGTH)
                    null -> null
                },
            imeAction = ImeAction.Done,
            onImeAction = { onAction(TasksAction.Add) },
        )
        RefButton(
            text = stringResource(Res.string.tasks_add),
            onClick = { onAction(TasksAction.Add) },
            // Lines the button up with the outlined field, whose label sits above its border.
            modifier = Modifier.padding(top = RefTheme.spacing.sm),
        )
    }
}

@Composable
private fun TaskList(
    tasks: List<Task>,
    onAction: (TasksAction) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = tasks, key = { it.id }) { task ->
            RefCheckboxListItem(
                title = task.title,
                checked = task.isDone,
                onCheckedChange = { onAction(TasksAction.DoneChanged(task.id, it)) },
                modifier = Modifier.animateItem(),
                trailing = {
                    IconButton(onClick = { onAction(TasksAction.Delete(task.id)) }) {
                        Icon(imageVector = RefIcons.Delete, contentDescription = stringResource(Res.string.tasks_delete, task.title))
                    }
                },
            )
            RefDivider()
        }
    }
}

@Preview
@Composable
private fun TasksScreenPreview() {
    val created = Instant.fromEpochMilliseconds(0)
    RefTheme {
        TasksScreen(
            state =
                TasksUiState(
                    isLoading = false,
                    tasks =
                        listOf(
                            Task(id = 1, title = "Buy milk", isDone = false, createdAt = created),
                            Task(id = 2, title = "Book dentist", isDone = true, createdAt = created),
                        ),
                ),
            onAction = {},
        )
    }
}
