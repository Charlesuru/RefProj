package com.studiodraug.refproj.feature.login

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.studiodraug.refproj.core.navigation.LoginRoute
import com.studiodraug.refproj.core.navigation.NavEntryInstaller
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Koin bindings and navigation entries of the login feature. */
val loginModule: Module =
    module {
        viewModelOf(::LoginViewModel)
        single<NavEntryInstaller>(named("login")) { NavEntryInstaller { loginEntries() } }
    }

private fun EntryProviderScope<NavKey>.loginEntries() {
    entry<LoginRoute> {
        val viewModel = koinViewModel<LoginViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        LoginScreen(state = state, onAction = viewModel::onAction)
    }
}
