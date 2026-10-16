package com.studiodraug.refproj.feature.tasks

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.studiodraug.refproj.core.navigation.NavEntryInstaller
import com.studiodraug.refproj.core.navigation.TasksRoute
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin bindings and navigation entries of the tasks feature. */
val tasksModule: Module =
    module {
        viewModelOf(::TasksViewModel)
        single<NavEntryInstaller>(named("tasks")) { NavEntryInstaller { tasksEntries() } }
    }

private fun EntryProviderScope<NavKey>.tasksEntries() {
    entry<TasksRoute> {
        val viewModel = koinViewModel<TasksViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        TasksScreen(state = state, onAction = viewModel::onAction)
    }
}
