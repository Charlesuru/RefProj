package com.studiodraug.refproj.feature.profile

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.studiodraug.refproj.core.navigation.NavEntryInstaller
import com.studiodraug.refproj.core.navigation.ProfileRoute
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin bindings and navigation entries of the profile feature. */
val profileModule: Module =
    module {
        viewModelOf(::ProfileViewModel)
        single<NavEntryInstaller>(named("profile")) { NavEntryInstaller { profileEntries() } }
    }

private fun EntryProviderScope<NavKey>.profileEntries() {
    entry<ProfileRoute> {
        val viewModel = koinViewModel<ProfileViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        ProfileScreen(state = state, onAction = viewModel::onAction)
    }
}
