package com.studiodraug.refproj.feature.home

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.studiodraug.refproj.core.navigation.HomeRoute
import com.studiodraug.refproj.core.navigation.NavEntryInstaller
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin bindings and navigation entries of the home feature. */
val homeModule: Module =
    module {
        viewModelOf(::HomeViewModel)
        single<NavEntryInstaller>(named("home")) { NavEntryInstaller { homeEntries() } }
    }

private fun EntryProviderScope<NavKey>.homeEntries() {
    entry<HomeRoute> {
        val viewModel = koinViewModel<HomeViewModel>()
        val state = viewModel.state.collectAsStateWithLifecycle()
        HomeScreen(state = state.value)
    }
}
